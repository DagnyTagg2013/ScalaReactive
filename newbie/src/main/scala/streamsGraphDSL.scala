/**
  * Created on 7/29/16.
  */

// **********************
// ATTN:  INSPIRATION!

/*
- (1) This is plagiarized from Kevin Webber of Lightbend (BUT adapted from using FlowGraph to GraphDSL!
https://medium.com/@kvnwbbr/a-journey-into-reactive-streams-5ee2a9cd7e29#.vb64a0ja9
- (2) This is modified according to latest Akka 2.4.8 Docs here:
http://doc.akka.io/docs/akka/2.4.8/scala/stream/stream-quickstart.html#broadcasting-a-stream
 */

import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._
import GraphDSL.Implicits._

// ****************************
// ATTN:  TAKEAWAYS!


// TODO:  necessary to create SINGLETON INSTANCE; as no STATICs w Classes in Scala!
object streamsGraphDSL extends App {

  // TODO:  Is it necessary create 1:1 ActorSystems PER Graph, or Flow Pipeline?
  // TODO:  Typically use DEFAULT ActorMaterializer?
implicit val system = ActorSystem("Sys")
implicit val materializer = ActorMaterializer()

val displayResults = Sink.foreach(println)

val myGraph = RunnableGraph.fromGraph(GraphDSL.create() {

  // TODO:  confused about this EXACT syntax, as have to init/create; but statements below refer to itself!
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
    inputSource ~> f1 ~> aBroadCast ~> f2 ~> aMerge ~> f3 ~> displayResultSink
    aBroadCast ~> f4 ~> aMerge

    // TODO:  understand what following does; and does it CLOSE/UNALLOCATE materialized pipeline?
    ClosedShape

} // end of Graph Create
) // end of RunnableGraph.fromGraph

// TODO:  RunnableGraph doesn't have INITIAL take() or onNext() methods to BOUND Stream Sink results from!
/*
val MAX_BOUND_RESULT_SIZE = 7
// OK. Collect up until max-th elements only, then cancel upstream
val boundedResult: Future[Seq[Int]] =
  myGraph.take(MAX_BOUND_RESULT_SIZE).runWith(Sink.seq)
*/

/*
    TODO:  want to use Subscriber.onNext() API to get real-Time results; i.e. simulate where Source is a real unbounded STREAM
    TODO:  is it only necessary to use Subscriber.request(7) to signal INITIAL pull buffer from Subscriber
    but then that's a MAX flow rate of Subscriber; and what if that somehow slows down, and
    we need to change Subscription buffer-size dynamically?
    TODO:  how to stop and cleanup-release materialized pipeline?
*/

  // ATTN:  this MATERIALIZEs the Graph DSL on RUN!
myGraph.run()

}  // End of App
