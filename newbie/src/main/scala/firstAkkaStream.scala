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
  * GIT:
  * * ALWAYs to git status before a commit!
  * * to rollback ADD of too many files prior to COMMIT:
  * - git reset
  * * to commit files physically deleted (and all other staged changes)
  * - git add -u
  *   git add -u for whole working tree
  *   git add -u . for current directory
  *
  */

import akka.stream._
import akka.stream.scaladsl._

object firstAkkaStream extends App {


  /* WEBINAR snippet */
  /*
  val bluePrint = Source().via(flow).map(_ * 2).to(sink)
  val mat: materializer = bluePrint.run()
  val f: Future =  Source.single(1).map(_.toString).runWith(Sink.head)
  */

  /* DOCs snippet */
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()
  val authors: Source[Author, Unit] =
    tweets
      .filter(_.hashtags.contains(akka))
      .map(_.author)
  authors.runWith(Sink.foreach(println))

}

