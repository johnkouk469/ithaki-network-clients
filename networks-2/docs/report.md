# Networks II: UDP client for the Ithaki virtual lab

Comments, setup notes and results from two sessions with the lab server, on 14 and 17 January 2021. The task is in [brief.md](brief.md). The program is in [`../src`](../src), the recorded images in [`../results`](../results).

## The program

The program collects all the data that can be requested from the Ithaki server and writes them to an Excel file. The file holds every packet received, plus the further data the program computed during the communication, and it was used to make the graphs below.

It consists of the classes `userApplication`, `echoPacket`, `ithakicopterPacket` and `vehiclePacket`. The last three create objects that hold, in an easily accessible form, the information of the packets received with the echo, Ithakicopter and OBD-II request codes. `userApplication` has the `main()` function and the functions `echo()`, `temperature()`, `image()`, `audio()`, `ithakiCopter()` and `vehicle()`. Each of them has its own `DatagramSocket`, sends its request code, and receives, processes and stores the packets that come back. `userApplication` also has five `exportExcel()` functions; each one, when it runs, creates a new sheet in the global workbook with all the packets, and the information about them, that it was given as input.

In one run `main()` requests, in this order: a temperature packet; Ithakicopter telemetry twice, for 90 seconds each; the image of the fixed camera and of the PTZ camera; five audio clips of 300 packets each (the frequency generator, `T300`; a random music clip, `F300`; music clip 01 with DPCM, `L01F300`; and music clips 01 and 02 with AQ-DPCM, `AQL01F300` and `AQL02F300`); the OBD-II vehicle data for 240 seconds; and echo packets for five minutes, first with the server's delay and then without it, using the code `E0000`. The audio functions decode the DPCM and AQ-DPCM packets, store them in an elastic buffer (a two-dimensional array) and then play them through `javax.sound`: the two-stage form the brief allows, which receives all the packets first and plays them afterwards.

## The UDP protocol

The User Datagram Protocol (UDP) is one of the core protocols of the Internet. Programs use it to send short messages, known as datagrams, from one computer to another in a computer network. One of its main characteristics is that it does not guarantee reliable communication: UDP packets sent from one computer can reach the receiver in the wrong order, duplicated, or not at all if the network is heavily loaded. TCP, by contrast, has all the mechanisms needed to check and enforce reliability, and can therefore guarantee reliable communication between computers. The lack of these mechanisms makes UDP considerably faster and more efficient, at least for applications that do not need reliable communication.

Audio and video streaming applications use UDP extensively. For them it is important that packets reach the receiver within a short time, so that the sound or the picture is not interrupted. UDP is therefore preferred because it is fast, even though some UDP packets may be lost. When a packet is lost, these applications have correction and interpolation mechanisms, so that the end user notices no alteration or break in the sound or picture. Unlike TCP, UDP supports broadcasting, sending one packet to all computers of a network, and multicasting, sending one packet to some specific computers of a network. The latter is used very often in streaming applications so that one stream of sound or picture reaches many subscribers at the same time.

Some important applications that use UDP are the Domain Name System (DNS), IPTV, Voice over IP (VoIP), the Trivial File Transfer Protocol (TFTP) and games played live over the Internet.

## Network setup

The server sends its responses to the public address and the client port of the machine that runs the program, so that machine has to be reachable from the Internet. In this setup the modem/router of the Internet provider had a second router connected to it. Port forwarding had to be set up twice: from the provider's router to the second router, and then from the second router to the computer that ran the program. The firewall setting of the router also had to be changed from High to Middle before packets from Ithaki were received. Wireshark was used to check which packets reached the computer.

## Results

The graphs are the ones the Excel workbook produced. The raw data behind them are not included. The titles and axes are as Excel drew them; the captions say what each graph shows, after the brief.

### Echo packets, with and without the server's delay

Each measurement lasted five minutes. In session 1 the throughput of the echo packets with delay was computed with a moving average over 32 seconds; in session 2, over 16 seconds. The throughput without delay used 8 seconds in both sessions. In R1 the measurements of G1 are treated as RTT values, and SRTT, the RTT variance and RTO are drawn with alpha = 0.8, beta = 0.75 and gamma = 4.

