package com.techmonad.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}

import scala.concurrent.duration._

case object Ping

case object Pong

class Pinger extends Actor with ActorLogging {

  var countDown = 10

  def receive = {
    case Pong =>
      log.info(s"${self.path} received pong, count down $countDown")

      if (countDown > 0) {
        countDown -= 1
        sender() ! Ping
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
  }
}

class Ponger(pinger: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case Ping =>
      log.info(s"${self.path} received ping")
      pinger ! Pong
  }
}

object PingPongApp extends App {

  val system = ActorSystem("PingPong")

  val pinger = system.actorOf(Props[Pinger], "pinger")

  val ponger = system.actorOf(Props(classOf[Ponger], pinger), "ponger")

  import system.dispatcher

  system.scheduler.scheduleOnce(500 millis) {
    ponger ! Ping
  }

  system.terminate()

}