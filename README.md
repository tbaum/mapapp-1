MapApp
======

This is a small view and Export-Helper to extract videos taken by action-camaras (like Garmin VIRB) and geo-tag them.
At this point it is only developed for OS.X, feel free to contribute for other OSs.

It will parse the Garmin-Virb database (expected in `$HOME/Library/Application Support/Garmin/VIRB Edit/Database`).

Please update the Video-Store Directories in `src/main/java/mapapp/Main.java`  matching your needs. 
I store them in `/Volumes/VIBR/Garmin VIRB`


If you can't see map-tiles, it possible the api-key has expired or is exhausted.
Please go to https://geodienste.lyrk.de to create your own.


Requirements
------------

- JDK 1.8
- maven
- opencv
- ffmpeg


Install on OS.X
---------------

    brew install homebrew/science/opencv
    brew install ffmpeg

create a symlink to fixing a version-issue in ffmpeg/opencv

    sudo ln -s libavfilter.4.dylib /usr/local/lib/libavfilter.3.dylib


Run
---

    mvn clean compile exec:java

