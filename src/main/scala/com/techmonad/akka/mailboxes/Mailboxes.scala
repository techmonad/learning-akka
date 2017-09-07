package com.techmonad.akka.mailboxes



import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.dispatch._
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.config.{Config, ConfigFactory}


class MyPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedStablePriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      // 'highpriority messages should be treated first if possible
      case 'highpriority => 0

      // 'lowpriority messages should be treated last if possible
      case 'lowpriority  => 2

      // PoisonPill when no other left
      case PoisonPill    => 3

      // We default to 1, which is in between high and low
      case otherwise     => 1
    })

class PriorityMailboxActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)

  self ! 'lowpriority
  self ! 'lowpriority
  self ! 'highpriority
  self ! 'pigdog
  self ! 'pigdog2
  self ! 'pigdog3
  self ! 'highpriority
  self ! PoisonPill

  def receive = {
    case x => log.info(x.toString)
  }
}

object MailboxesApp extends App{

val system = ActorSystem("MailboxesApp", ConfigFactory.load("mailbox"))

  //val priorityMailboxActor = system.actorOf(Props[PriorityMailboxActor].withDispatcher("prio-mailbox") , "priorityMailboxActor")

  val priorityMailboxActor = system.actorOf(Props[PriorityMailboxActor] , "priorityMailboxActor")

}


case object MyControlMessage extends ControlMessage

class ControlAwareMailboxActor extends Actor {

  val log: LoggingAdapter = Logging(context.system, this)

  self ! "foo"
  self ! "bar"
  self ! MyControlMessage
  self ! PoisonPill

  def receive = {
    case x => log.info(x.toString)
  }
}
object ControlAwareMailboxApp extends App {

  val system = ActorSystem("MailboxesApp", ConfigFactory.load("mailbox"))

  val controlAwareMailboxActor = system.actorOf(Props[ControlAwareMailboxActor].withDispatcher("control-aware-mailbox"),"controlAwareMailboxActor" )
}












/*class MyBoundedActor extends Actor with RequiresMessageQueue[BoundedMessageQueueSemantics]{

  override def receive = {
    case msg =>

  }
}*/
