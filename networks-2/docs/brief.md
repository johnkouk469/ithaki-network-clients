# Computer Networks II: Java socket programming

This is the assignment text issued by the course staff (Department of Electrical and Computer Engineering, Aristotle University of Thessaloniki), "Computer Networks II", experimental virtual lab. It is a shortened English translation of the Greek handout: the parts about installing Java, the Java audio and socket tutorials, the template applications and the submission rules are left out, and so are the questions the handout puts to the student. Only the translation is new.

## Purpose

The assignment is a network programming exercise. It aims to develop an experimental network application in Java, to become familiar with the UDP (user datagram protocol) and TCP (transmission control protocol) protocols, to introduce the mechanisms of real-time digital audio transmission over packet-switched networks (audio packet transmission) and to collect statistical measurements of parameters that, among others, determine the quality of communication between computers on the Internet. Some measurements have to be made against the Ithaki server of the experimental virtual lab that the courses Computer Networks I and II share.

## Background

The work is about sending and receiving single packets of information, known as datagrams (UDP packets), with the Java libraries `java.net` and `java.io`.

- `DatagramSocket` creates sockets that send and receive UDP packets, without guaranteed delivery. A UDP packet carries the bytes of an array across the network in one send operation, on a best-effort basis. Checking that a packet arrived, and assembling a set of such packets, is the responsibility of the applications.
- `send()` sends a datagram; the packet holds the IP address and port number of the destination. `receive()` waits for a packet and stores it with the sender's address. It blocks until the next packet arrives. `setSoTimeout()` sets a maximum waiting time, after which `receive()` stops waiting and the program continues.
- `DatagramPacket` sets the parameters of a datagram; its `setAddress()` and `setPort()` methods give the destination's Internet address and port.
- `java.io` provides `File`, `FileInputStream` and `FileOutputStream`. `System.currentTimeMillis()` returns the system time in milliseconds and is used to time events such as the departure and arrival of packets, which the assignment relies on.

### Digital audio

The package `javax.sound.sampled` provides the objects for recording and playing digital audio. `AudioSystem` gives access to the audio devices and `AudioFormat` formats the audio. The assignment uses mono pulse code modulation (PCM) with 8000 samples per second and uniform signed linear quantisation of Q bits per sample. `AudioFormat(8000, Q, 1, true, false)` returns the format object; Q is 8 or 16 in the experiments. Playback gets an output from `AudioSystem.getSourceDataLine`, which returns a `SourceDataLine`; `open()` takes the audio format and the size of the internal audio buffer, `write()` takes a `byte[]` with the samples, an offset and a length, and `stop()` and `close()` pause and release the output. A Java `byte` is a signed integer from -128 to +127.

### Packet audio

Audio must play at a constant rate even though audio packets arrive at irregular intervals, so the receiver uses an elastic buffer, which can be a two-dimensional array with one audio packet per row. Packets arrive in it at random times and are taken from it at a constant rate towards the audio output. Because `receive()` and `write()` both block, the buffer can be managed by two cooperating threads, `rxThread`, which fills it, and `playThread`, which feeds the audio output. Playing cannot run ahead of filling. Real systems need more (cooperation between sender and receiver, or with the network), but the assignment uses an open-loop scheme without that cooperation. Since multi-threaded programming is not required, audio can be played in two successive stages: first all packets are received and stored in order in the buffer, then the buffer feeds the audio output at a constant rate. The buffer must hold up to 30 seconds of audio. A simple implementation can also be based on `available()` of the `SourceDataLine`.

## The application

The task is a Java application, `userApplication`, that (a) talks to the lab server through datagram connections (UDP socket programming) and (b) measures some parameters of that communication.

