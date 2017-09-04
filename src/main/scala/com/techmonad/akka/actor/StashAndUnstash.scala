package com.techmonad.akka.actor


import akka.actor.{Actor, ActorSystem, Props, Stash}

class ActorWithProtocol extends Actor with Stash {

  def receive = {
    case "open" =>
      unstashAll()
      context.become({
        case "write" => // do writing...
        case "close" =>
          unstashAll()
          context.unbecome()
        case msg => stash()
      }, discardOld = false) // stack on top instead of replacing
    case msg => stash()
  }
}

object ActorWithProtocolApp extends App {

  val system = ActorSystem("ActorWithProtocolSystem")

  val show = system.actorOf(Props[ActorWithProtocol])

  show ! "write"

  show ! "write"

  show ! "write"

  show ! "open"

  show ! "open"

  show ! "close"

  show ! "write"

}

