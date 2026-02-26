	
document.addEventListener("DOMContentLoaded", function() {
	const date = new Date();
	const offsetInMinutes = date.getTimezoneOffset();
	if (document.getElementById("tzOffset")) {
		document.getElementById("tzOffset").value = offsetInMinutes;
		document.getElementById("timezoneName").textContent = "Timezone: " + getTimeZone() + "."
		setShortTimeZone();
		setTimeAndDate();
		const rangeSearch = document.querySelector("#rangeSearch");
	    if (rangeSearch) {
			rangeSearch.checked = false;
	        rangeSearch.onclick = function() {
				const endTime = document.querySelector("#endTimeDiv");
				const endDate = document.querySelector("#endDateDiv");
				const startTimeLabel = document.querySelector("#startTimeLabel");
				const startDateLabel = document.querySelector("#startDateLabel");
	            if (rangeSearch.checked) {
					startTimeLabel.textContent = "Start time: ";
					startDateLabel.textContent = "Start date: ";
					endTime.style.display = 'block';
					endDate.style.display = 'block';
				} else {
					startTimeLabel.textContent = "Time: ";
					startDateLabel.textContent = "Date: ";
					endTime.style.display = 'none';
					endDate.style.display = 'none';
					document.querySelector("#endTime").value = "";
					document.querySelector("#endDate").value = "";
				}
	        };
	    }
    }
});

getTimeZone = function(){
	const date = new Date();
	return new Intl.DateTimeFormat([], { timeZoneName: 'long' })
    .formatToParts(date)
    .find(part => part.type === 'timeZoneName').value;
}

setShortTimeZone = function(){
	const date = new Date();
	const shortName = date.toLocaleString('en-US', {
		timeZoneName: 'short'
	});
	const tzStr = shortName.split(' ').pop();
	document.getElementById("tzString").value = tzStr;
}

setTimeAndDate = function (){
	const startTime = document.querySelector("#startTime");
	const startDate = document.querySelector("#startDate");
	const endTime = document.querySelector("#endTime");
	const endDate = document.querySelector("#endDate");
	const today = new Date();
	today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
	const formattedDate = today.toISOString().slice(0, 10);
	const formattedTime = today.toISOString().slice(11, 16);
	startDate.value = formattedDate;
	startTime.value = formattedTime;
	endDate.value = formattedDate;
	endTime.value = formattedTime;
};