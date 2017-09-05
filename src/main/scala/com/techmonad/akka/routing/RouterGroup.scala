package com.techmonad.akka.routing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.{FromConfig, RandomGroup, RoundRobinGroup}
import com.typesafe.config.ConfigFactory


class WorkerActor extends Actor with ActorLogging {

  override def receive = {
    case n: Int =>
      val result = factorial(n)
      log.info(s"factorial of $n is $result by: [$self]")

  }

  def factorial(n: Int): BigInt = {
    def factorial(n: Int, acc: BigInt): BigInt = {
      if (n <= 1) acc else factorial(n - 1, acc * n)
    }

    factorial(n, 1)
  }
}


/**
  * RoundRobinGroup
  */
object RoundRobinGroupApp extends App {

  val system = ActorSystem("RoundRobinGroupApp", ConfigFactory.load("group-routing"))

  val worker1 = system.actorOf(Props[WorkerActor], "w1")
  val worker2 = system.actorOf(Props[WorkerActor], "w2")
  val worker3 = system.actorOf(Props[WorkerActor], "w3")
 // val paths = List("/user/w1", "/user/w2", "/user/w3")*/

// val workerRoundRobinGroupRouter = system.actorOf(RoundRobinGroup(paths).props(), "workerRoundRobinGroupRouter")

  //List.fill(1000)(13) foreach { n => workerRoundRobinGroupRouter ! n }*/

  //or by config
   val workerRoundRobinGroupRouter = system.actorOf(FromConfig.props(), "workerRoundRobinGroupRouter")

    List.fill(1000)(13) foreach { n => workerRoundRobinGroupRouter ! n }
}



/**
  * RandomGroup
  */
object RandomGroupApp extends App {

  val system = ActorSystem("RandomGroupApp", ConfigFactory.load("group-routing"))

    val worker1 = system.actorOf(Props[WorkerActor], "w1")
    val worker2 = system.actorOf(Props[WorkerActor], "w2")
    val worker3 = system.actorOf(Props[WorkerActor], "w3")
    val paths = List("/user/w1", "/user/w2", "/user/w3")

    val workerRandomGroupRouter = system.actorOf(RandomGroup(paths).props(), "workerRandomGroupRouter")

    List.fill(1000)(13) foreach { n => workerRandomGroupRouter ! n }


}
