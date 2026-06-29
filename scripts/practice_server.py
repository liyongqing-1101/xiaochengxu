#!/usr/bin/env python3
"""HTTP server for practice page on port 3002"""
import http.server
import socketserver
import urllib.request
import urllib.error
import json
import os
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3002
BACKEND = "http://localhost:8080"
STATIC_DIR = os.path.dirname(os.path.abspath(__file__))

class PracticeHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)

    def do_GET(self):
        if self.path.startswith('/api/'):
            self.proxy_request('GET')
        else:
            # SPA fallback: serve practice.html for any non-file route
            path = self.path.split('?')[0]
            if path == '/' or not os.path.exists(os.path.join(STATIC_DIR, path.lstrip('/'))):
                self.path = '/practice.html'
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/api/'):
            self.proxy_request('POST')
        else:
            self.send_error(404)

    def proxy_request(self, method):
        url = BACKEND + self.path
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length) if content_length > 0 else None

        headers = {}
        for key, value in self.headers.items():
            if key.lower() in ('host', 'content-length'):
                continue
            headers[key] = value

        req = urllib.request.Request(url, data=body, headers=headers, method=method)
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
            self.wfile.write(e.read())

if __name__ == '__main__':
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.ThreadingTCPServer(("0.0.0.0", PORT), PracticeHandler) as httpd:
        print(f"Practice server running on port {PORT}")
        httpd.serve_forever()
