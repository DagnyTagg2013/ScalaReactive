

/**
  * Created on 8/18/16.
  */

// QUESTIONS:
// bindAndHandleSync vs BindAndHandleAsync

/*
  NOTE:  the following is INSPIRED from these Blog posts here:
  http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html#example
  https://github.com/akka/akka/blob/v2.4.9/akka-docs/rst/scala/code/docs/http/scaladsl/server/WebSocketExampleSpec.scala
  http://blog.scalac.io/2015/07/30/websockets-server-with-akka-http.html
  https://dzone.com/articles/create-reactive-websocket
  https://dzone.com/articles/creating-a-scalable-websocket-application-in-an-ho
  http://stackoverflow.com/questions/31579385/connection-between-js-and-akka-http-websocke:ts-fails-95-of-the-time/36393417

  NOTE:  LATEST Akka Docs here:
  http://doc.akka.io/docs/akka/2.4.9/scala/stream/stream-quickstart.html
  http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/directives/
  http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html#server-side-websocket-support-scala
  http://doc.akka.io/docs/akka/2.4.9/scala/http/low-level-server-side-api.html#controlling-server-parallelism
  http://doc.akka.io/docs/akka/2.4.9/scala/http/introduction.html#routing-dsl-for-http-servers
  http://doc.akka.io/docs/akka/2.4.9/scala/stream/stream-cookbook.html#working-with-flows
  http://doc.akka.io/docs/akka/2.4.9/scala/http/client-side/websocket-support.html#message
  http://doc.akka.io/docs/akka/2.4.9/scala/futures.html#composing-futures

  TODO:  learn how to use the WebSocketTool!
  NOTE:  CHROME WEBSOCKET TOOL here:
  https://blog.kaazing.com/2012/05/09/inspecting-websocket-traffic-with-chrome-developer-tools/

*/

// TODO:  figure if this is overkill; and what the alternative is!
// http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html#server-api
// http://stackoverflow.com/questions/27781476/why-cant-the-compiler-find-implicit-executioncontext-with-implicits-global-impo
// usage of implicit:  http://stackoverflow.com/questions/9416760/scala-named-and-default-arguments-in-conjunction-with-implicit-parameters
// implicit exec: ExecutionContext (scala.concurrent.ExecutionContext) e.g. (implicit exec: Executor) @ END of METHOD ARGLIST!
// http://stackoverflow.com/questions/21256615/scala-executioncontext-for-future-for-comprehension
// http://blog.jessitron.com/2014/02/scala-global-executioncontext-makes.html
// http://stackoverflow.com/questions/35051127/throwing-exception-inside-of-futures-flatmap
// http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html
// http://stackoverflow.com/questions/31579385/connection-between-js-and-akka-http-websockets-fails-95-of-the-time/36393417


import akka.actor.ActorSystem
import akka.{ Done, NotUsed }
import akka.stream.ActorMaterializer
import scala.concurrent.{TimeoutException, Await}
import scala.io.StdIn
// TODO:  find out if following is necessary for Futures map calls
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.Http
import akka.stream.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import scala.concurrent.duration._


// TODO:  review Singleton object syntax!
object firstWebSocketServer extends App {

  implicit val actorSystem = ActorSystem("akka-system")
  // TODO:  figure how FlowMaterializer is distinct from ActorMaterializer!
  implicit val flowMaterializer = ActorMaterializer()

  // ***** CONFIGURATION *****
  // TODO:  review loading of these Params from an *.inf file;
  // see Akka Http Microservice Template for an example!
  // NOTE:  Scala type inference!
  val INTERFACE = "localhost"
  val PORT = 9001

  // ***** REQUEST *****
  // TODO:  find out when unapply gets called; then what its used for!
  object WSRequest {

    def unapply(req: HttpRequest): Option[HttpRequest] = {

      if (req.header[UpgradeToWebSocket].isDefined) {
        req.header[UpgradeToWebSocket] match {
          case Some(upgrade) => Some(req)
          case None => None
        }
      } else None

    }

  }

  // ***** URL BINDINGS TO HANDLERS! *****

  // NOTE:  from Scala.io BLOG POST and FOR SIMPLE HTTP SERVER!
  /*

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  // TODO:  find out what unbind operation does -- on RETURN from HTTP request?
  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
  println("Server is down...")

  // NOTE:  this specifies HANDLERs for specific URL patterns!
  val route = get {    def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String] = None)(implicit mat: FlowMaterializer): HttpResponse

    pathEndOrSingleSlash {      def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String] = None)(implicit mat: FlowMaterializer): HttpResponse

      complete("Welcome to My FIRST Web Socket Server!")
    }
    // NOTE: TILDE does somposition of several routes into one rule
  }

*/

