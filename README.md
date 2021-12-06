
# OwlTorrent

OwlTorrent is a BitTorrent client primarily intended for research
and educational purposes. It was developed by a team of Rice 
Computer Science Undergraduates as a capstone project, as part of
the class Comp 413. 

OwlTorrent is still in development and does not have the full capabilities of a modern
BitTorrent client. 



## Build Locally

You will need to have Gradle and Java 1.11 installed.

Clone the project

```bash
  git clone https://github.com/maxyu1115/owltorrent
```

Go to the project directory

```bash
  cd owltorrent
```

Do a build and run tests

```bash
  ./gradlew build
```





## Usage/Examples

OwlTorrent is a command line application:
```
Usage: owltorrent [-hV] [-s=<seedingFileName>] <torrentFileName>
Downloads a torrent file through BitTorrent.
      <torrentFileName>   Path to the torrent file to download.
  -h, --help              Show this help message and exit.
  -s, --seed=<seedingFileName>
                          Path to the file to seed.
  -V, --version           Print version information and exit.

```

Building with gradlew will put the exe in owltorrent-core/build/libs, e.g. 
you can download a file with the following command:
```bash
java -jar owltorrent-core/build/libs/owltorrent-core-1.0-SNAPSHOT.jar <.torrent file>
```
## Features

- Downloading and seeding of files over the BitTorrent network
- Fully Java implementation of the BitTorrent protocol
- Testing and benchmarking harness for download strategy comparison


