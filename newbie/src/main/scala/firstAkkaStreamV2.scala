/**
  * Created on 7/29/16.
  */

/*
- (1) This is plagiarized from Kevin Webber of Lightbend:
https://medium.com/@kvnwbbr/a-journey-into-reactive-streams-5ee2a9cd7e29#.vb64a0ja9
- (2) This is modified according to latest Akka 2.4.8 Docs here:
http://doc.akka.io/docs/akka/2.4.8/scala/stream/stream-quickstart.html#broadcasting-a-stream
 */


import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._

// NOTE:  below now no longer used from deprecated FlowGraph ...
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

// NOTE: below modified from deprecated FlowGraph ...
import GraphDSL.Implicits._

import scala.util.{ Failure, Success }


object firstAkkaStreamV2 extends App {


  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()

  val displayResults = Sink.foreach(println)

  val myGraph = RunnableGraph.fromGraph(GraphDSL.create() {

    implicit aBuilder =>

      // TODO:  What does this do?
      val displayResultSink = Sink.foreach[Int](elem => println(s"sink received: $elem"))

      val inputSource = Source(1 to 10)

      val aBroadCast = aBuilder.add(Broadcast[Int](2))
      val aMerge = aBuilder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      inputSource ~> f1 ~> aBroadCast ~> f2 ~> aMerge ~> f3 ~> displayResultSink
                           aBroadCast ~> f4 ~> aMerge

      // TODO:  understand what following does; and does it CLOSE/UNALLOCATE materialized pipeline?
      ClosedShape
    } // end of aBuilder
    ) // end of Graph

  // TODO:  RunnableGraph doesn't have take() method to BOUND Stream Sink results from!
  /*
  val MAX_BOUND_RESULT_SIZE = 7
  // OK. Collect up until max-th elements only, then cancel upstream
  val boundedResult: Future[Seq[Int]] =
    myGraph.take(MAX_BOUND_RESULT_SIZE).runWith(Sink.seq)
  */

  /*
      TODO:  want to use Subscriber.onNext() API to get real-Time results; i.e. simulate where Source is a real unbounded STREAM
      TODO:  is it only necessary to use Subscriber.request(8) to signal INITIAL pull buffer from Subscriber
      but then that's a MAX flow rate of Subscriber; and what if that somehow slows down, and
      we need to change Subscription buffer-size dynamically?
      TODO:  how to stop and cleanup-release materialized pipeline?
  */

  myGraph.run()

  // NOTE:  below copied from Kevin Weber example with now deprecated FlowGraph ...
  /*
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    def main(args: Array[String]): Unit = {

      val out = Sink.foreach(println)

      val g = FlowGraph.closed(out) { implicit builder =>
        sink =>

          val in = Source(1 to 10)

          val bcast = builder.add(Broadcast[Int](2))
          val merge = builder.add(Merge[Int](2))

          val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

          in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> sink.inlet
          bcast ~> f4 ~> merge
      }.run()

      // ensure the output file is closed and the system shutdown upon completion
      g.onComplete {
        case Success(_) =>
          system.shutdown()
        case Failure(e) =>
          println(s"Failure: ${e.getMessage}")
          system.shutdown()
      }
    } // end of main

 */
}


