# Electron+Java Demo
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fjreznot%2Felectron-java-app&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=PAGE+VIEWS&edge_flat=false)](https://hits.seeyoufarm.com)

Java Desktop Application with HTML 5 UI based on Electron and Vaadin.

## Uses

1. Node JS
2. Electron
3. Gradle
4. JDK 11
5. Jetty HTTP Server
6. Vaadin 14

## Features

0. Easy building with Gradle
1. Jetty server with Web Sockets enabled
2. Vaadin UI code in plain Java
3. Bi-directional WebSocket connection with Vaadin Push and Jetty WebSocket module
4. Two way communication between Electron and web application using javascript functions
5. Auto start / stop of server side on application init / exit
6. Custom window header
7. Menu option to show developer tools only when running in debug mode

Want to know how to implement all the features? See complete tutorial: https://github.com/cuba-labs/java-electron-tutorial !

## Try it!

### Preparations

Run debug version:

    }> gradlew runApp

### Building standalone app
        
    }> gradlew bundleApp
    
Application will be bundled to `build/bundle`

### Run in production mode

    }> gradlew runApp -Pvaadin.productionMode
    
Or

    }> gradlew bundleApp -Pvaadin.productionMode


If you want to see a real world application that is built with this approach take a look at CUBA Studio https://www.cuba-platform.com/discuss/t/cuba-studio-se-a-desktop-application-based-on-electron/2914

## If you want to support the project

<a href="https://www.buymeacoffee.com/jreznot" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>

## Screenshot

![Demo Image](./docs/app-window.png?raw=true "Application Window")
