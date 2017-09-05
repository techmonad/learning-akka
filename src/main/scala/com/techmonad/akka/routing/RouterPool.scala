package com.techmonad.akka.routing

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.routing._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * http://doc.akka.io/docs/akka/current/scala/routing.html
  *
  * akka.routing.RoundRobinRoutingLogic
  *akka.routing.RandomRoutingLogic
  *akka.routing.SmallestMailboxRoutingLogic
  *akka.routing.BroadcastRoutingLogic
  *akka.routing.ScatterGatherFirstCompletedRoutingLogic
  *akka.routing.TailChoppingRoutingLogic
  *akka.routing.ConsistentHashingRoutingLogic
  */


class Worker extends Actor with ActorLogging {

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


object RoundRobinPoolApp extends App {

  val system = ActorSystem("RoundRobinPoolApp", ConfigFactory.load("pool-routing"))

  /**
    * RoundRobinPool
    */
  //val workerRoundRobinRouter = system.actorOf(RoundRobinPool(10).props(Props[Worker]), "workerRoundRobinRouter")

  //or by config
  val workerRoundRobinPoolRouter = system.actorOf(FromConfig.props(Props[Worker]), "workerRoundRobinPoolRouter")

  List.fill(1000)(13) foreach { n => workerRoundRobinPoolRouter ! n }

}



object RandomPoolApp extends App {

  val system = ActorSystem("RandomPoolApp", ConfigFactory.load("pool-routing"))


  val workerRandomPoolRouter = system.actorOf(RandomPool(10).props(Props[Worker]), "workerRandomPoolRouter")



  List.fill(1000)(13) foreach { n => workerRandomPoolRouter ! n }

}

  object SmallestMailboxPoolApp extends App {

  val system = ActorSystem("SmallestMailboxPoolApp")

  val workerSmallestMailboxRouter = system.actorOf(SmallestMailboxPool(5).props(Props[Worker]), "workerSmallestMailboxRouter")

  List.fill(1000)(13) foreach { n => workerSmallestMailboxRouter ! n }


}

object BalancingPoolApp extends App {

  val system = ActorSystem("BalancingPoolApp")

  val workerBalancingRouter = system.actorOf(BalancingPool(10).props(Props[Worker]), "workerBalancingRouter")

  List.fill(1000)(13) foreach { n => workerBalancingRouter ! n }

}

class HelloActor extends Actor with ActorLogging {

  def receive = {
    case msg => log.info(s"Received massage $msg [actor : $self]")

  }
}


object BroadcastPoolApp extends App {

  val system = ActorSystem("BroadcastPoolApp")

  val workerBroadcastRouter = system.actorOf(BroadcastPool(10).props(Props[HelloActor]), "workerBroadcastRouter")

  workerBroadcastRouter ! "Hello All"

}


class SenderActor(router: ActorRef) extends Actor with ActorLogging {

  import SenderActor._

  override def receive = {
    case Request(n) =>
      router ! n
    case response =>
      log.info(s"factorial  is $response by: [$sender]")
  }
}

object SenderActor {

  def props(router: ActorRef) = Props(classOf[SenderActor], router)

  case class Request(n: Int)
}


class ReceiverActor extends Actor with ActorLogging {

  override def receive = {
    case n: Int =>
      log.info(s"Getting factorial for $n by:[$sender]")
      sender() ! factorial(n)

  }

  def factorial(n: Int): BigInt = {
    def factorial(n: Int, acc: BigInt): BigInt = {
      if (n <= 1) acc else factorial(n - 1, acc * n)
    }

    factorial(n, 1)
  }
}

object ScatterGatherFirstCompletedPoolApp extends App {

  val system = ActorSystem("ScatterGatherFirstCompletedPoolApp")

  val workerScatterGatherFirstCompletedRouter = system.actorOf(ScatterGatherFirstCompletedPool(10, within = 1 seconds).props(Props[ReceiverActor]), "workerScatterGatherFirstCompletedRouter")

  val senderActor = system.actorOf(SenderActor.props(workerScatterGatherFirstCompletedRouter), "senderActor")

  senderActor ! SenderActor.Request(10)

}

/**
  * TailChoppingPool
 */
object TailChoppingPoolApp extends App{

  val system = ActorSystem("TailChoppingPoolApp")

  val workerTailChoppingPoolRouter = system.actorOf(TailChoppingPool(10, within = 1 seconds, interval = 20 millis).props(Props[ReceiverActor]), "workerTailChoppingPoolRouter")

  val senderActor = system.actorOf(SenderActor.props(workerTailChoppingPoolRouter), "senderActor")

  senderActor ! SenderActor.Request(10)
}