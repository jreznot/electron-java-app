const {app, BrowserWindow} = require('electron');

// electron.crashReporter.start();

var mainWindow = null;
var serverProcess = null;

app.on('window-all-closed', function () {
    app.quit();
});

app.on('ready', function () {
    serverProcess = require('child_process')
        .spawn('cmd.exe', ['/c', 'electron-vaadin.bat'],
            {
                cwd: './build/install/electron-vaadin/bin'
            });

    serverProcess.stdout.on('data', function (data) {
        console.log('Server: ' + data);
    });

    console.log("Server PID: " + serverProcess.pid);

    var requestPromise = require('request-promise');
    let appUrl = 'http://localhost:8080';

    var openWindow = function () {
        mainWindow = new BrowserWindow({width: 1024, height: 768, title: 'TODO List - Electron Vaadin application'});
        mainWindow.setMenu(null);
        mainWindow.loadURL(appUrl);

        // mainWindow.webContents.openDevTools();

        mainWindow.on('closed', function () {
            mainWindow = null;
        });

        mainWindow.on('close', function (e) {
            if (serverProcess) {
                e.preventDefault();

                console.log('Kill server process');

                var kill = require('tree-kill');
                kill(serverProcess.pid, 'SIGTERM', function(err) {
                    console.log('Server process killed');

                    serverProcess = null;
                    mainWindow.close();
                });
            }
        });
    };

    var startUp = function () {
        requestPromise(appUrl)
            .then(function (htmlString) {
                console.log('Server started!');
                openWindow();
            })
            .catch(function (err) {
                console.log('Waiting for the server start...');
                startUp();
            });
    };

    startUp();
});