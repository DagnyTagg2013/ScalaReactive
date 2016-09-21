package streams

/**
  * Created on 7/29/16.
  */

// **********************
// ATTN:  INSPIRATION!

/*
- (1) This is plagiarized from Kevin Webber of Lightbend (BUT adapted from using FlowGraph to GraphDSL!
https://medium.com/@kvnwbbr/a-journey-into-reactive-streams-5ee2a9cd7e29#.vb64a0ja9
- (2) This is modified according to latest Akka 2.4.8 Docs here:
http://doc.akka.io/docs/akka/2.4.8/scala/stream/stream-quickstart.html#broadcasting-a-
- (3) TODO:  BOUNDED BUFFER BACKPRESSURE!
http://doc.akka.io/docs/akka/2.4.10/scala/stream/stream-quickstart.html
 */

import akka.actor.ActorSystem
import akka.stream.{OverflowStrategy, ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._
import GraphDSL.Implicits._

// ****************************
// ATTN:  TAKEAWAYS!


// NOTE:  necessary to create SINGLETON INSTANCE; as no STATICs w Classes in Scala!
object streamsGraphDSL extends App {

  // TODO 1:  Typically use DEFAULT ActorMaterializer; or different dedicated one-per-Graph?
  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()

  val displayResults = Sink.foreach(println)

  val myGraph = RunnableGraph.fromGraph(GraphDSL.create() {

    // TODO 2:  confused about this line, as have to init/create a builder; but statements within refer to builder itsef?
    implicit aBuilder =>

      // ATTN:  this defines a Graph SINK that prints results
      val displayResultSink = Sink.foreach[Int](elem => println(s"sink received: $elem"))

      // ATTN: this defines a Graph Source as just a simple numeric sequence
      val inputSource = Source(1 to 10)

      // ATTN:  this defines Graph PATH Divergence/Broadcast, Convergence/Merge
      val aBroadCast = aBuilder.add(Broadcast[Int](2))
      val aMerge = aBuilder.add(Merge[Int](2))

      // ATTN:  defining Flow stages that operate on EACH element in a Stage!
      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      // ATTN:  usage of ~> to COMBINE elements into flows
      // ATTN:  PARALLEL invocation of BROADCAST!
      // ATTN:  BROADCAST essentially DUPLICATES inputs to F3,
      //        MERGE then GLUES the DUPLICATES!

      // TODO 3:  MODIFIED to calling buffered API within Graph mapping -- check assumptions!
      //          - where INTERNAL Akka implementation calls onNext() to get next BUFFERED batch,
      //            so you don't have to worry about it as a DEV?
      //          - NUMERIC bound of 10 refers to NUMBER of elements (of possibly complex types) on a
      //            UNIFORM-ELEMENT-TYPED stream, rather than Bytes, right?
      //          - if source produces N < BUFFER_MAX elements; then those are simply passed through the pipeline without
      //            waiting to accumulate BUFFER_MAX elements
      //
      inputSource.buffer(10, OverflowStrategy.dropHead) ~> f1 ~> aBroadCast ~> f2 ~> aMerge ~> f3 ~> displayResultSink
      aBroadCast ~> f4 ~> aMerge

      // TODO 4:  understand what following does; and does it CLOSE/UNALLOCATE materialized pipeline?
      ClosedShape

  } // end of Graph Create
  ) // end of RunnableGraph.fromGraph

  // ATTN:  this MATERIALIZEs the Graph DSL on RUN!
  myGraph.run()

}// End of App

