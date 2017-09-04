package com.techmonad.akka.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}


class WorkerActor extends Actor with ActorLogging{
  import WorkerActor._

   def receive = {
     case Task(name) =>
       log.info(s"Doing task $name")

  }

  override def postStop() = {
    log.info("I am dying...... ")
  }
}


object WorkerActor {

  case class Task(name:String)

  def props()= Props[WorkerActor]

}





object PoisonPillApp extends App{

  val system = ActorSystem("PoisonPillApp")
  val workerActor = system.actorOf(WorkerActor.props(),"workerActor")

  import WorkerActor._
  (1 to 10).foreach{ n =>
    workerActor ! Task("task-" + n )
  }
  workerActor ! PoisonPill

  // try to message to actor
  workerActor ! Task("task-0")

  system.terminate()
}
