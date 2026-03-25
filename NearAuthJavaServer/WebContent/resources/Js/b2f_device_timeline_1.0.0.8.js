document.addEventListener("DOMContentLoaded", function(){
	hideNowIfNeeded();
	refreshPost();
	const rename = document.getElementById("renameButton");
	if (rename) {
		rename.onclick = function() {
			showPopup();
			const input = document.getElementById("popupInput1");
			input.focus();
			input.addEventListener('keypress', function(event) {
				if (event.key === 'Enter') {
					event.preventDefault();
					if (document.getElementById("popupInput1").value.trim()) {
						document.getElementById("popupInput1").value = document.getElementById("popupInput1").value.trim()
						document.getElementById("renameForm").submit();
					}
				}
			})
		}
	}
});

var popupX  = document.querySelector("#popupX");
if (popupX) {
    popupX.onclick = function(event) {
        closePopup();
        event.preventDefault();
    };
}

var popupCancel  = document.querySelector("#popupCancel");
if (popupCancel) {
    popupCancel.onclick = function(event) {
        closePopup();
        event.preventDefault();
    };
}

var popupOk = document.querySelector("#popupOk");
if (popupOk) {
    popupOk.onclick = function(event) {
    	if (document.getElementById("popupInput1")) {
			if (document.getElementById("popupInput1").value.trim()) {
				document.getElementById("popupInput1").value = document.getElementById("popupInput1").value.trim()
				document.getElementById("renameForm").submit();
			} else {
				event.preventDefault();
			}
    	} else {
			event.preventDefault();
		}
    };
}

function refreshPost(){
	try {
		if (document.getElementById("fromPush")) {
			if (document.getElementById("fromPush").value == "true") {
				window.location.href = window.location.href;
			}
		}
	} catch (e) {
		
	}
}

function showPopup(){
	showCover();
	var popup = document.getElementById("popup");
    popup.style.display = "block";
    popup.style.zIndex = 101;
    var popupInputRow = document.getElementById("popupInputRow1");
    popupInputRow.style.display = "block";
}

function closePopup(){
    var popup = document.getElementById("popup");
    popup.style.display = "none";
    popup.style.top = "50%";
    popup.style.zIndex = -100;
    hideCover();
    document.getElementById("popupInput1").value = "";
}

function showCover(){
    var cover = document.getElementById("cover");
    cover.style.display = "block";
    cover.style.zIndex = 100;
}

function hideCover(){
    var cover = document.getElementById("cover");
    cover.style.display = "none";
    cover.style.zIndex = -100;
    
}

function showPopupText(popupElemId, segmentElemId, rowElemId) {
	
    let popup = document.getElementById(popupElemId);
    if (popup.style.visibility == "visible") {
    	popup.style.visibility = "hidden";
    } else {
    	const popups = document.querySelectorAll('.popuptext');
    	popups.forEach(element => {
    	    element.style.visibility = 'hidden';
    	});
    	let segment = document.getElementById(segmentElemId);
    	if (segment) {
        const segmentLeft = getXPosition(segment);
        const width = segment.offsetWidth;
        const rowX = getXPositionById(rowElemId)
        popup.style.left = (segmentLeft - rowX - 10 + (width/2)) + "px";
        popup.style.visibility = "visible";
	        setTimeout (function() {
	        	popup.style.visibility = "hidden";
	        }, 3000);
    	}
    }
}

function hideNowIfNeeded(){
	const nowText = document.getElementById("nowText");
	const nowX = getXPosition(nowText);
	const dayLabel = document.getElementById("dayLabel1");
	if (dayLabel != null) {
    	const dayLabelX = getXPosition(dayLabel);
    	const dayLabelWidth = dayLabel.offsetWidth;
    	const dayLabelRight = dayLabelX + dayLabelWidth;
    	if (dayLabelRight + 10 > nowX) {
    		nowText.style.visibility = "hidden";
    		document.getElementById("nowArrow").style.visibility = "hidden";
    	}
    }
}

function getXPositionById(elemId){
	const element = document.getElementById(elemId);
	return getXPosition(element);
}

function getXPosition(element){
	let left = null;
	if (element != null) {
    	const rect = element.getBoundingClientRect();
    	left = rect.left
	}
	return left;
}