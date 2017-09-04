package com.techmonad.akka.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

class HelloActor extends Actor with ActorLogging {

  def receive = {
    case msg => log.info(s"Received massage $msg")

  }
}


object HelloApp extends App {

  val system = ActorSystem("actorSystem")
  val helloActor = system.actorOf(Props[HelloActor])
  helloActor ! "hello"
  helloActor ! "hi"

  system.terminate()
}