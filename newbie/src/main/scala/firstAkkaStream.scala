/**
  * Created on 6/28/16.
  *
  * INTELLIJ:
  * - SBT project
  *
  *
  * DOC SOURCES:
  *
  * * Akka Stream & Http Experimental Scala Docs v.2.0.2
  * - SECTION 1.2.1 Quickstart; Reactive Tweets
  * - SECTION 1.7.1 Buffers and working with Rate
  *
  * * Webinar on Akka Streams and HTTP
  * - Akka Streams in 20 seconds
  *
  * * Akka Stream Quickstart
  *
  * CODE SOURCES:
  *
  * * copy build.sbt from Activator Akka Http Microservice template, then download dependencies as follows:
  * from Terminal cmd-line in dir of build.sbt file:
  * - sbt relaod
  * - sbt update; to DOWNLOAD dependencies from remote locations!
  * from IntelliJ:
  * - open SBT window on RHS, then Rclick REFRESH External Sources to be able to RESOLVE
  * dependencies
  *
  *
  *
  */

// *******************************************************
// RANDOM STARTS on code samples
// *******************************************************

/* WEBINAR snippet for REACTIVE FLOWs */
/*
val bluePrint = Source().via(flow).map(_ * 2).to(sink)
val mat: materializer = bluePrint.run()
val f: Future =  Source.single(1).map(_.toString).runWith(Sink.head)
*/

/* Akka Streams WEBINAR for HTTP Streams API snippet */
/*
  - gitHub repo for
  - imports for Tweet does not match
    MY sample tweets of a different format (without
    sentimentScores) and streamed directly in Python from this URL:
     "https://stream.twitter.com/1.1/statuses/sample.json"
  - APIs for translation of Tweet character encoding?
  - auto-parse of statuses, sentimentScores, etc is dealt with WHERE?
  - where default overflow buffer config is for Tweet size; NOT bytesize
  - handlers for timeout, connection errors, etc
  - ("stream") configuration for STREAM connection and OAuth2 params?
  - if .repeat(NotUsed); how does Akka know how to parse object chunk out
    of stream?
  - no need to materialize Actors, right?
*/

/*
path("framed")  {
  entity(asSource[Tweet]) { statuses =>
    val sumScore = statuses.runFold(0) {
      case (sentimentScore, stat) => sentimentScore + stat.sentimentScore }
    onComplete(sumScore) {
      totalSentimentScore =>
        complete(s"total sentiments score:  $totalSentimentsScore")
    } onComplete
  } // entity
} // path
*/

/* AKKA Streams Docs snippet */
/*
   http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-M2/scala/stream-quickstart.html
 */
/*
implicit val system = ActorSystem("reactive-tweets")
implicit val materializer = ActorMaterializer()
val authors: Source[Author, Unit] =
  tweets
    .filter(_.hashtags.contains(akka))
    .map(_.author)
authors.runWith(Sink.foreach(println))
*/

/*  Akka Http Websocket Reactive Streams Activator template snippet
    http://www.lightbend.com/activator/template/akka-http-websocket-reactive-streams
    - Actors are explicit here; and not UNDER an API
    - where is config file for URL, and OAuth2 parameters?
*/
// *******************************************************

import akka.stream._
import akka.stream.scaladsl._

object firstAkkaStream extends App {



}

