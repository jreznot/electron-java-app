# Electron+Java Demo

Java Desktop Application with HTML 5 UI based on Electron and Vaadin

## Uses

1. Node JS
2. Electron
3. Gradle
4. JDK 8
5. Jetty HTTP Server
6. Vaadin Framework

## Features

1. Jetty server with Web Sockets enabled
2. Vaadin UI code in plain Java
3. Two way communication between Electron and web application using javascript functions
4. Auto start / stop of server side on application init / exit

## Try it!

__Note:__ These steps now are relevant to Windows only.

1. Download and install `npm` from https://nodejs.org/en/download/
2. Install required `npm` modules:

        > cd electron-src
        > npm install

3. Build java application:

        > gradlew installDist
        
4. Run debug version:

        > electron-app-debug.bat
        
5. Install electron-packager:

        > npm install electron-packager -g
        
6. Build standalone app:

        > electron-app-package.bat

7. Application will be bundled to `electron-src\electron-vaadin-win32-x64`

![Demo Image](/docs/app-window.png?raw=true "Application Window")