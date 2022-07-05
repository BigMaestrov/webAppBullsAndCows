/**
 * Module with auxiliary functions for input processing.
 * @author Vladimir Kizelbashev
 * @version 1.0
 */

/**
 * Cancels the default browser action to prevent a form request from being sent.
 * @param evt Event
 */
function keyDown(evt) {
    if (evt.keyCode == 13 || evt.which == 13) {
        evt.preventDefault();
    }
}

/**
 * Allows to enter only numbers in the input field without repeating the already entered numbers.
 * @param evt Event
 */
function keyPress(evt) {
    var str = document.getElementById("guess-text").value;
    var chr = getChar(evt);
    if (chr == null) {
        return;
    }
    if (chr < '0' || chr > '9' || str.indexOf(chr) > -1) {
        return evt.preventDefault();
    }
}

/**
 * Returns the entered char.
 * @param evt Event
 * @returns {*}
 */
function getChar(evt) {
    if (evt.which == null) {
        if (evt.keyCode < 32) {
            return null;
        }
        return String.fromCharCode(evt.keyCode) // IE
    }
    if (evt.which != 0 && evt.charCode != 0) {
        if (evt.which < 32) {
            return null;
        }
        return String.fromCharCode(evt.which) // others
    }
    return null;
}

/**
 * Inserts a symbol in the text input field (maximum of 4 digits).
 * It is the digit from 0 to 9, corresponding to the pressed button.
 * @param val Value of pressed button
 */
function btnClick(chr) {
    var text = document.getElementById("guess-text");
    if (text.value.length < 4 && chr >= '0' && chr <= '9' && text.value.indexOf(chr) < 0) {
        text.value += chr;
    }
}
