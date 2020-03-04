const {app, session, protocol, BrowserWindow, globalShortcut, dialog} = require('electron');
const path = require('path');

let mainWindow = null;
let serverProcess = null;

app.allowRendererProcessReuse = true;

// Provide API for web application
global.callElectronUiApi = function() {
    console.log('Electron called from web app with args "' + JSON.stringify(arguments) + '"');

    if (arguments) {
        switch (arguments[0]) {
            case 'exit':
                console.log('Kill server process');

                const kill = require('tree-kill');
                kill(serverProcess.pid, 'SIGTERM', function (err) {
                    console.log('Server process killed');

                    serverProcess = null;

                    if (mainWindow !== null ) {
                        mainWindow.close();
                    }
                });
                break;
            case 'minimize':
                mainWindow.minimize();
                break;
            case 'maximize':
                if (!mainWindow.isMaximized()) {
                    mainWindow.maximize();
                } else {
                    mainWindow.unmaximize();
                }
                break;
            case 'devtools':
                mainWindow.webContents.openDevTools();
                break;
        }
    }
};

app.on('window-all-closed', function () {
    app.quit();
});

app.on('ready', function () {
    platform = process.platform;

    if (platform === 'win32') {
        serverProcess = require('child_process')
            .spawn('cmd.exe', ['/c', 'electron-vaadin.bat'],
                {
                    cwd: './electron-vaadin/bin'
                });
    } else {
        serverProcess = require('child_process')
            .spawn(__dirname + '/electron-vaadin/bin/electron-vaadin');
    }

    if (!serverProcess) {
        console.error('Unable to start server from ' + __dirname);
        app.quit();
        return;
    }

    serverProcess.stdout.on('data', function (data) {
        process.stdout.write('Server: ' + data);
    });
    serverProcess.stderr.on('data', function (data) {
        process.stderr.write('Server error: ' + data);
    });

    serverProcess.on('exit', code => {
        serverProcess = null;

        if (code !== 0) {
            console.error(`Server stopped unexpectedly with code ${code}`);
            dialog.showErrorBox("An error occurred", "The server stopped unexpectedly, app will close.");
        }
        if (mainWindow !== null ) {
            mainWindow.close();
        }
    });

    console.log("Server PID: " + serverProcess.pid);

    let appUrl = 'http://localhost:8080';

    const openWindow = function () {
        mainWindow = new BrowserWindow({
            title: 'TODO List - Electron Vaadin application',
            width: 500,
            height: 768,
            frame: false,
            webPreferences: { nodeIntegration: true }
        });

        mainWindow.loadURL(appUrl);

        // uncomment to show debug tools
        //mainWindow.webContents.openDevTools();

        mainWindow.on('closed', function () {
            mainWindow = null;
        });

        mainWindow.on('close', function (e) {
            if (serverProcess) {
                e.preventDefault();

                mainWindow.webContents.executeJavaScript("vaadinApi.appWindowExit();");
            }
        });
    };

    const startUp = function () {
        const requestPromise = require('minimal-request-promise');

        requestPromise.get(appUrl).then(function (response) {
                console.log('Server started!');
                openWindow();
            }, function (response) {
                console.log('Waiting for the server start...');

                setTimeout(function () {
                    startUp();
                }, 1000);
            });
    };

    startUp();

    // Register a shortcut listener.
    const ret = globalShortcut.register('CommandOrControl+Shift+`', () => {
        console.log('Bring to front shortcut triggered');
        if (mainWindow) {
            mainWindow.focus();
        }
    })
});

app.on('will-quit', () => {
    // Unregister all shortcuts.
    globalShortcut.unregisterAll();
});