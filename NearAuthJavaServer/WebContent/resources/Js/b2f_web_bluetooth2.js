/**
 * 
 */

 class BlueFactor {

  constructor() {
    this.device = null;
    this.onDisconnected = this.onDisconnected.bind(this);
  }
  
  request() {
    let options = {
      "filters": [{
        "name": "NearAuth.ai"
      }],
      "optionalServices": [0xFF02]
    };
    return navigator.bluetooth.requestDevice(options)
    .then(device => {
      this.device = device;
      this.device.addEventListener('gattserverdisconnected', this.onDisconnected);
    });
  }
  
  connect() {
    if (!this.device) {
      return Promise.reject('Device is not connected.');
    }
    return this.device.gatt.connect();
  }
  
  writeColor(data) {
    return this.device.gatt.getPrimaryService(0xFF02)
    .then(service => service.getCharacteristic(0xFFFC))
    .then(characteristic => characteristic.writeValue(data));
  }

  disconnect() {
    if (!this.device) {
      return Promise.reject('Device is not connected.');
    }
    return this.device.gatt.disconnect();
  }

  onDisconnected() {
    console.log('Device is disconnected.');
  }
}

var blueFactor = new BlueFactor();

document.querySelector('bluetoothButton').addEventListener('click', event => {
  blueFactor.request()
  .then(_ => blueFactor.connect())
  .then(_ => { /* Do something with blueFactor... */})
  .catch(error => { console.log(error) });
});