const {app, session, protocol, BrowserWindow, Menu, globalShortcut} = require('electron');
const path = require('path');

let mainWindow = null;
let serverProcess = null;

// Provide API for web application
global.callElectronUiApi = function(args) {
    console.log('Electron called from web app with args "' + args + '"');

    if (args) {
        if (args[0] === 'exit') {
            console.log('Kill server process');

            const kill = require('tree-kill');
            kill(serverProcess.pid, 'SIGTERM', function (err) {
                console.log('Server process killed');

                serverProcess = null;
                mainWindow.close();
            });
        }

        if (args[0] === 'minimize') {
            mainWindow.minimize();
        }

        if (args[0] === 'maximize') {
            if (!mainWindow.isMaximized()) {
                mainWindow.maximize();
            } else {
                mainWindow.unmaximize();
            }
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
    } else if (platform === 'darwin') {
        serverProcess = require('child_process')
            .spawn(app.getAppPath() + '/electron-vaadin/bin/electron-vaadin');
    }

    serverProcess.stdout.on('data', function (data) {
        console.log('Server: ' + data);
    });

    console.log("Server PID: " + serverProcess.pid);

    let appUrl = 'http://localhost:8080';

    function setupVaadinFilesService() {
        protocol.registerFileProtocol('vaadin', (request, callback) => {
            console.log(`Vaadin Request URL: ${request.url}`);

            let urlPath = request.url.substr('vaadin://'.length);
            if (urlPath.indexOf('?') >= 0) {
                urlPath = urlPath.substr(0, urlPath.indexOf('?'));
            }
            if (urlPath.indexOf('#') >= 0) {
                urlPath = urlPath.substr(0, urlPath.indexOf('#'));
            }
            console.log(`Vaadin Request Path: ${urlPath}`);

            const fsPath = path.normalize(`${__dirname}/electron-vaadin/VAADIN/${urlPath}`);

            console.log(`Vaadin Request File: ${fsPath}`);

            callback({path: fsPath});
        }, (error) => {
            if (error) console.error('Failed to register protocol');
        });

        const filter = {
            urls: ['http://localhost:8080/VAADIN/*']
        };
        session.defaultSession.webRequest.onBeforeRequest(filter, (details, callback) => {
            let vaadinFile = details.url.replace('http://localhost:8080/VAADIN/', '');

            console.log(`Vaadin URL: ${vaadinFile}`);

            callback({cancel: false, redirectURL: 'vaadin://' + vaadinFile})
        });
    }

    const openWindow = function () {
        setupVaadinFilesService();

        mainWindow = new BrowserWindow({
            title: 'TODO List - Electron Vaadin application',
            width: 500,
            height: 768,
            frame: false
        });

        mainWindow.loadURL(appUrl);

        // uncomment to show debug tools
        // mainWindow.webContents.openDevTools();

        mainWindow.on('closed', function () {
            mainWindow = null;
        });

        mainWindow.on('close', function (e) {
            if (serverProcess) {
                e.preventDefault();

                mainWindow.webContents.executeJavaScript("appWindowExit();");
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
                }, 200);
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