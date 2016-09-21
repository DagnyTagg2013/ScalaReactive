package actors

/**
  *
  * Created on 9/1/16.
  *
  * INSPIRED BY:
  * - https://www.toptal.com/scala/concurrency-and-fault-tolerance-made-easy-an-intro-to-akka
  * - https://gist.github.com/Diego81/9887105
  *
  * CHEAT:
  * - ! (“tell”) – sends the message and returns immediately
  * - ? (“ask”) – sends the message and returns a Future representing a possible reply
  *
  * ADAPTED to work with Akka v2.4.9
  * - http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  *
  * GO READ THIS for info on ActorSystem, Context, Props, etc:
  * - http://doc.akka.io/docs/akka/current/general/actor-systems.html#actor-systems
  * - http://doc.akka.io/docs/akka/current/scala/actors.html
  * - integrating Streams with LOCAL Actors:
  * http://doc.akka.io/docs/akka/2.4.10/scala/stream/stream-integrations.html#integrating-with-external-services
  *
  * Recommended Practices:
It is a good idea to provide factory methods on the companion object of each Actor
which help keeping the creation of suitable Props as close to the actor definition as possible.
This also avoids the pitfalls associated with using the Props.apply(...) method which takes a by-name argument,
since within a companion object the given code block will not retain a reference to its enclosing scope
  *
  *
  * TODO 9:
  * HUGE QUANDARY:
  * - Akka Clustering solution requires hardcoding of IPs of Cluster Nodes -- an Ops nightmare on adding-deleting Nodes
  * - it also has finicky Master-Slave hierarchy requirements for Startup and Shutdown protocols -- not elegant and a SPOF vs Peer-to-Peer
  * - NOT SURE how Akka Actors State-Machine info can integrate well with Cassandra!
  * - is it necessary to use the KRYO serializer?
  * - Actors should handle events (i.e., process messages) asynchronously and should not block,
  * otherwise context switches will happen which can adversely affect performance.
  * Specifically, it is best to perform blocking operations (IO, etc.) in a Future so as not to block the actor; i.e.:
  *
  * case evt => blockingCall() // BAD
  * case evt => Future {
  *   blockingCall()           // GOOD
  * }
  *
  *
  * NOTE:
  * - BAD practice is to pass out references to internal Actor state on messages out
  * - INSTEAD, have incremental state requests on messages INTO Actor which modifies its OWN state
  * - GOOD practice to have FACTORY METHOD Props() on SINGLETON object for class to invoke from implicit thread-pooling Context:
  *  context.actorOf(DemoActor.props(42), "demo")
  *
  * TODOs:
  * - TODO 0:  CONFIGURABLE CREATION with ApplicationContext external startup config file using DEPENDENCY INJECTION INSTEAD of empty Props() file?
  * - LOCATION TRANSPARENCY for Actor Creation required Application.conf WITH HARDCODED NODES!
  * http://doc.akka.io/docs/akka/current/general/remoting.html
  * - TODO 1:  SUPERVISOR-COORDINATED SHUTDOWN STRATEGY; but SPOF if MASTER SUPERVISOR fails!
  *   HOW to shutdown simply and gracefully; via having Supervisor MESSAGE Children, so they can finish processing their event queues
  *   before final shutdown?
  *   - http://doc.akka.io/api/akka/2.3.0/#akka.actor.SupervisorStrategy
  *   SHUTDOWN (top-down or bottom-up?) of hierarchy via delegating "ActorSystem.terminate",
  *   or "Manager.gracefulStop() with timeout" or via Supervisor's "DeathWatch" or via "Poison Pill"?
  * - TODO 2:  FAULT-TOLERANCE SPOF risk with Master-Parent Actor Lifecycle; then how is that managed to RECONSTITUTE state to
  *   bring up replacement Actor (e.g. snapshot at time first Actor died,
  *   including interim input message queue when first actor unavailable)
  * - What are best-practices for Hierarchy Lifetime Management
  * - TODO 3:  What is default message ordering --  serial-single-threaded, first-one-arriving-wins?
  *             However, with multiple Senders on one Receiver; may not be ordered relative to each Sender's send order
  * - How does this integrate with persistence TO = FROM FLOWs!
  * - TODO 4:  find out best-practices for RECEIVER retries on ACK-TIMEOUT failures,
  *            RECEIVER duplicate-message-checking i.e. with Sequence IDs?
  * - TODO 5:  find out how to integrate with Cassandra distributed UUID generator in a CLUSTER scenario for auto-replication, dynamic scaling, e
  * - TODO 6:  find out fault-tolerance with Akka-Cluster as far as down-Actor detection and
  *            up-replicated-Actor serialization-from-disk ALONG with INTERIM-EVENT capture!
  * - TODO 7:  find out data-modeling for coordination between microservices via foreign-key-UUID for associations!
  * - TODO 8:find out TIMEOUT and RETRY logic!
   *
  */
import akka.actor.{ Actor, ActorRef, Props, ActorSystem }

case class ProcessStringMsg(string: String)
case class StringProcessedMsg(words: Integer)

class StringCounterActor extends Actor {
  def receive = {
    case ProcessStringMsg(string) => {
      val wordsInLine = string.split(" ").length
      sender ! StringProcessedMsg(wordsInLine)
    }
    case _ => println("Error: message not recognized")
  }
}

case class StartProcessFileMsg()

class WordCounterActor(filename: String) extends Actor {

  // TODO 6:  how to TEST this
  // implicit val timeout = Timeout(5 seconds)

  private var running = false
  private var totalLines = 0
  private var linesProcessed = 0
  private var result = 0
  private var fileSender: Option[ActorRef] = None

  def receive = {
    case StartProcessFileMsg() => {
      if (running) {
        // println just used for example purposes;
        // Akka logger should be used instead
        println("Warning: duplicate start message received")
      } else {
        running = true
        fileSender = Some(sender) // save reference to process invoker
        import scala.io.Source._
        fromFile(filename).getLines.foreach { line =>
          context.actorOf(Props[StringCounterActor]) ! ProcessStringMsg(line)
          totalLines += 1
          println("Sent a Line Message for Line: %s".format(line))
        }
      }
    }
    case StringProcessedMsg(words) => {
      result += words
      linesProcessed += 1
      println("Received a Word Count Message with count:  %d".format(words))
      if (linesProcessed == totalLines) {
        fileSender.map(_ ! result) // provide result to process invoker
      }
    }
    case _ => println("message not recognized!")
  }
}

object Sample extends App {

  import akka.util.Timeout
  import scala.concurrent.duration._
  import akka.pattern.ask
  import akka.dispatch.ExecutionContexts._
  implicit val ec = global

  // ATTN:  main API deprecated
  //override def main(args: Array[String]) {
  val system = ActorSystem("System")
  // val actor = system.actorOf(Props(new WordCounterActor(args(0))))
  val actor = system.actorOf(Props(new WordCounterActor("./testdata/quotes.txt")))
  implicit val timeout = Timeout(25 seconds)
  val future = actor ? StartProcessFileMsg()
  future.map { result =>
    println("Total number of words " + result)
    // ATTN:  terminate API deprecated
    // system.shutdown
    system.terminate()
    //}
  }
}
