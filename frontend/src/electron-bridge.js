if (window.require) {
    let {remote} = window.require('electron');
    window.callElectronUiApi = remote.getGlobal("callElectronUiApi");
} else {
    window.callElectronUiApi = function (args) {
    };
    console.error("Electron bridge is not available");
}