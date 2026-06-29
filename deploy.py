#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
自动部署脚本 - 将本地修改同步到阿里云 ECS 并重新部署
用法: python deploy.py [backend|frontend|all]
  backend   - 仅部署后端 (SpringBoot)
  frontend  - 仅部署管理后台前端 (Vue3)
  all       - 全部部署 (默认)
"""

import paramiko
import sys
import os
import time
from datetime import datetime

# ========== 配置 ==========
ECS_HOST = "47.97.193.230"
ECS_PORT = 22
ECS_USER = "root"
ECS_PASSWORD = "Liyong9!*"

PROJECT_DIR = "/home/xiaochengxu"
BACKEND_DIR = f"{PROJECT_DIR}/backend"
FRONTEND_DIR = f"{PROJECT_DIR}/admin-frontend"

# 本地路径
LOCAL_PROJECT = os.path.dirname(os.path.abspath(__file__))

# ========== 颜色输出 ==========
class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

def log(msg, color=Colors.RESET):
    ts = datetime.now().strftime("%H:%M:%S")
    print(f"{color}[{ts}] {msg}{Colors.RESET}", flush=True)

def log_step(msg):
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'='*60}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}  {msg}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'='*60}{Colors.RESET}\n", flush=True)

# ========== SSH 连接 ==========
def connect_ssh():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(ECS_HOST, ECS_PORT, ECS_USER, ECS_PASSWORD, timeout=30)
    return client

def run_remote(client, cmd, timeout=120, show_output=True):
    """执行远程命令并打印输出"""
    if show_output:
        log(f"执行: {cmd}", Colors.BLUE)
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if show_output:
        if out.strip():
            print(out.strip())
        if err.strip():
            # Maven 和 npm 的常规输出有时走 stderr，不算错误
            if any(kw in err.lower() for kw in ['error', 'fail', 'exception', 'cannot']):
                log(f"STDERR: {err.strip()}", Colors.YELLOW)
            elif show_output:
                print(err.strip())
    return out, err

def run_remote_stream(client, cmd, timeout=600):
    """执行远程命令并流式输出（用于长时间运行的命令如 mvn package）"""
    log(f"执行: {cmd}", Colors.BLUE)
    transport = client.get_transport()
    channel = transport.open_session()
    channel.settimeout(timeout)
    channel.exec_command(cmd)
    channel.setblocking(0)

    buf = b''
    start = time.time()
    while time.time() - start < timeout:
        try:
            if channel.recv_ready():
                data = channel.recv(4096)
                print(data.decode('utf-8', errors='replace'), end='', flush=True)
                buf += data
        except:
            pass
        if channel.exit_status_ready() and not channel.recv_ready():
            break
        time.sleep(0.2)

    # 收集剩余输出
    try:
        while channel.recv_ready():
            data = channel.recv(4096)
            print(data.decode('utf-8', errors='replace'), end='', flush=True)
            buf += data
    except:
        pass

    exit_code = channel.recv_exit_status()
    return buf.decode('utf-8', errors='replace'), exit_code

# ========== Git 操作 ==========
def git_push():
    """推送本地修改到 GitHub"""
    log_step("📤 推送本地修改到 GitHub")
    os.chdir(LOCAL_PROJECT)

    # 检查是否有未提交的修改
    import subprocess
    result = subprocess.run(["git", "status", "--porcelain"], capture_output=True, text=True, cwd=LOCAL_PROJECT)
    if result.stdout.strip():
        log("有未提交的修改，自动提交...", Colors.YELLOW)
        subprocess.run(["git", "add", "-A"], cwd=LOCAL_PROJECT, capture_output=True)
        subprocess.run(["git", "commit", "-m", f"deploy: auto-deploy {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"],
                       cwd=LOCAL_PROJECT, capture_output=True)
    else:
        log("没有未提交的修改", Colors.GREEN)

    result = subprocess.run(["git", "push"], cwd=LOCAL_PROJECT, capture_output=True, text=True)
    if result.returncode != 0:
        log(f"Git push 失败: {result.stderr}", Colors.RED)
        return False
    log("Git push 成功 ✅", Colors.GREEN)
    return True

def remote_git_pull(client):
    """在 ECS 上拉取最新代码"""
    log_step("📥 ECS 拉取最新代码")
    out, err = run_remote(client, f"cd {PROJECT_DIR} && git pull")
    if "Already up to date" in out:
        log("代码已是最新", Colors.GREEN)
        return False  # 没有更新
    return True  # 有更新

# ========== 后端部署 ==========
def deploy_backend(client):
    """部署后端: git pull → mvn package → 重启服务"""
    log_step("🔧 部署后端 (SpringBoot)")

    # 1. 拉取代码
    remote_git_pull(client)

    # 2. Maven 打包
    log("📦 Maven 打包中...")
    out, exit_code = run_remote_stream(
        client,
        f"cd {BACKEND_DIR} && mvn package -DskipTests -q 2>&1",
        timeout=600
    )

    if exit_code != 0:
        log("Maven 打包失败! 尝试完整构建...", Colors.RED)
        out, exit_code = run_remote_stream(
            client,
            f"cd {BACKEND_DIR} && mvn clean package -DskipTests 2>&1",
            timeout=600
        )
        if exit_code != 0:
            log("❌ Maven 打包失败!", Colors.RED)
            return False

    # 检查 JAR 是否生成
    out, _ = run_remote(client, f"ls -la {BACKEND_DIR}/target/*.jar 2>&1", show_output=False)
    if '.jar' not in out:
        log("❌ JAR 文件未生成!", Colors.RED)
        return False

    log("Maven 打包成功 ✅", Colors.GREEN)

    # 3. 停止旧服务
    log("🛑 停止旧后端服务...")
    run_remote(client, "pkill -f 'exam-backend' 2>/dev/null; sleep 2")
    run_remote(client, "pkill -9 -f 'exam-backend' 2>/dev/null; sleep 1")

    # 4. 启动新服务
    log("🚀 启动新后端服务...")
    run_remote(client,
        f"cd {BACKEND_DIR} && nohup java -Xms128m -Xmx256m -jar target/exam-backend-1.0.0.jar "
        f"> /home/backend.log 2>&1 &"
    )

    # 5. 等待启动
    log("⏳ 等待服务启动 (15秒)...")
    time.sleep(15)

    # 6. 验证
    out, _ = run_remote(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/doc.html", show_output=False)
    if '200' in out:
        log("✅ 后端启动成功! http://47.97.193.230:8080/api/doc.html", Colors.GREEN)
    else:
        log(f"⚠️  后端可能未正常启动 (HTTP {out.strip()})，请检查日志", Colors.YELLOW)
        run_remote(client, "tail -30 /home/backend.log")

    # 7. 检查内存
    run_remote(client, "free -h | head -2")

    return True

# ========== 前端部署 ==========
def deploy_frontend(client):
    """部署管理后台前端: git pull → npm install → 重启 vite"""
    log_step("🎨 部署管理后台前端 (Vue3)")

    # 1. 拉取代码
    remote_git_pull(client)

    # 2. 安装依赖 (如果有 package.json 变化)
    log("📦 检查依赖更新...")
    run_remote(client, f"cd {FRONTEND_DIR} && npm install 2>&1 | tail -5", timeout=120)

    # 3. 停止旧服务
    log("🛑 停止旧前端服务...")
    run_remote(client, "pkill -f 'vite' 2>/dev/null; sleep 2")
    run_remote(client, "pkill -9 -f 'vite' 2>/dev/null; sleep 1")

    # 4. 启动新服务
    log("🚀 启动新前端服务...")
    run_remote(client,
        f"cd {FRONTEND_DIR} && NODE_OPTIONS='--max-old-space-size=256' "
        f"nohup npx vite --host 0.0.0.0 --port 3001 > /home/admin-frontend.log 2>&1 &"
    )

    # 5. 等待启动
    log("⏳ 等待服务启动 (8秒)...")
    time.sleep(8)

    # 6. 验证
    out, _ = run_remote(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost:3001", show_output=False)
    if '200' in out:
        log("✅ 前端启动成功! http://47.97.193.230:3001", Colors.GREEN)
    else:
        log(f"⚠️  前端可能未正常启动 (HTTP {out.strip()})，请检查日志", Colors.YELLOW)
        run_remote(client, "cat /home/admin-frontend.log")

    return True

# ========== 状态检查 ==========
def check_status(client):
    """检查所有服务状态"""
    log_step("📊 服务状态")
    run_remote(client, "echo '--- 进程 ---' && ps aux | grep -E 'java|node|vite' | grep -v grep")
    run_remote(client, "echo '\n--- 内存 ---' && free -h | head -2")
    run_remote(client, "echo '\n--- 端口监听 ---' && ss -tlnp | grep -E '8080|3001'")

    # HTTP 测试
    be_out, _ = run_remote(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/doc.html", show_output=False)
    fe_out, _ = run_remote(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost:3001", show_output=False)
    log(f"后端 API: {'✅ HTTP ' + be_out.strip() if '200' in be_out else '❌ ' + be_out.strip()}", Colors.GREEN if '200' in be_out else Colors.RED)
    log(f"前端页面: {'✅ HTTP ' + fe_out.strip() if '200' in fe_out else '❌ ' + fe_out.strip()}", Colors.GREEN if '200' in fe_out else Colors.RED)

# ========== 查看日志 ==========
def view_logs(client, service='backend'):
    """查看服务日志"""
    log_file = f"/home/{'backend' if service == 'backend' else 'admin-frontend'}.log"
    lines = 50
    log_step(f"📋 {service} 日志 (最近 {lines} 行)")
    run_remote(client, f"tail -{lines} {log_file}")

# ========== 主流程 ==========
def main():
    target = sys.argv[1] if len(sys.argv) > 1 else 'all'
    action = sys.argv[2] if len(sys.argv) > 2 else None

    print(f"\n{Colors.BOLD}{Colors.CYAN}")
    print("╔══════════════════════════════════════════════════╗")
    print("║     🚀 高校教资刷题 - 自动部署脚本              ║")
    print("║     ECS: 47.97.193.230                          ║")
    print("╚══════════════════════════════════════════════════╝")
    print(f"{Colors.RESET}")

    # 处理特殊动作
    if action == 'logs':
        client = connect_ssh()
        try:
            view_logs(client, target)
        finally:
            client.close()
        return

    if action == 'status':
        client = connect_ssh()
        try:
            check_status(client)
        finally:
            client.close()
        return

    # 推送本地代码到 GitHub
    if not git_push():
        log("Git push 失败，中止部署", Colors.RED)
        sys.exit(1)

    # 连接 ECS
    log("🔌 连接 ECS 服务器...")
    client = connect_ssh()
    log("✅ 已连接到 ECS", Colors.GREEN)

    try:
        if target in ['all', 'backend']:
            deploy_backend(client)

        if target in ['all', 'frontend']:
            deploy_frontend(client)

        # 最终状态
        check_status(client)

    finally:
        client.close()

    print(f"\n{Colors.BOLD}{Colors.GREEN}")
    print("╔══════════════════════════════════════════════════╗")
    print("║     ✅ 部署完成!                                ║")
    print("╠══════════════════════════════════════════════════╣")
    print("║  后端 API 文档: http://47.97.193.230:8080/api/doc.html  ║")
    print("║  管理后台:     http://47.97.193.230:3001               ║")
    print("║  管理员登录:   admin / admin123                       ║")
    print("╚══════════════════════════════════════════════════╝")
    print(f"{Colors.RESET}")

if __name__ == '__main__':
    main()
