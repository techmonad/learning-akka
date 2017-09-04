package com.techmonad.akka.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

class MyActor extends Actor with ActorLogging {
  import MyActor._

  def receive = {
    case Greeting(greeter) => log.info(s"I was greeted by $greeter.")
    case Goodbye           => log.info("Someone said goodbye to me.")
  }
}

/**
  * Recommended Practices: Put messages and props inside actor companion object
  */
object MyActor {

  case class Greeting(from: String)
  case object Goodbye
  def props = Props[MyActor]
}


object CompanionPatternApp extends App{

  val system = ActorSystem("CompanionPatternApp")

  val myActor = system.actorOf(MyActor.props, "myActor")

  import MyActor._

  myActor ! Greeting("Sky")
  myActor ! Goodbye

  system.terminate()

}