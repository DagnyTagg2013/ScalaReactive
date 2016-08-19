import java.util.concurrent.Executor

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import scala.concurrent.ExecutionContext.Implicits.global

// TODO:  figure if this is overkill; and what the alternative is!
// http://stackoverflow.com/questions/27781476/why-cant-the-compiler-find-implicit-executioncontext-with-implicits-global-impo
// usage of implicit:  http://stackoverflow.com/questions/9416760/scala-named-and-default-arguments-in-conjunction-with-implicit-parameters
// implicit exec: ExecutionContext (scala.concurrent.ExecutionContext) e.g. (implicit exec: Executor) @ END of METHOD ARGLIST!
// http://stackoverflow.com/questions/21256615/scala-executioncontext-for-future-for-comprehension
// http://blog.jessitron.com/2014/02/scala-global-executioncontext-makes.html
// http://stackoverflow.com/questions/35051127/throwing-exception-inside-of-futures-flatmap
// http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html

/**
  * Created on 8/18/16.
  */

/*
  NOTE:  the following is INSPIRED from this Blog post here:
  http://blog.scalac.io/2015/07/30/websockets-server-with-akka-http.html
*/

// TODO:  review Singleton object syntax!
object firstWebSocketServer extends App {

  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  // TODO:  review loading of these Params from an *.inf file; see Akka Http Microservice Template for an example!
  // NOTE:  Scala type inference!
  val interface = "localhost"
  val port = 8000

  // NOTE:  this specifies HANDLERs for specific URL patterns!
  val route = get {
    pathEndOrSingleSlash {
      complete("Welcome to My FIRST Web Socket Server!")
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  // TODO:  find out what unbind operation does -- on RETURN from HTTP request?
  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.shutdown())
  println("Server is down...")

}

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