  // NOTE:  from DevZone WAY!
  /*
  Streams always start flowing from a Source[Out,M1]
  then can continue through Flow[In,Out,M2] elements
  or more advanced graph elements to
  finally be consumed by a Sink[In,M3]
  where M is MATERIALIZER class

  As you can see this function requires a Flow with an open input which accepts a Message
  and an open output which also expects a message.
  Akka-streams will attach the created websocket as a Source and pass any sent messages from the client into this flow.
  Akka-streams will also use the same websocket as a Sink and pass the resulting message from this flow to it.
  The result from this function is a HTTPResponse that will be sent to the client.
  */

  /**
    * Simple helper function, that connects a flow to a specific websocket upgrade request
    */
  def customHandleWith(req: HttpRequest, flow: Flow[Message, Message, NotUsed]) = req.header[UpgradeToWebSocket].get.handleMessages(flow)

  /*
     Calling Flow[Message] like this, returns a minimal flow,
     which just returns the message it received as is input,
     directly to the output. So in our case any websocket message received,
     is passed back to the client.
   */
  def echoFlow: Flow[Message, Message, NotUsed] = Flow[Message]

  // TODO:  how to properly instantiate materializer for use; OR use the global default one!
  // TODO:  currently CANNOT resolve flowMaterializer!
  // def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String] = None)(implicit mat: FlowMaterializer): HttpResponse
  // def handleMessages(handlerFlow: Flow[Message, Message, Any], subprotocol: Option[String] = None)(implicit mat: ActorMaterializer): HttpResponse

  // TODO:  find out whether best to use bindAndHandle, or bindAndHandleAsync
  // TODO:  find out whether to SEPARATE out call into requestHandler!
  // NOTE:  handleWith is not supported with req and flow params!
  /*
  val binding = Http().bindAndHandleSync({
    // case WSRequest(req@HttpRequest(GET, Uri.Path("/echo"), _, _, _)) => handleWith(req, echoFlow)
    case req@WSRequest(GET, Uri.Path("/echo"), _, _, _) => customHandleWith(req, echoFlow)
    case _: HttpRequest => HttpResponse(400, entity = "Invalid websocket request")
  }, INTERFACE, PORT)
  */
  val binding = Http().bindAndHandleSync({
    // case WSRequest(req@HttpRequest(GET, Uri.Path("/echo"), _, _, _)) => handleWith(req, echoFlow)
    case req@WSRequest(GET, Uri.Path("/echo"), _, _, _) => customHandleWith(req, echoFlow)
    case _: HttpRequest => HttpResponse(400, entity = "Invalid websocket request")
  }, INTERFACE, PORT)

  // TODO:  find out how this is BOUND to the incoming REQUEST!
  // http://doc.akka.io/docs/akka/2.4.9/scala/http/routing-dsl/websocket-support.html#example

  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/greeter"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessages(greeterWebSocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }

  // The Greeter WebSocket Service expects a "name" per message and
  // returns a greeting message for that name
  val greeterWebSocketService =
    Flow[Message]
      .mapConcat {
        // we match but don't actually consume the text message here,
        // rather we simply stream it back as the tail of the response
        // this means we might start sending the response even before the
        // end of the incoming message has been received
        case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
        case bm: BinaryMessage =>
          // ignore binary messages but drain content to avoid the stream being clogged
          bm.dataStream.runWith(Sink.ignore)
          Nil
      }

  // binding is a future, we assume it's ready within a second or timeout
  try {
    Await.result(binding, 1 second)
    println("Server online at http://localhost:9001")
  } catch {
    case exc: TimeoutException =>
      println("Server took to long to startup, shutting down")
      // TODO:  verify if this is correct way to shutdown, as DEPRECATED method!
      actorSystem.shutdown()
  }

} // END APP


/*
  val requestHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(HttpMethods.GET, Uri.Path("/greeter"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessages(greeterWebSocketService)
        case None          => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }  http://www.websocket.org/echo.html
*/


// NOTE:  GENERAL outline BELOW!
/*

  // STEP1:  create a fake Data Sequence for input data: then make it a SOURCE of an Akka FLOW!
  // TODO:  do this
  Sequence


  //STEP2:  create FLOW where we DO NOT accept input from EXTERNAL; and just stream to EXTERNAL!
  // TODO:  overflow strategy NOT to keep overflow
  Flow.wrap(Sink.ignore, tweetSource filter tweetFilter map toMessage)(Keep.none)


  // STEP3: use WebSocket API to stream out to UI
  def mockUserTweetsSocketOut = (pathPrefix("users") & path(Segment)) { userName =>
    handleWebsocketMessages(tweetFlowOfUser(userName))
  }

  // TODO:  setup this configuration info in an EXTERNAL config file!
  /ws/tweets/users/[userName].
    pathPrefix("ws") {
      pathPrefix("tweets") {
        get {
          mockUserTweetsSocketOut
        } }
    }
  // TODO:  modify index.html to RECEIVE this Tweets info!

*/

