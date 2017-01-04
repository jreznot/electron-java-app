const {app, BrowserWindow} = require('electron');

// electron.crashReporter.start();

var mainWindow = null;

app.on('window-all-closed', function () {
    app.quit();
});

app.on('ready', function () {
    var jvmProcess = require('child_process')
        .spawn('cmd.exe', ['/c', 'electron-vaadin.bat'], {cwd: './build/install/electron-vaadin/bin'});
    var requestPromise = require('request-promise');

    var openWindow = function () {
        mainWindow = new BrowserWindow({width: 800, height: 600});
        mainWindow.loadURL('http://localhost:8080');

        // mainWindow.webContents.openDevTools();

        mainWindow.on('closed', function () {
            mainWindow = null;
            jvmProcess.kill('SIGINT');
        });
    };

    var startUp = function () {
        requestPromise('http://localhost:8080')
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