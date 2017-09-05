package com.techmonad.akka.supervision

package com.techmonad.akka.supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, Terminated}

import scala.concurrent.duration._


class WorkerActor extends Actor with ActorLogging {

  import WorkerActor._

  override def receive = {

    case SubTask(id) =>
      log.info(s"Processing subtask  $id [actor: $self]")
  }
}

object WorkerActor {

  def props = Props[WorkerActor]

  case class SubTask(id: Int)

}

class ManagerActor extends Actor with ActorLogging {

  import ManagerActor._
  import WorkerActor._


  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException => Resume
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }

  var workers: List[ActorRef] = Nil

  override def receive = {
    case Task(id, totalSubTask) =>
      log.info("Processing task  " + id)
      (0 until totalSubTask).foreach(n => workers(n % workers.length) ! SubTask(n))

    case Done =>
      workers.foreach {
        _ ! PoisonPill
      }

    case Terminated(deadWorker) =>
      log.info(s"Work $deadWorker is dead")
        workers = workers.filter(worker => worker != deadWorker)
      if (workers.isEmpty)
        context.system.terminate()
  }

  override def preStart(): Unit = {
    log.info("Initializing actor resources  ........ ")
    workers = (1 to 10).toList.map { n =>
      val worker = context.actorOf(WorkerActor.props, s"worker-$n")
      context.watch(worker)
      worker
    }

  }

}


object ManagerActor {

  def props = Props[ManagerActor]

  case class Task(id: Int, totalSubTask: Int)

  case object Done
}


object DeathWatcherApp extends App {

  val system = ActorSystem("DeathWatcherApp")

  val managerActor = system.actorOf(ManagerActor.props, "managerActor")

  managerActor ! ManagerActor.Task(1, 50)

  managerActor ! ManagerActor.Done


}