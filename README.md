# Electron+Java Demo

Java Desktop Application with HTML 5 UI based on Electron and Vaadin.

## Uses

1. Node JS
2. Electron
3. Gradle
4. JDK 8
5. Jetty HTTP Server
6. Vaadin Framework

## Features

0. Easy building with Gradle
1. Jetty server with Web Sockets enabled
2. Vaadin UI code in plain Java
3. Bi-directional WebSocket connection with Vaadin Push and Jetty WebSocket module
4. Two way communication between Electron and web application using javascript functions
5. Auto start / stop of server side on application init / exit
6. Custom window header

Want to know how to implement all the features? See complete tutorial: https://github.com/cuba-labs/java-electron-tutorial !

## Try it!

### Preparations

Build java application:

    > gradlew build

Run debug version:

    > gradlew runApp

### Building standalone app
        
    > gradlew bundleApp

Application will be bundled to `build/bundle`

If you want to see a real world application that is built with this approach take a look at CUBA Studio https://www.cuba-platform.com/discuss/t/cuba-studio-se-a-desktop-application-based-on-electron/2914

## Screenshot

![Demo Image](/docs/app-window.png?raw=true "Application Window")
