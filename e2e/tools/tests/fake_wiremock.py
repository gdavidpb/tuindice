#!/usr/bin/env python3
"""Fake WireMock for the harness tests: answers 200 to every request on $PORT. Any argument is ignored
(the tests pass the ownership marker as one so that the command line shows it). With the argument
`ignore-term` it ignores SIGTERM, like a server that is slow to stop."""

import http.server
import os
import signal
import sys


class Handler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'{"requests": []}')

    do_POST = do_DELETE = do_GET

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    if "ignore-term" in sys.argv:
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
    port = int(os.environ["PORT"])
    server = http.server.HTTPServer(("127.0.0.1", port), Handler)
    print("fake wiremock listening on %d (pid %d)" % (port, os.getpid()), flush=True)
    server.serve_forever()
