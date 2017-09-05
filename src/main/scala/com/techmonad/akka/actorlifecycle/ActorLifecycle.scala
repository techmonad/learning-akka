package com.techmonad.akka.actorlifecycle

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

class ActorWithLifecycleMethods extends Actor with ActorLogging {


  override def receive = {
    case msg => log.info(s"Received massage $msg")
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

object ActorWithLifecycleMethodsApp extends App {

  val system = ActorSystem("ActorWithLifecycleMethodsApp")

  val actor = system.actorOf(Props[ActorWithLifecycleMethods])

  actor ! "Hello"


  actor ! PoisonPill

  //system.terminate()


}

/**
  *
  * The remaining visible methods are user-overridable life-cycle hooks which are described in the following:
  * def preStart(): Unit = ()
  * *
  * def postStop(): Unit = ()
  * *
  * def preRestart(reason: Throwable, message: Option[Any]): Unit = {
  *context.children foreach { child ⇒
  *context.unwatch(child)
  *context.stop(child)
  * }
  * postStop()
  * }
  * *
  * def postRestart(reason: Throwable): Unit = {
  * preStart()
  * }
  * The implementations shown above are the defaults provided by the Actor trait.
  */