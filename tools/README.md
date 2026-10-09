# Stand-in servers

The Ithaki lab server of the course is not reachable any more, so the two programs cannot be run against it. These two stand-ins were written in October 2026, to check that the published code builds and runs end to end. The programs, reports and results of the repository date from 2020 and 2021; the stand-ins did not exist then.

They follow the assignment texts in `networks-1/docs/brief.md` and `networks-2/docs/brief.md` and a packet capture of the lab. They exercise the code paths of the programs and give made-up data (the audio is silence). They are not an implementation of the lab, and a run against them says nothing about the lab's real behaviour.

| Folder | Stands in for | Used by |
|---|---|---|
| `mock-modem/` | the course library `ithakimodem` (class `ithakimodem.Modem`) | `networks-1` |
| `mock-lab/` | the lab's UDP server | `networks-2` |

## Networks I with the mock modem

Needs Java 8 or later and Maven. From the repository root:

```
mkdir -p tools/mock-modem/classes
javac -d tools/mock-modem/classes tools/mock-modem/ithakimodem/Modem.java
jar cf tools/mock-modem/ithakimodem-mock.jar -C tools/mock-modem/classes .
mkdir -p networks-1/lib
cp tools/mock-modem/ithakimodem-mock.jar networks-1/lib/ithakimodem.jar     # stands in for the course jar at build time
cd networks-1
cp ithaki.properties.example ithaki.properties
sed -i 's/XXXX/1111/; s/^# output_dir=out/output_dir=out/' ithaki.properties
mvn package dependency:copy-dependencies
java -cp "target/classes:target/dependency/*:lib/ithakimodem.jar" userApplication ithaki.properties
```

On Windows use `;` instead of `:` in the class path. The run takes about 10 minutes (two five-minute measurements) and writes two images, a GPS map image and two Excel files to `networks-1/out/`. Start Java with `-Dmock.dead=true` to see what the program does when the modem cannot be opened, and with `-Dmock.delayms=10` for faster answers (the number of packets grows, and the run still lasts ten minutes).

Remove the stand-in jar from `networks-1/lib/` before using the real course library.

## Networks II with the mock lab

Needs Java 8 or later, Maven and Python 3. In one terminal:

```
python tools/mock-lab/mock_lab.py
```

In another, from the repository root:

```
cd networks-2
cp ithaki.properties.example ithaki.properties
sed -i 's/XXXX/1111/; s/^# output_dir=out/output_dir=out/; s/^# server_ip=.*/server_ip=127.0.0.1/; s/^# copter_server_port=.*/copter_server_port=48079/' ithaki.properties
mvn package dependency:copy-dependencies
java -cp "target/classes:target/dependency/*" userApplication ithaki.properties
```

Use `;` in the class path on Windows. The run takes about 18 minutes, plays a minute of silence and writes two images, the temperature file and the workbook `session.xlsx` to `networks-2/out/`. The copter ports differ (48078 on the client, 48079 on the mock) because the client and the mock share one machine.
