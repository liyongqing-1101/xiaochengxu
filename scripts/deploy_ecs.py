#!/usr/bin/env python3
"""Deploy latest code to ECS and restart services"""
import paramiko
import time

HOST = '47.97.193.230'
USER = 'root'
PASSWORD = 'Liyong981027'

def ssh_connect():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30)
    return client

def run(client, cmd, timeout=120):
    print(f'>>> {cmd}')
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    exit_code = stdout.channel.recv_exit_status()
    if out:
        print(out[-500:])
    if err:
        print(f'STDERR: {err[-500:]}')
    return exit_code, out, err

def main():
    client = ssh_connect()
    try:
        # Step 1: Git pull latest code
        print('\n=== Step 1: Git Pull ===')
        run(client, 'cd /home/xiaochengxu && git pull origin main')

        # Step 2: Open firewall port 3001
        print('\n=== Step 2: Open UFW port 3001 ===')
        run(client, 'ufw allow 3001/tcp')
        run(client, 'ufw status')

        # Step 3: Stop backend to free memory
        print('\n=== Step 3: Stop backend ===')
        run(client, 'pkill -9 -f "exam-backend" || true')
        time.sleep(2)
        run(client, 'pkill -9 -f "static_server.py" || true')
        time.sleep(2)

        # Step 4: Build backend
        print('\n=== Step 4: Build backend (mvn clean package) ===')
        exit_code, out, err = run(client,
            'cd /home/xiaochengxu/backend && mvn clean package -DskipTests -q 2>&1',
            timeout=300)
        if exit_code != 0:
            print(f'Backend build FAILED (exit={exit_code}). Checking full error...')
            run(client, 'cd /home/xiaochengxu/backend && mvn clean package -DskipTests 2>&1 | tail -80',
                timeout=300)
            return False

        # Step 5: Build frontend
        print('\n=== Step 5: Build frontend (vite build) ===')
        exit_code, out, err = run(client,
            'cd /home/xiaochengxu/admin-frontend && rm -rf dist && NODE_OPTIONS="--max-old-space-size=512" npx vite build 2>&1',
            timeout=300)
        if exit_code != 0:
            print(f'Frontend build FAILED (exit={exit_code})')
            return False

        # Step 6: Start backend
        print('\n=== Step 6: Start backend ===')
        run(client,
            'cd /home/xiaochengxu/backend && nohup java -Xms64m -Xmx192m -XX:+UseSerialGC -jar target/exam-backend-1.0.0.jar > /home/backend.log 2>&1 &')
        time.sleep(10)

        # Verify backend started
        exit_code, out, err = run(client, 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/doc.html')
        print(f'Backend HTTP status: {out.strip()}')

        # Step 7: Start static file server
        print('\n=== Step 7: Start static file server ===')
        # Ensure static_server.py exists
        run(client, 'ls -la /home/xiaochengxu/scripts/static_server.py')
        run(client,
            'cd /home/xiaochengxu && nohup python3 scripts/static_server.py > /home/admin-frontend.log 2>&1 &')
        time.sleep(3)

        # Verify frontend
        exit_code, out, err = run(client, 'curl -s -o /dev/null -w "%{http_code}" http://localhost:3001')
        print(f'Frontend HTTP status: {out.strip()}')

        # Step 8: Check backend logs
        print('\n=== Step 8: Backend logs (last 30 lines) ===')
        run(client, 'tail -30 /home/backend.log')

        print('\n=== Deploy Complete! ===')
        print('Backend:  http://47.97.193.230:8080/api/doc.html')
        print('Frontend: http://47.97.193.230:3001')
        return True

    finally:
        client.close()

if __name__ == '__main__':
    main()
