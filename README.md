# Ithaki network clients

Two Java programs that talk to the Ithaki virtual lab of the Aristotle University of Thessaloniki: a client for the lab's virtual serial modem (Computer Networks I) and a UDP client (Computer Networks II). They request echo packets, camera images, GPS traces, audio streams, helicopter telemetry and vehicle diagnostics, time the responses and export the measurements to Excel.

![Number of retransmissions per packet in the ARQ experiment of session 1: 337 packets needed none, 61 needed one, 12 needed two, 2 needed three and 2 needed four.](networks-1/docs/figures/session-1-arq-retransmissions.png)

## Context

Two individual assignments at the Aristotle University of Thessaloniki, School of Electrical and Computer Engineering: Computer Networks I (spring 2020) and Computer Networks II (winter 2020-21). The course provided the assignment texts, the lab server and the virtual modem library; the programs and the reports are mine. The Networks II program reuses parts of the Networks I code.

| Folder | What it is |
|---|---|
| [`networks-1`](networks-1) | Serial modem client ([report](networks-1/docs/report.md), [brief](networks-1/docs/brief.md)) |
| [`networks-2`](networks-2) | UDP socket client ([report](networks-2/docs/report.md), [brief](networks-2/docs/brief.md)) |
| [`tools`](tools) | Stand-in servers for running the programs without the lab |

## What dates from when

The programs, the reports and the recorded results are from 2020 and 2021. The `.java` files were not kept as files; the code survives as the listings in the submitted report documents, and the sources here were restored from those listings. Added in October 2026, to make the repository usable and to check that what is published is correct: the stand-in servers in [`tools`](tools) (with them the code was built and run end to end), the settings file for request codes and other options, the corrections the checks turned up, the CSV versions of the Excel results, the figures of `networks-1`, the English translations of the reports and briefs, and `make_figures.py`.

## What the programs do

**Networks I** (`networks-1/src`). Through the course's `ithakimodem.Modem` class it:

- requests a camera image without and with transmission errors and saves them as JPEG files;
- requests a GPS trace, takes every tenth sentence and uses the positions to request a map image;
- runs an ARQ exchange for five minutes at 1000 bps: it checks each packet's frame check sequence (XOR of its 16 characters), answers ACK or NACK, and computes the bit error rate from the number of retransmissions;
- requests echo packets for five minutes at 1000 bps;
- exports the ARQ and echo measurements to Excel.

**Networks II** (`networks-2/src`). Over UDP datagram sockets it:

- measures echo packets with and without the server's artificial delay (the code `E0000` cancels it) and reads a temperature packet;
- receives camera images from the fixed and the PTZ camera;
- receives audio as DPCM and as adaptive-quantiser AQ-DPCM packets, stores them in an elastic buffer and plays them with `javax.sound`;
- receives Ithakicopter telemetry and OBD-II vehicle data;
- writes everything to one Excel workbook, one sheet per measurement.

## Getting started

You need Java 8 or later and Maven. The two programs have separate `pom.xml` files and read the request codes of a lab session from a properties file.

1. Copy `ithaki.properties.example` to `ithaki.properties` in the project folder and fill in the codes and ports your session shows. The file is listed in `.gitignore`. The optional lines at the end of the example set the folder for the output files (`output_dir`), the name of the Excel workbook (`workbook_file`, Networks II) and, for Networks II, the server address and the copter ports.
2. Networks I also needs the course library `ithakimodem.jar`, which is not included here: get it from the course page of the lab and put it in `networks-1/lib/`.
3. Build and run:

```
cd networks-1          # or networks-2
mvn package dependency:copy-dependencies
java -cp "target/classes:target/dependency/*:lib/ithakimodem.jar" userApplication ithaki.properties
```

On Windows use `;` instead of `:` in the class path. `networks-2` has no `lib/` folder, so leave out `lib/ithakimodem.jar` there. Images and Excel files go to `output_dir`, or to the working folder if it is not set; `out/` is ignored by git.

A Networks I run takes about 10 minutes (two five-minute measurements), a Networks II run about 18 minutes (three minutes of copter telemetry, four of OBD-II, ten of echo, about a minute of audio). The lab server is run by the university. [`tools`](tools) explains how to run both programs against stand-in servers instead.

## Results

The measurements of the two Networks I sessions (20 and 22 May 2020) are in [`networks-1/results`](networks-1/results) as CSV files, with the camera and map images of each session; `make_figures.py` there recomputes the numbers below and redraws the figures. In the images, a black bar hides the session's request code, which the lab prints in every picture.

| | Session 1 | Session 2 |
|---|---|---|
| Echo packets in 5 minutes | 772 | 714 |
| Mean echo response time | 388.75 ms | 420.40 ms |
| ARQ packets in 5 minutes | 414 | 376 |
| Mean ARQ response time | 725.04 ms | 798.84 ms |
| ARQ packets that needed no retransmission | 337 (81.4 %) | 290 (77.1 %) |

For Networks II, two sessions on 14 and 17 January 2021 gave the graphs, camera images and audio histograms in the [report](networks-2/docs/report.md). The raw data behind them are not included.

## Project structure

```
networks-1/src/       userApplication.java  main program: modem requests, ARQ, BER, Excel export
                      echoPacket.java, GPSsentence.java, Config.java
networks-1/docs/      report.md, brief.md, figures/
networks-1/results/   session-1, session-2 (echo.csv, arq.csv, images), make_figures.py
networks-2/src/       userApplication.java  main program: UDP requests, audio decoding, Excel export
                      echoPacket.java, ithakicopterPacket.java, vehiclePacket.java, Config.java
networks-2/docs/      report.md, brief.md, figures/
networks-2/results/   session-1, session-2 (camera images)
tools/                mock-modem/, mock-lab/, README.md
```

## Limitations

- The lab server was not reachable when this repository was prepared (October 2026). The programs were run against the real server in May 2020 and January 2021, and the recorded results come from those runs. In their present form they have been built, and run end to end only against the stand-in servers in `tools`.
- The audio decoding has been run only on the silence that the stand-in server sends; sound output and the decoding of real packets are untested in this form.
- Both programs run their whole measurement sequence in one go, with no command-line options other than the path of the properties file.
- The copter section uses UDP port 48078 on both sides, the port of the January 2021 sessions. The assignment text in the archive gives the telemetry port as 48038. `copter_client_port` and `copter_server_port` change it.

## Credits and third-party material

- `ithakimodem.Modem`, the virtual modem class of the course, is not included and not covered by the licence below. The way the Networks I program uses it follows the course's seed code. The socket and audio calls of the Networks II program follow the code snippets in the course handout.
- `ithakicopter.jar`, the course's Ithakicopter application, is not included.
- Apache POI (Apache License 2.0) writes the Excel files; Maven downloads it.
- The assignment briefs in `networks-1/docs/brief.md` and `networks-2/docs/brief.md` come from the course and are shortened translations from Greek. They are not covered by the licence below.
- The camera images come from the lab's cameras. The map images in `networks-1/results` are Google Maps satellite imagery returned by the lab server (credits in the pictures: Google, CNES / Astrium, Spot Image, DigitalGlobe); they are not covered by the licence below.

## Licence

MIT for the code and the reports; see [LICENSE](LICENSE).
