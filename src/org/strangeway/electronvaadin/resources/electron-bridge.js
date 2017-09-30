if (window.require) {
    let {remote} = window.require('electron');
    window.callElectronUiApi = remote.getGlobal("callElectronUiApi");
} else {
    window.callElectronUiApi = function (args) {
    };
    console.log("Electron bridge is not available");
}