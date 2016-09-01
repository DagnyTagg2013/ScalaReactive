package actors

/**
  * Created on 9/1/16.
  *
  * INSPIRED BY:
  * - https://www.toptal.com/scala/concurrency-and-fault-tolerance-made-easy-an-intro-to-akka
  * - https://gist.github.com/Diego81/9887105
  *
  * ADAPTED to work with Akka v2.4.9
  * - http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  *
  * GO READ THIS for info on ActorSystem, Context, Props, etc:
  * - http://doc.akka.io/docs/akka/2.4.9/general/actor-systems.html#id1
  * - http://doc.akka.io/docs/akka/current/scala/actors.html
  *
  * TODOs
  * - find out best-practices for sender retries, receiver duplicate-message-checking i.e. with Sequence IDs?
  * - suspect best-practice is for parent to message children to shutdown to prevent mid-flow-processing errors
  * - find out best-practices for handling parent-master SPF
  * - find out operation of Akka-Cluster as far as down-Actor detection and up-replicated-Actor creation
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
        }
      }
    }
    case StringProcessedMsg(words) => {
      result += words
      linesProcessed += 1
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

  // API deprecated
  //override def main(args: Array[String]) {
  val system = ActorSystem("System")
  val actor = system.actorOf(Props(new WordCounterActor(args(0))))
  implicit val timeout = Timeout(25 seconds)
  val future = actor ? StartProcessFileMsg()
  future.map { result =>
    println("Total number of words " + result)
    // API deprecated
    // system.shutdown
    system.terminate()
    //}
  }
}
