const {app, BrowserWindow} = require('electron');

// electron.crashReporter.start();

var mainWindow = null;
var jvmProcess = null;

app.on('window-all-closed', function () {
    app.quit();
});

app.on('ready', function () {
    jvmProcess = require('child_process')
        .spawn('cmd.exe', ['/c', 'electron-vaadin.bat'], {cwd: './build/install/electron-vaadin/bin'});

    console.log("Server PID: " + jvmProcess.pid);

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
    };

    var startUp = function () {
        requestPromise(appUrl)
            .then(function (htmlString) {
                console.log('Server started!');

                if (jvmProcess) {
                    require('ps-tree')(jvmProcess.pid, function (err, children) {
                        console.log("Server spawned processes: ");

                        for (let child of children) {
                            console.log(" + Child " + child.PID);
                        }
                    });
                }

                openWindow();
            })
            .catch(function (err) {
                console.log('Waiting for the server start...');
                startUp();
            });
    };

    startUp();
});