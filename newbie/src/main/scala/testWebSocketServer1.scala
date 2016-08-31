
import akka.http.scaladsl.testkit.{WSProbe}
import akka.util.ByteString

// ATTN:  following TWO import(s) needed for ~> Operator!
import akka.stream.scaladsl._
import GraphDSL.Implicits._
import akka.stream.scaladsl.GraphDSL

import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.{ UnsupportedWebSocketSubprotocolRejection, ExpectedWebSocketRequestRejection, Route, RoutingSpec }


// ATTN:  INSPIRATION!
// - http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html

// *****************************************************
// ATTN: TEST SCRIPT!

// TESTS:
// Create a testing probe representing the client-side
val wsClient = WSProbe()

// WS creates a WebSocket request for testing
// WS("/greeter", wsClient.flow) ~> websocketRoute ~>
WS("/greeter", wsClient.flow) ~> streamsWebSocket.requestHandler ~>

  check {

    // checks response for WS Upgrade headers
    isWebSocketUpgrade shouldEqual true

    // manually run a WS conversation
    wsClient.sendMessage("Peter")
    wsClient.expectMessage("Hello Peter!")

    wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
    wsClient.expectNoMessage(100.millis)

    wsClient.sendMessage("John")
    wsClient.expectMessage("Hello John!")

  }



