package com.techmonad.akka.ask

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
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


object WordCountApp extends App {

  val system = ActorSystem("WordCountApp")

  implicit val timeout = Timeout(1 second)

  val wordCountActor = system.actorOf(WordCountActor.props, "wordCountActor")

  import WordCountActor._
  import system.dispatcher

  val wordCountResult: Future[WordCountResult] = ask(wordCountActor, GetWordCount("Hello Hello I am here")).mapTo[WordCountResult]
  //or
  // val wordCountResult: Future[WordCountResult] = (wordCountActor ? GetWordCount("Hello Hello I am here")).mapTo[WordCountResult]

  wordCountResult onComplete {
    case Success(result) =>
      println("wordCount:  " + result.wordCount)
      system.terminate()
    case Failure(th) =>
      th.printStackTrace()
      system.terminate()
  }
}