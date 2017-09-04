package com.techmonad.akka.supervision

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}

import scala.concurrent.duration._


class Supervisor extends Actor with ActorLogging {
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case arithmeticEx: ArithmeticException =>
        log.error("Error occurred and resuming...  ", arithmeticEx)
        Resume
      case nullPointerEx: NullPointerException =>
        log.error("Error occurred and restarting...  ", nullPointerEx)
        Restart
      case illegalArgumentEx: IllegalArgumentException =>
        log.error("Error occurred and stopping ...  ", illegalArgumentEx)
        Stop
      case ex: Exception =>
        log.error("Error occurred and escalating ...  ", ex)
        Escalate
    }


  def receive: Receive = {
    case props: Props =>

      sender ! context.actorOf(props)
  }

}

class Child extends Actor {
  var state = 0
  def receive = {
    case ex: Exception => throw ex
    case x: Int        => state = x
    case "get"         => sender() ! state
  }
}


object SupervisionApp extends App {

val system = ActorSystem("SupervisionApp")

  val supervisor = system.actorOf(Props[Supervisor], "supervisor")

  supervisor ! Props[Child]
  val child = expectMsgType[ActorRef] // retrieve answer from TestKit’s testActor
}