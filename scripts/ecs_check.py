import paramiko
import sys
import time

host = "47.97.193.230"
port = 22
username = "root"
password = "Liyong9!*"

def ssh_connect():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, port, username, password, timeout=30)
    return client

def run_cmd(client, cmd, timeout=120):
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if out:
        print(out)
    if err:
        print(f"STDERR: {err}")
    return out, err

def run_cmd_stream(client, cmd, timeout=300):
    """Run command with streaming output (for long-running commands)"""
    print(f"\n>>> {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)

    # Stream stdout
    while not stdout.channel.exit_status_ready():
        if stdout.channel.recv_ready():
            data = stdout.channel.recv(4096).decode('utf-8', errors='replace')
            print(data, end='', flush=True)
        time.sleep(0.1)

    # Get remaining
    try:
        remaining = stdout.read().decode('utf-8', errors='replace')
        if remaining:
            print(remaining)
    except:
        pass

    err = stderr.read().decode('utf-8', errors='replace')
    if err:
        print(f"STDERR: {err}")
    return err

print("=" * 60)
print("Connecting to ECS server...")
print("=" * 60)

client = ssh_connect()
print("Connected!")

# 1. Check current state
print("\n\n=== STEP 1: Check current state ===")
run_cmd(client, "uname -a")
run_cmd(client, "cat /etc/os-release | head -4")
run_cmd(client, "df -h / | tail -2")
run_cmd(client, "free -h | head -2")

# 2. Check if git is installed
print("\n\n=== STEP 2: Check git ===")
run_cmd(client, "git --version 2>&1 || echo 'git not installed'")

# 3. Check Java
print("\n\n=== STEP 3: Check Java ===")
run_cmd(client, "java -version 2>&1 || echo 'java not installed'")

# 4. Check Node.js
print("\n\n=== STEP 4: Check Node.js ===")
run_cmd(client, "node --version 2>&1 && npm --version 2>&1 || echo 'node not installed'")

# 5. Check Docker and running containers
print("\n\n=== STEP 5: Check Docker ===")
run_cmd(client, "docker ps --format '{{.Names}}: {{.Status}}' 2>&1")

# 6. Check existing directories
print("\n\n=== STEP 6: Check existing files ===")
run_cmd(client, "ls -la /root/")

# 7. Check if repo already cloned
run_cmd(client, "ls -la /root/xiaochengxu/ 2>&1 || echo 'repo not cloned yet'")

client.close()
print("\n\nDone with initial check!")
