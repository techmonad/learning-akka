package com.techmonad.akka.dispatchers

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util._

class WordCountActor extends Actor {

  import WordCountActor._

  def receive: Receive = {
    case GetWordCount(str) =>
      val wordCount: Map[String, Int] = str.split(" ").groupBy { word => word }.map { case (word, frequency) => (word, frequency.length) }
      sender() ! WordCountResult(wordCount)
  }
}

object WordCountActor {

  def props: Props = Props[WordCountActor]

  case class GetWordCount(str: String)

  case class WordCountResult(wordCount: Map[String, Int])

}


object DispatchersWithCallbackApp extends App {

  val system = ActorSystem("DispatchersWithCallbackApp", ConfigFactory.load("dispatcher"))

  implicit val timeout = Timeout(1 second)

  val wordCountActor = system.actorOf(WordCountActor.props, "wordCountActor")

  import WordCountActor._

  val wordCountResult: Future[WordCountResult] = (wordCountActor ? GetWordCount("Hello Hello I am here")).mapTo[WordCountResult]

  //import system.dispatcher

  implicit val executionContext = system.dispatchers.lookup("my-dispatcher")

  wordCountResult onComplete {
    case Success(result) =>
      println("Current Thread " + Thread.currentThread().getName)
      println("wordCount:  " + result.wordCount)
      system.terminate()
    case Failure(th) =>
      th.printStackTrace()
      system.terminate()
  }

}


object ActorWithDispatchersApp extends App {

  val system = ActorSystem("ActorWithDispatchersApp", ConfigFactory.load("dispatcher"))

  implicit val timeout = Timeout(1 second)

  //inject your custom dispatchers
  val wordCountActor = system.actorOf(WordCountActor.props.withDispatcher("my-dispatcher"), "wordCountActor")

  //define dispatchers into config file
  // val wordCountActor = system.actorOf(WordCountActor.props, "wordCountActor2")


  import WordCountActor._

  val wordCountResult: Future[WordCountResult] = (wordCountActor ? GetWordCount("Hello Hello I am here")).mapTo[WordCountResult]


  import system.dispatcher

  wordCountResult onComplete {
    case Success(result) =>
      println("wordCount:  " + result.wordCount)
      system.terminate()
    case Failure(th) =>
      th.printStackTrace()
      system.terminate()
  }

}

/**
  * There are 3 different types of message dispatchers:
  * 1) Dispatcher(Default dispatcher, Bulkheading)
  * 2) PinnedDispatcher(Bulkheading and high availability )
  * 3) CallingThreadDispatcher(For testing)
  *
  */
object DispatchersApp extends App {


  val system = ActorSystem("ActorWithDispatchersApp", ConfigFactory.load("dispatcher"))

  implicit val timeout = Timeout(1 second)

  //inject PinnedDispatcher dispatchers
  val wordCountActor = system.actorOf(WordCountActor.props.withDispatcher("my-pinned-dispatcher"), "wordCountActor")


  import WordCountActor._

  val wordCountResult: Future[WordCountResult] = (wordCountActor ? GetWordCount("Hello Hello I am here")).mapTo[WordCountResult]


  import system.dispatcher

  wordCountResult onComplete {
    case Success(result) =>
      println("wordCount:  " + result.wordCount)
      system.terminate()
    case Failure(th) =>
      th.printStackTrace()
      system.terminate()
  }

}

class BlockingActor extends Actor {
  def receive = {
    case i: Int =>
      Thread.sleep(5000) //block for 5 seconds, representing blocking I/O, etc
      println(s"Blocking operation finished: ${i}")
  }
}

object BlockingInSideActorApp extends App{

  val system = ActorSystem("ActorWithDispatchersApp", ConfigFactory.load("dispatcher"))

  val blockingActor = system.actorOf(Props[BlockingActor], "blockingActor")

  1 to 100 foreach(blockingActor ! _)

}



class BlockingFutureActor extends Actor {

     // Don't use this dispatcher because the blocking operations will dominate the entire dispatcher.
  //implicit val executionContext: ExecutionContext = context.dispatcher

   //use dedicated dispatcher for blocking operations
  implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("blocking-io-dispatcher")


  def receive = {

    case i: Int =>
      println(s"Calling blocking Future: ${i}")
      Future {
        Thread.sleep(5000) //block for 5 seconds
        println(s"Blocking future finished ${i}")
      }

  }
}

object HandlingBlockingInSideActorApp extends App{

  val system = ActorSystem("ActorWithDispatchersApp", ConfigFactory.load("dispatcher"))

  val blockingFutureActor = system.actorOf(Props[BlockingFutureActor], "blockingFutureActor")
  1 to 100 foreach(blockingFutureActor ! _)
}