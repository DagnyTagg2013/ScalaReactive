/**
 * Created on 8/31/16.
 */

// TODO:  Verify this is all that's needed to initiate a websocket negotiation with the Server?
// What if the SERVER negotiates and does a PUSH?
// TODO:  verify DEFAULT PORT that Akka Http Sever runs from
var connection = new WebSocket("ws://localhost:8080/greeter");


function sendMessage() {
    connection.send(document.getElementById("inputMessage").value);
}

document.addEventListener("DOMContentLoaded", function (event) {

    // ATTENTION:  URL must MATCH Server Source URL for emitting Stream to!
    connection.onopen = function (event) {
        connection.send("connection established");
        console.log("connection established, Client-Side!")
    };

    // TODO:  Verify this handles INCOMING data from WebSockets Server!
    connection.onmessage = function (event) {
        console.log(event.data);
        document.getElementById("response").innerHTML = event.data;
    };

    // TODO: Any need to CLOSE websocket?
})
