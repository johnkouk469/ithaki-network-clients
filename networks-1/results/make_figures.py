"""Summarise the recorded sessions and draw the figures used in docs/report.md.

Usage (from this folder):  python make_figures.py
Needs pandas and matplotlib. Reads session-*/echo.csv and session-*/arq.csv,
writes PNG files to ../docs/figures/ and prints the numbers quoted in the report.
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import pandas as pd

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "docs", "figures")
SURFACE, INK, MUTED, GRID, SERIES = "#fcfcfb", "#0b0b0b", "#52514e", "#e3e2dd", "#2a78d6"

plt.rcParams.update({
    "figure.facecolor": SURFACE, "axes.facecolor": SURFACE, "savefig.facecolor": SURFACE,
    "axes.edgecolor": MUTED, "axes.labelcolor": MUTED, "xtick.color": MUTED, "ytick.color": MUTED,
    "text.color": INK, "axes.titlesize": 11, "axes.labelsize": 9, "xtick.labelsize": 8,
    "ytick.labelsize": 8, "axes.spines.top": False, "axes.spines.right": False,
    "axes.grid": True, "grid.color": GRID, "grid.linewidth": 0.6, "axes.axisbelow": True,
})


def line_figure(values, title, path):
    fig, ax = plt.subplots(figsize=(7, 3))
    ax.plot(range(1, len(values) + 1), values, color=SERIES, linewidth=1)
    ax.set_title(title, loc="left")
    ax.set_xlabel("Packet number")
    ax.set_ylabel("Response time (ms)")
    ax.set_ylim(bottom=0)
    fig.tight_layout()
    fig.savefig(path, dpi=150)
    plt.close(fig)


def histogram_figure(counts, title, path):
    fig, ax = plt.subplots(figsize=(7, 3))
    ax.bar(counts.index, counts.values, color=SERIES, width=0.7)
    ax.set_title(title, loc="left")
    ax.set_xlabel("Retransmissions of a packet")
    ax.set_ylabel("Number of packets")
    ax.set_xticks(list(counts.index))
    ax.set_ylim(top=counts.max() * 1.12)
    ax.grid(axis="x", visible=False)
    for x, y in zip(counts.index, counts.values):
        ax.annotate(str(y), (x, y), ha="center", va="bottom", fontsize=8, color=MUTED, xytext=(0, 2),
                    textcoords="offset points")
    fig.tight_layout()
    fig.savefig(path, dpi=150)
    plt.close(fig)


os.makedirs(OUT, exist_ok=True)
for n in (1, 2):
    echo = pd.read_csv(os.path.join(HERE, "session-%d" % n, "echo.csv"))
    arq = pd.read_csv(os.path.join(HERE, "session-%d" % n, "arq.csv"))
    counts = arq["nack"].astype(int).value_counts().sort_index()
    print("session %d echo: %d packets, mean %.2f ms, min %d, max %d" % (
        n, len(echo), echo["response_time_ms"].mean(), echo["response_time_ms"].min(), echo["response_time_ms"].max()))
    print("session %d arq: %d packets, mean %.2f ms, min %d, max %d" % (
        n, len(arq), arq["response_time_ms"].mean(), arq["response_time_ms"].min(), arq["response_time_ms"].max()))
    print("session %d arq retransmissions per packet: %s; %d of %d (%.1f %%) needed none" % (
        n, {int(k): int(v) for k, v in counts.items()}, counts[0], len(arq), 100.0 * counts[0] / len(arq)))
    print("session %d arq mean BER by retransmissions: %s" % (
        n, {int(k): float(v) for k, v in arq.groupby(arq["nack"].astype(int))["ber"].first().items()}))
    line_figure(echo["response_time_ms"], "Session %d: echo response time, 1000 bps, 5 minutes" % n,
                os.path.join(OUT, "session-%d-echo-response-time.png" % n))
    line_figure(arq["response_time_ms"], "Session %d: ARQ response time per packet, 1000 bps, 5 minutes" % n,
                os.path.join(OUT, "session-%d-arq-response-time.png" % n))
    histogram_figure(counts, "Session %d: retransmissions per packet" % n,
                     os.path.join(OUT, "session-%d-arq-retransmissions.png" % n))
