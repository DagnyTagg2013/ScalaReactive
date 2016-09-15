/**
 * Created on 8/31/16.
 *
 * INPSIRED BY:
 * - http://blog.teamtreehouse.com/an-introduction-to-websockets
 * - https://github.com/websockets/ws/issues/423
 * - http://stackoverflow.com/questions/32709270/padrino-websockets-heroku-connection-closed-before-receiving-a-handshake-resp
 */

/*
ERROR:
 WebSocket connection to 'ws://localhost:9000/greeter' failed: Connection closed before receiving a handshake response
 basicWebSockets.js:30 WebSocket Error: [object Event]
 */


// "use strict";


// Ensure that WebSocket is defined, BUT then require is not defined!
// var ws = require('ws');


// TODO:  Verify this is all that's needed to initiate a websocket negotiation with the Server?
// What if the SERVER negotiates and does a PUSH?
// TODO:  verify DEFAULT PORT that Akka Http Sever runs from
var wsConnection = new WebSocket("ws://localhost:9000/greeter");

// Show a connected message when the WebSocket is opened.
wsConnection.onopen = function(event) {
    socketStatus.innerHTML = 'Connected to: ' + event.currentTarget.URL;
    socketStatus.className = 'open';
};

// Handle any errors that occur.
wsConnection.onerror = function(error) {
    console.log('WebSocket Error: ' + error);
};


// START:  sends message over WebSocket connection FROM CLIENT TO SERVER
// TODO:  do this the OPPOSITE WAY; or RECEIVE DATA FROM SERVER!
function sendMessage() {
    console.log("SENDING input message, from Client-Side!")
    wsConnection.send(document.getElementById("inputMessage").value);
}

// document.addEventListener("DOMContentLoaded", function (event) {


// TODO:  Verify this handles INCOMING data from WebSockets Server!
wsConnection.onmessage = function (event) {
    console.log(event.data);
    document.getElementById("response").innerHTML = event.data;
};

// TODO: WHEN-WHERE to CLOSE websocket? OR Shutdown and Cleanup anything?
// wsConnection.close();

