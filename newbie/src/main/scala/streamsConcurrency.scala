/**
  * Created on 8/26/16.
  */
import akka.actor.ActorSystem
import akka.stream._
import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source, Flow}
// TODO:  find out if following is necessary for Futures map calls
import scala.concurrent.ExecutionContext.Implicits.global

// TODO:  review Singleton object syntax!
object firstStreams extends App {            http://stackoverflow.com/questions/31579385/connection-between-js-and-akka-http-websocke:ts-fails-95-of-the-time/36393417


  val system = ActorSystem("LifecycleDemo")
  implicit val materializer = ActorMaterializer.create(system)

  // ****************************
  // ATTN:  TAKEAWAYS!

  /*

- FLOW  is like a REUSABLE STAGE or transformation within a data pipeline;
  Streams always start flowing from a Source[Out,M1]
  then can continue through Flow[In,Out,M2] elements
  or more advanced graph elements to
  finally be consumed by a Sink[In,M3]
  where M is MATERIALIZER class


- Each STAGE async and can go faster or slower than other stages processing entire Stream

- Streams do not run on the CALLER thread. Instead, they run on a different thread in the BACKGROUND, without blocking the caller.

- Stream stages usually share the same thread unless they are explicitly demarcated
  from each other by an ASYNCHRONOUS boundary (which can be added by calling .async between the stages we want to separate).
  .e.g. modules which may be run independently, and don't require prior module to be run
  i.e. DEFAULT is SINGLE-THREADED SERIAL operation!

- Stages demarcated by asynchronous boundaries might run concurrently with each other; or ahead of other stages.

- Stages do not run on a dedicated thread, but they borrow one from a common pool for a short period.

- HOWEVER, each FINAL (FULL-Pipeline) OUTPUT on (any SINGLE) Input from INPUT STREAM essentially does JOIN-WAIT on ALL STAGES' within PIPELINE to
  finish for that SPECIFIC INPUT Segment!
  THEREFORE, the FINAL PIPELINE Output is SEQUENCED-ALIGNED with each Item from INPUT STREAM

 */
  def processingStage(name: String): Flow[String, String, NotUsed] =
    Flow[String].map { s ⇒
      println(name + " started processing " + s + " on thread " + Thread.currentThread().getName)
      Thread.sleep(100) // Simulate long processing *don't sleep in your real code!*
      println(name + " finished processing " + s)
      s
    }

  val completion = Source(List("Hello", "Streams", "World!"))
    .via(processingStage("A")).async
    .via(processingStage("B")).async
    .via(processingStage("C")).async
    .runWith(Sink.foreach(s ⇒ println("Got output " + s)))

  completion.onComplete(_ => system.terminate())

}