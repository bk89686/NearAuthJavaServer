<div id='popup' class='popup'>
    <div id='popupIcon'></div>
    <div id='popupX'>x</div>
    <div id='popupTitle'>Title</div>
    <div id='popupMessage'>Message</div>
    <form method='POST' id='companyForm' action='/company'>
        <div id='popupTextAreaDivLabel'></div>
        <div id='popupTextAreaDiv'>
           <textarea id='popupTextArea' name='popupTextArea'></textarea>
        </div>
        <div id='popupTextAreaDiv2Label'></div>
        <div id='popupTextAreaDiv2'>
           <textarea id='popupTextArea2' name='popupTextArea2'></textarea>
        </div>
        <div class='popupInputRow' id='popupInputRow1'>
            <span id='popupInputLabel1'></span><input type='text' class='popupInput' id='popupInput1' name='popupInput1'/>
        </div>
        <div class='popupInputRow' id='popupInputRow2'>
            <span id='popupInputLabel2'></span><input type='text' class='popupInput' id='popupInput2' name='popupInput2'/>
        </div>
        <div class='popupInputRow' id='popupInputRow3'>
            <span id='popupInputLabel3'></span><input type='text' class='popupInput' id='popupInput3' name='popupInput3'/>
        </div>
        <div class='popupInputRow' id='selectUserRow'>
            <label for='role'>Select a role</label>
            <select name='role' id='role'>
                <option value="SUPER_ADMIN" id="superAdminPopup">Super Admin</option>
                <option value="ADMIN" id="adminPopup">Admin</option>
                <option value="AUDITOR" id="auditorPopup">Auditor</option>
                <option value="USER" id="userPopup">User</option>
            </select>
        </div>
        <div id='popupButtons'>
            <button class='popupButton' id='popupCancel' >Cancel</button>
            <button class='popupButton' id='popupOk' >OK</button>
        </div>
        <input id='popupHidden1' name='popupHidden1' type='hidden'>
        <input id='popupHidden2' name='popupHidden2' type='hidden'>
    </form>
</div>
<div id='cover'></div>
