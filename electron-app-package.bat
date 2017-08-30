call gradlew bundle
rmdir .\electron-src\electron-vaadin-win32-x64\ /s /q
cd electron-src
call electron-packager . --icon=icon.ico --ignore=README.md --ignore=.npmignore --ignore=.travis.yml
xcopy ..\build\install\electron-vaadin .\electron-vaadin-win32-x64\electron-vaadin\ /E
cd ..