| | Session 1 | Session 2 |
|---|---|---|
| G1: response time of echo packets with delay | ![](figures/session-1-g1.png) | ![](figures/session-2-g1.png) |
| G2: throughput with delay, moving average | ![](figures/session-1-g2.png) | ![](figures/session-2-g2.png) |
| G3: response time without delay (code `E0000`) | ![](figures/session-1-g3.png) | ![](figures/session-2-g3.png) |
| G4: throughput without delay, moving average | ![](figures/session-1-g4.png) | ![](figures/session-2-g4.png) |
| R1: RTT, SRTT, RTT variance and RTO | ![](figures/session-1-r1.png) | ![](figures/session-2-r1.png) |

The histograms G5 to G8 show the frequency of the values measured in G1 to G4 respectively: G5 the response time with delay, G6 the throughput with delay, G7 the response time without delay and G8 the throughput without delay.

| | Session 1 | Session 2 |
|---|---|---|
| G5 | ![](figures/session-1-g5.png) | ![](figures/session-2-g5.png) |
| G6 | ![](figures/session-1-g6.png) | ![](figures/session-2-g6.png) |
| G7 | ![](figures/session-1-g7.png) | ![](figures/session-2-g7.png) |
| G8 | ![](figures/session-1-g8.png) | ![](figures/session-2-g8.png) |

### Temperature

A temperature packet, requested by adding `T00` to the echo code, in session 2: `PSTART 17-01-2021 01:21:20 T00 17-01 00:00 +19 C PSTOP`.

### Images

The fixed camera and the PTZ camera, in each session.

| | Fixed camera | PTZ camera |
|---|---|---|
| Session 1 (14 January 2021) | ![](../results/session-1/camera-fixed.jpg) | ![](../results/session-1/camera-ptz.jpg) |
| Session 2 (17 January 2021) | ![](../results/session-2/camera-fixed.jpg) | ![](../results/session-2/camera-ptz.jpg) |

### Audio

Each audio request asked for 300 packets. G9 is a segment of the waveform from the virtual frequency generator (request `T300`) and G10 from the music repertoire (`F300`). G11 and G12 are histograms of the differences and of the sample values of the DPCM clip `L01F300`, and G13 and G14 the same for the AQ-DPCM clip `AQL01F300`. G15 and G16 are histograms of the mean and of the quantiser step received with the packets of the AQ-DPCM clip `L01`, and G17 and G18 those received with the clip `L02`.

G11 is drawn with Python from the recorded sample values: the differences are recovered from consecutive samples. The other graphs of this section are the Excel charts.

| | Session 1 | Session 2 |
|---|---|---|
| G9 | ![](figures/session-1-g9.png) | ![](figures/session-2-g9.png) |
| G10 | ![](figures/session-1-g10.png) | ![](figures/session-2-g10.png) |
| G11 | ![](figures/session-1-g11.png) | ![](figures/session-2-g11.png) |
| G12 | ![](figures/session-1-g12.png) | ![](figures/session-2-g12.png) |
| G13 | ![](figures/session-1-g13.png) | ![](figures/session-2-g13.png) |
| G14 | ![](figures/session-1-g14.png) | ![](figures/session-2-g14.png) |
| G15 | ![](figures/session-1-g15.png) | ![](figures/session-2-g15.png) |
| G16 | ![](figures/session-1-g16.png) | ![](figures/session-2-g16.png) |
| G17 | ![](figures/session-1-g17.png) | ![](figures/session-2-g17.png) |
| G18 | ![](figures/session-1-g18.png) | ![](figures/session-2-g18.png) |

### Ithakicopter telemetry

Telemetry received over UDP while the Ithakicopter moved, for two desired flight levels: the motor values `lmotor` and `rmotor`, the altitude, the temperature and the pressure.

| | Session 1 | Session 2 |
|---|---|---|
| G19: first flight level | ![](figures/session-1-g19.png) | ![](figures/session-2-g19.png) |
| G20: second flight level | ![](figures/session-1-g20.png) | ![](figures/session-2-g20.png) |
