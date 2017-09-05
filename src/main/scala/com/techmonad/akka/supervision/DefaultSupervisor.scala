package com.techmonad.akka.supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}
import scala.concurrent.duration._


class WorkerActor extends Actor with ActorLogging{
  import WorkerActor._

  override def receive = {

    case SubTask(id) =>
      log.info(s"Processing subtask  $id [actor: $self]")
  }
}

object WorkerActor{

  case class SubTask(id: Int)

  def props = Props[WorkerActor]

}

class ManagerActor extends Actor with ActorLogging{

import ManagerActor._
  import WorkerActor._


  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException =>        Resume
      case _: NullPointerException =>        Restart
      case _: IllegalArgumentException =>        Stop
      case _: Exception =>        Escalate
    }

  var workers : List[ActorRef] = Nil

  override def receive = {
    case Task(id, totalSubTask) =>
      log.info("Processing task  " + id)
      (0 until totalSubTask).foreach(n => workers(n % workers.length) ! SubTask(n) )

  }

  override def preStart(): Unit = {
    log.info("Initializing actor resources  ........ ")
    workers = (1 to 10).toList.map {  n =>
      context.actorOf(WorkerActor.props, s"worker-$n")
    }

  }

}


object ManagerActor{

  case class Task(id:Int, totalSubTask: Int)

  def props = Props[ManagerActor]
}


object DefaultSupervisorApp extends App{

  val system = ActorSystem("DefaultSupervisorApp")

  val managerActor = system.actorOf(ManagerActor.props, "managerActor")

  managerActor ! ManagerActor.Task(1, 50)

  //system.terminate()


}