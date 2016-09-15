package streams


import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage, Message, UpgradeToWebSocket}
import akka.http.scaladsl.model.{Uri, HttpResponse, HttpRequest}
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.scaladsl.{Sink, Source, Flow}
import akka.http.scaladsl.model.ws.{ TextMessage, Message, BinaryMessage }

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.stream.ActorMaterializer



/**
  * Created on 8/30/16.
  *
  */
// ATTN:  INSPIRATION!
// - http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html
// TODO:
// - Find out if error-handling for Routes approach is as good as request-handler approach!
// TODO:
// - Go look at AkkaHttpServer Activator Tempalte to see how Application.conf can be used!
// TODO:
// - Go look at Akka TestKit to see how this maybe aligned with automated tests; say for
//   FINAL-SEQUENCED RESULTS!
// - http://doc.akka.io/docs/akka/current/scala/testing.html
// - http://stackoverflow.com/questions/36945414/how-do-i-supply-an-implicit-value-for-an-akka-stream-materializer-when-sending-a
// - http://stackoverflow.com/questions/32240359/is-it-possible-to-make-an-akka-http-core-client-request-inside-an-actor


object  streamsWebSocket extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // HANDLING REQUEST @ given URL path via pattern-match
  val requestHandler: HttpRequest => HttpResponse = {

    // CHEAT:  @ BINDS matching result to variable!
    // http://stackoverflow.com/questions/25290275/case-with-an-symbol-in-akka
    // CASE to support WebSocket Request
    // CHEAT:  Akka Symbols
    // http://stackoverflow.com/questions/17644273/akka-in-scala-exclamation-mark-and-question-mark
    // TODO:  verify if this is all that's needed to handle incoming JS request
    // TODO:  verify changes for SERVER-initiated data push to JS client!

    // TODO:  add nicer logging!
    case req @ HttpRequest(GET, Uri.Path("/greeter"), _, _, _) =>
      println("GOT 1:  handling Server Request!")
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => {
                                println("GOT 2:  just prior to handling Service for WebSocket Message!")
                                upgrade.handleMessages(greeterWebSocketService)
                              }
        case None => HttpResponse(400, entity = "Not a valid WebSocket request!")
      }

    // CASE to ignore all other requests
    // TODO:  find out why sample code uses r instead of req
    case req: HttpRequest =>
      req.discardEntityBytes() // important to drain incoming HTTP Entity stream HttpResponse(404, entity = "Unknown resource!")
      HttpResponse(404, entity = "Unknown resource!")
  }

  // ATTN:  HANDLER Service!

  // ATTN:  A message handler is expected to be implemented as a Flow[Message, Message, Any].
  // For typical request-response scenarios this fits very well and such a Flow can be constructed
  // from a simple function by using Flow[Message].map or Flow[Message].mapAsync.
  // There are other use-cases, e.g. in a server-push model, where a server message is sent spontaneously,
  // or in a true bi- directional scenario where input and output aren't logically connected.
  // Providing the handler as a Flow in these cases may not fit. Another method, UpgradeToWebSocket.handleMessagesWithSinkSource,
  // is provided which allows to pass an output-generating Source[Message, Any] and an input-receiving Sink[Message, Any] independently.
  // Note that a handler is required to consume the data stream of each message to make place for new messages. Otherwise,
  // subsequent messages may be stuck and message traffic in this direction will stall.

  // The Greeter WebSocket Service expects a "name" per message and returns a greeting message for that name
  val greeterWebSocketService =
    Flow[Message].mapConcat {
      // we match but don't actually consume the text message here,
      // rather we simply stream it back as the tail of the response
      // this means we might start sending the response even before the
      // end of the incoming message has been received
      case tm: TextMessage => {
        println("GOT 3:  Inside Text Message Handler!")
        TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      }
      case bm: BinaryMessage =>
        // ATTN:  ignore binary messages but drain content to avoid the stream getting clogged!
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
}
