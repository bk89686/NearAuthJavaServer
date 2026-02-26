/**
 * 
 */


let serviceId = getServiceId();

let options = {
    filters: [
        { services: [serviceId] },
    ],
};

button.addEventListener('pointerup', async function(event) {
	const btPermission = await navigator.permissions.query({ name: "bluetooth" });
	if (btPermission.state !== "denied") {
	    navigator.bluetooth.getAvailability().then((available) => {
	        if (available) {
	            console.log("Bluetooth supported");
	            navigator.bluetooth.requestDevice({
					options
				})
				.then(device => { /* … */ })
				.catch(error => { console.error(error); });
	            
	        } else {
	            console.log("Bluetooth in the browser is not supported");
	        }
	    });
	}
});

function getServiceId(){
	
}