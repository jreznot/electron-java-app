call gradlew bundle
rmdir .\electron-src\electron-vaadin-win32-x64\ /s /q
cd electron-src
call electron-packager . --no-prune --icon=icon.ico
xcopy ..\build\install\electron-vaadin .\electron-vaadin-win32-x64\electron-vaadin\ /E
cd ..