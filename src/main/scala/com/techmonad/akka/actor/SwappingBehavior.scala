package com.techmonad.akka.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

class HotSwapActor extends Actor with ActorLogging {

  import context._

  def angry: Receive = {
    case "angry" =>
      log.info("I am already angry")
    case "are you still angry ?" =>
      log.info("yes, I am angry .. ")
    case "happy" =>
      log.info("Swapping angry to happy ")
      become(happy)
  }

  def happy: Receive = {
    case "happy" =>
      log.info("I am already happy")
    case "are you still happy ?" =>
      log.info("yes, I am happy .... ")
    case "angry" =>
      log.info("Swapping happy to angry ")
      become(angry)
  }

  def receive = {
    case "angry" =>
      become(angry)
    case "happy" =>
      become(happy)
  }
}

object SwappingBehaviorApp extends App {

  val system = ActorSystem("SwappingBehaviorApp")

  val hotSwapActor = system.actorOf(Props[HotSwapActor], "hotSwapActor")

  hotSwapActor ! "angry"

  hotSwapActor ! "angry"

  hotSwapActor ! "are you still angry ?"

  hotSwapActor ! "happy"

  hotSwapActor ! "are you still happy ?"


  system.terminate()


}