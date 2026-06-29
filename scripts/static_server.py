#!/usr/bin/env python3
"""
轻量级静态文件服务器 + API 反向代理
用于 ECS 上托管管理后台前端构建产物，并将 /api/* 请求代理到后端
"""
import http.server
import socketserver
import urllib.request
import urllib.error
import os
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3001
BACKEND = "http://localhost:8080"
STATIC_DIR = "/home/xiaochengxu/admin-frontend/dist"

class ProxyHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    def do_GET(self):
        if self.path.startswith('/api/'):
            self.proxy_request('GET')
        else:
            # SPA fallback: serve index.html for non-file routes
            if not os.path.exists(os.path.join(STATIC_DIR, self.path.lstrip('/'))) and '.' not in self.path.split('/')[-1]:
                self.path = '/index.html'
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/api/'):
            self.proxy_request('POST')
        else:
            super().do_POST()

    def do_PUT(self):
        if self.path.startswith('/api/'):
            self.proxy_request('PUT')
        else:
            super().do_PUT()

    def do_DELETE(self):
        if self.path.startswith('/api/'):
            self.proxy_request('DELETE')
        else:
            super().do_DELETE()

    def proxy_request(self, method):
        url = BACKEND + self.path
        body = None
        content_length = int(self.headers.get('Content-Length', 0))
        if content_length > 0:
            body = self.rfile.read(content_length)

        req = urllib.request.Request(url, data=body, method=method)
        # Forward headers
        for key, value in self.headers.items():
            if key.lower() not in ('host', 'connection'):
                req.add_header(key, value)

        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                self.send_response(resp.status)
                for key, value in resp.headers.items():
                    if key.lower() not in ('transfer-encoding', 'connection'):
                        self.send_header(key, value)
                self.end_headers()
                self.wfile.write(resp.read())
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            self.end_headers()
            try:
                self.wfile.write(e.read())
            except:
                pass
        except Exception as e:
            self.send_response(502)
            self.end_headers()
            self.wfile.write(f'{{"code":502,"message":"Backend unavailable: {e}"}}'.encode())

    def log_message(self, format, *args):
        print(f"[{self.log_date_time_string()}] {args[0]}", flush=True)

if __name__ == '__main__':
    print(f"Serving {STATIC_DIR} on port {PORT}, proxying /api -> {BACKEND}", flush=True)
    with socketserver.TCPServer(("0.0.0.0", PORT), ProxyHandler) as httpd:
        httpd.serve_forever()
