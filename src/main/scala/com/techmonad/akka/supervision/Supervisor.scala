package com.techmonad.akka.supervision

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class Supervisor extends Actor with ActorLogging {

  override val supervisorStrategy: OneForOneStrategy =
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

class Child extends Actor with ActorLogging {

  var state = 0

  def receive = {
    case ex: Exception => throw ex
    case x: Int => state = x
    case "get" => sender() ! state
  }

  override def preStart(): Unit = {
    log.info("Initializing actor resources  ........ ")
    super.preStart()
  }

  override def postStop(): Unit = {
    log.info("Releasing actor resources ........ ")
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("Pre restart ........ ")
    super.preRestart(reason, message)
  }


  override def postRestart(reason: Throwable): Unit = {
    log.info("Post restart ........ ")
    super.postRestart(reason)
  }

}


object SupervisionApp extends App {

  val system = ActorSystem("SupervisionApp")

  val log = Logging(system, getClass)

  implicit val timeout = Timeout(1 second)

  val supervisor = system.actorOf(Props[Supervisor], "supervisor")

  val child = blocking {
    (supervisor ? Props[Child]).mapTo[ActorRef]
  }

  child ! 1

  val currentState = blocking {
    (child ? "get").mapTo[Int]
  }

  log.info("Current State " + currentState)

  child ! new ArithmeticException()

  val afterArithmeticExceptionCurrentState = blocking {
    (child ? "get").mapTo[Int]
  }
  log.info("after Arithmetic Exception Current State " + afterArithmeticExceptionCurrentState)

  child ! new NullPointerException()

  val afterNullPointerExceptionCurrentState = blocking {
    (child ? "get").mapTo[Int]
  }

  log.info("after NullPointer Exception Current State " + afterNullPointerExceptionCurrentState)

  system.terminate()


  /**
    * Never use on production
    * this is only for demo and testing
    */
  def blocking[T](f: Future[T]): T =
    Await.result((f), Duration.Inf)

}