1. When the student chooses the assignment "Java socket programming" on the lab's web page, the server gives them a copy (a thread) of its application `serverThread`.
2. Up to 60 seconds pass between choosing the assignment and the start of `serverThread`. The total time the copy stays online is shown on the server's response page; for more time the assignment must be chosen again.
3. `serverThread` opens a UDP socket at the server address and at the port shown on the response page as the server listening port.
4. `userApplication` must be able to send many `clientRequest` datagrams one after the other. For each one the server answers with one or more `serverResponse` datagrams.
5. The `serverResponse` packets are sent to the client address and to the client listening port shown on the response page. If the connection to the lab goes through NAT, as on ordinary ADSL connections, the NAT must be set up as a virtual server, so that packets sent to the public address and port are forwarded to the private address and port on which `userApplication` runs. The internal firewall must allow UDP packets in both directions.
6. **Echo.** `clientRequest` packets carry the code `echo_request_code` shown on the response page as their payload. The server answers with a packet containing its date and time, `PSTART DD-MM-YYYY HH:MM:SS PSTOP`. After each request and before sending the response, the server waits for a time, in milliseconds, drawn at random from a probability distribution whose form and parameters it chooses anew at the start of every session. The delay is cancelled for the next response only, after a request that carries the code `E0000`. When the code is followed by `TXX`, the packets returned carry temperatures collected in near real time along the eastern part of the Egnatia Motorway, from points close to the road surface: `PSTART DD-MM-YYYY HH:MM:SS TXX DD-MM HH:MM +ZZ C PSTOP`, where `XX` is the code of the telemetry station as shown on the map of the lab's "Egnatia Motorway temperatures" option, `+ZZ` is the signed temperature and `DD-MM HH:MM` is the date and time at which the Lightnet Digital Radio Telemetry Network server recorded the temperature.
7. **Images.** `image_request_code` returns the current frame of the lab's video coder, which shows the traffic in front of the School of Engineering. The frame is sent in successive UDP packets. All but the last have the same length; the number of packets and the length of the last one depend on the frame size in bytes. To keep the packet rate low enough for an ADSL router's firewall, the server sends them slowly. A JPEG file starts with the bytes `0xFF 0xD8` and ends with `0xFF 0xD9`. The code can be followed by:
   - `CAM=FIX` or `CAM=PTZ`, which selects the camera: the fixed one (the default) gives 640x480 pixels at a fixed angle, the other gives 320x240 pixels and an angle that changes at random times while the camera is under pan-tilt-zoom control. `CAM=PTZ` can be followed by `DIR=X` with `X` equal to `L`, `U`, `R` or `D` (left, up, right, down), `M` to memorise the angle or `C` to return to the memorised one; the camera takes a few seconds to react;
   - `FLOW=ON`, which turns on a simple flow control: for every image packet received, the application sends a UDP packet containing `NEXT`, and the server waits for it before sending the next one. If the server gets no `NEXT` within 1400 ms it repeats the packet, at most twice per packet; after more than two repetitions it abandons the image and waits for the next request;
   - `UDP=L`, the length in bytes of the info part of the UDP packets that carry the image: 128, 256, 512 or 1024 (128 if absent).
8. **Audio.** `sound_request_code` makes the server send a sequence of audio datagrams. The parameter `YXXX` after the code selects the audio source `Y` and the number `XXX` of audio packets. `Y=T` activates a virtual frequency generator in the range 200 to 4000 Hz; `Y=F` plays part of a clip chosen at random from the server's experimental music repertoire. The parameter `LZZ` before `FXXX` makes the server choose clip number `ZZ` deterministically, which helps while developing.
   - **DPCM.** The packets are encoded with differential pulse code modulation. In the simple, non-adaptive form, the difference of two successive samples is quantised with a uniform non-adaptive quantiser and coded with 4 bits, from -8 to +7. Two differences are placed in one byte, after +8 has been added to each, so that each nibble is an unsigned number from 0 to 15. A packet is 128 bytes of difference pairs, which is 256 audio samples; 32 such packets played at 8000 samples per second are about one second of audio, so the largest request is about 30 seconds. Each signed difference can be multiplied on playback by a factor beta greater than 1, which is related to the quantiser step. The receiver assumes mean 0 and beta equal to 1 (or greater than 1). The number of bits Q used on playback is 8 or 16.
   - **AQ-DPCM.** The server also has an adaptive quantiser, adaptive quantiser DPCM. Every 256 samples it chooses the step of an optimal 4-bit uniform quantiser, assuming a Gaussian distribution of the differences (the step comes from the Max-Lloyd algorithm for optimal scalar quantisers). The mean and the quantiser step of the 256 samples are coded linearly with 16 bits each and placed in 4 bytes (a header) in front of the 128 bytes of differences, so one UDP packet is 132 bytes. The least significant byte comes first (little-endian). AQ-DPCM is requested by sending `AQ` in the same request packet, right after the code and before `YXXX`. On playback Q must be 16 to keep the 16-bit precision. AQ-DPCM applies only to the music repertoire.

## TCP and HTTP

TCP defines a socket by the IP address and the port number of the remote computer. `java.net.Socket` creates and manages TCP sockets. The address comes from `InetAddress.getByAddress(addressArray)`, where `addressArray` is a `byte[]` in dot-decimal form such as `{ (byte)155, (byte)207, 18, (byte)208 }`, and the port is an integer. The input and output streams of a socket `s` are `s.getInputStream()` and `s.getOutputStream()`.

A browser and a web server cooperate over HTTP, a character-based protocol that uses TCP. A small program that opens a TCP socket to port 80 and sends `GET /index.html HTTP/1.0\r\n\r\n` gets back the HTTP headers, a blank line made of `\r\n` delimiters, and the HTML of the page. The handout shows the response of the lab server, which starts `HTTP/1.1 200 OK` and contains a short HTML page saying "Hello there !".

The optional experiments with TCP can include prefixes from two or three public servers freely chosen on the Internet.

### The Ithakicopter

The experiments with TCP can also record telemetry about the position and the motor power of the flying micro-platform Ithakicopter, which is shown at the lab's `ithakicopter.html` page. The output stream of a TCP socket to port 38048 on the Ithaki server, after an initial preamble message, carries telemetry of the form

`ITHAKICOPTER LMOTOR=LLL RMOTOR=RRR ALTITUDE=AAA ~ TEMPERATURE=+TT.TT PRESSURE=PPPP.PPTELEMETRY \r\n`

