"""Stand-in for the Ithaki lab server of Computer Networks II, for running networks-2 without the lab.

Written in October 2026 to check the published code. It follows the assignment text and a packet
capture of the lab, answers on 127.0.0.1 and does not implement the whole lab: echo (with a delay, and
without it for the code E0000), the temperature variant of the echo, camera images in 128-byte packets,
audio in the DPCM and AQ-DPCM layouts (silence), Ithakicopter telemetry and OBD-II requests.

Usage:  python mock_lab.py [image.jpg]
The image defaults to the session 1 camera picture of networks-2. Ports: 38000 (all requests except the
copter) and 48079 (copter). Set server_ip=127.0.0.1 and copter_server_port=48079 in networks-2's
ithaki.properties (see tools/README.md).
"""
import os
import random
import socket
import struct
import sys
import threading
import time

HERE = os.path.dirname(os.path.abspath(__file__))
IMAGE = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "networks-2", "results", "session-1",
                                                           "camera-fixed.jpg")
JPEG = open(IMAGE, "rb").read()
MAIN_PORT, COPTER_PORT = 38000, 48079
STAMP = "14-01-2021 21:54:13"
OBD = {"1F": "41 1F 00 2A", "0F": "41 0F 4B", "11": "41 11 33", "0C": "41 0C 1A F8", "0D": "41 0D 3C", "05": "41 05 7B"}
lock = threading.Lock()
stats = {}


def count(kind):
    with lock:
        stats[kind] = stats.get(kind, 0) + 1


def audio_packets(request):
    adaptive = "AQ" in request
    digits = "".join(ch for ch in request.split("F")[-1] if ch.isdigit()) if "F" in request else ""
    for _ in range(int(digits) if digits else 300):
        body = bytes([0x88] * 128)  # both nibbles 8: a difference of zero, so silence
        yield struct.pack("<hh", 0, 1) + body if adaptive else body


def handle(sock, data, addr):
    request = data.decode("latin1")
    if request[0] == "E":
        count("echo")
        # the lab answered about 4 packets per second without delay and about 0.7 per second with delay
        time.sleep(0.25 if request == "E0000\r" else random.uniform(0.2, 1.0))
        if "T00" in request:
            reply = "PSTART %s T00 14-01 21:50 +05 C PSTOP" % STAMP
        else:
            reply = "PSTART %s PSTOP" % STAMP
        sock.sendto(reply.encode(), addr)
    elif request[0] == "M":
        count("image")
        for i in range(0, len(JPEG), 128):
            sock.sendto(JPEG[i:i + 128], addr)
            time.sleep(0.001)
    elif request[0] == "A":
        count("audio")
        for packet in audio_packets(request):
            sock.sendto(packet, addr)
            time.sleep(0.004)
    elif request[0] == "V":
        count("obd")
        time.sleep(0.05)
        sock.sendto(OBD[request.split("OBD=01 ")[1].strip()].encode(), addr)
    elif request[0] == "Q":
        count("copter")
        time.sleep(0.05)
        reply = "ITHAKICOPTER %s LMOTOR=000 RMOTOR=000 ALTITUDE=066 TEMPERATURE=+21.63 PRESSURE=1004.11 TELEMETRY" % STAMP
        sock.sendto(reply.encode(), addr)


def serve(port):
    server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server.bind(("127.0.0.1", port))
    while True:
        data, addr = server.recvfrom(2048)
        threading.Thread(target=handle, args=(server, data, addr), daemon=True).start()


threading.Thread(target=serve, args=(COPTER_PORT,), daemon=True).start()
threading.Thread(target=serve, args=(MAIN_PORT,), daemon=True).start()
print("mock lab listening on 127.0.0.1, ports", MAIN_PORT, "and", COPTER_PORT, flush=True)
while True:
    time.sleep(30)
    print("requests answered so far:", stats, flush=True)