where `LLL` and `RRR` are 3-digit decimal numbers giving, with 8-bit precision, the duty cycle of the PWM waveforms that control the two micro-motors, `AAA` is the altitude in pixels above the Ground Level reference of the lab's live video, and `TT.TT` and `PPPP.PP` are the temperature and the atmospheric pressure around the platform. The telemetry is a response to a request that must come first. An AUTOPILOT remote-control application sends, at suitable intervals, requests of the form

`AUTO FLIGHTLEVEL=FFF LMOTOR=LLL RMOTOR=RRR PILOT \r\n`

through the same socket, where `FFF` is the desired flight level (used only for display on the video) and `LLL` and `RRR` are the desired motor values, which the system uses to steer the platform. Repeating the request and response, the application can control the platform for up to 180 seconds, the longest single control session, which allows a complete networked control loop.

A simple TELEMETRY application only receives the values of `LMOTOR`, `RMOTOR` and `ALTITUDE` in the same form, as UDP packets on the port 48038 (listening port), at the same time as the `ithakicopter.html` application runs, provided the two applications share the same network address.

### Vehicle diagnostics

The experiments can also collect data about the operation of a vehicle through OBD-II (on-board diagnostics II), under the standard SAE J1979. The standard sends request codes to an electronic control unit and receives response codes that describe the state of a vehicle subsystem; the units communicate over a controller area network (CAN, ISO 15765-4). The server holds recordings of real OBD-II data covering more than an hour of a vehicle at rest or in motion, available over a TCP socket at port 29078. A request has two parameters, each a 2-digit hexadecimal number: MODE and PID (parameter ID), separated by a space, with the character CR (ASCII 13) at the end as a delimiter. The response repeats MODE and PID, with the most significant nibble of MODE replaced by 0x4, followed by one hexadecimal value `XX` or two values `XX` and `YY`, separated by spaces and ending with CR. Alternatively UDP can be used, by sending the OBD-II request code followed by `OBD=MODE PID`; the CR is not needed then.

| Mode | PID | Description | 1st byte | 2nd byte | Units | Formula |
|---|---|---|---|---|---|---|
| 01 | 1F | Engine run time | XX | YY | s | 256*XX+YY |
| 01 | 0F | Intake air temperature | XX | | degrees C | XX-40 |
| 01 | 11 | Throttle position | XX | | % | XX*100/255 |
| 01 | 0C | Engine RPM | XX | YY | RPM | ((XX*256)+YY)/4 |
| 01 | 0D | Vehicle speed | XX | | km/h | XX |
| 01 | 05 | Coolant temperature | XX | | degrees C | XX-40 |

## Measurements and presentation of results

- **A.** Results from at least two sessions at least 48 hours apart.
- **B.** From each session:
  - (i) a graph G1 of the response time, in milliseconds, of every echo packet sent over at least 4 minutes;
  - (ii) a graph G2 of the throughput of the system over at least 4 minutes, computed with a moving average every second over the most recent 8 or 16 or 32 seconds (as examples);
  - (iii) graphs G3 and G4, the same as (i) and (ii) with the server's delay switched off;
  - (iv) four histograms G5, G6, G7 and G8 of the frequency, or approximately the probability, of the values recorded in the measurements (i) to (iii);
  - (v) a graph R1 that treats the measurements (i) as instantaneous RTT (round trip time) values and shows, superimposed, the SRTT (smooth round trip time), the RTT variance and the RTO (retransmission timeout) with weights alpha, beta and gamma of your choice, as your own implementation of TCP on your own terminal would determine them.
- **C.** From the measurements of B, or from others, an estimate of (i) the type of distribution of the delay the server inserted between the packets it sent, (ii) its mean and its variance as recorded in the application and (iii) the frequencies and the titles of the audio clips that come from the virtual frequency generator and the experimental music repertoire.
- **D.** Also: (a) two images E1 and E2 from the cameras CAM1 and CAM2; (b) eight temperatures T1 to T8 from different Egnatia Motorway telemetry stations; (c) two graphs G9 and G10 of segments of the waveforms that come from the virtual frequency generator and from the music repertoire; (d) four graphs G11, G12, G13 and G14 of the distributions of the differences, and of the values of the samples themselves, of the audio waveforms played when decoding DPCM and AQ-DPCM signals (the distributions found approach a Gauss distribution, a Laplace distribution or another one); (e) four graphs G15, G16, G17 and G18 of segments of the sequences of the mean value and of the step of the adaptive quantiser, received during AQ-DPCM reception of two audio clips from the music repertoire; (f) two graphs G19 and G20 of the telemetry received while the Ithakicopter moves, through the UDP telemetry mechanism, for two desired flight levels; (g) five separate graphs, or one graph with the individual curves superimposed, for the parameters of the OBD-II table, for four (any consecutive) minutes of operation of the vehicle.
- **E.** The results come with: (a) short comments or observations; (b) a short bibliographic technical reference to the UDP protocol; (c) a short bibliographic reference to international audio streaming standards; (d) a screenshot of the IP, DNS, DHCP, NAT and port settings of the ADSL device that may have been used to communicate with the lab server.
