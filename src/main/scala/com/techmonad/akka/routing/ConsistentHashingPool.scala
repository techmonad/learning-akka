package com.techmonad.akka.routing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.ask


class Cache extends Actor {
  import Cache._

  var cache = Map.empty[String, String]

  def receive = {
    case Entry(key, value) => cache += (key -> value)
    case Get(key)          => sender() ! cache.get(key)
    case Evict(key)        => cache -= key
  }
}

object Cache {

  final case class Evict(key: String)

  final case class Get(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }

  final case class Entry(key: String, value: String)

}

object ConsistentHashingPoolApp extends App{

  import Cache._

  val system = ActorSystem("ConsistentHashingPoolApp")
  val log = Logging(system, getClass)

  implicit val timeout = Timeout(1 second)

  def hashMapping: ConsistentHashMapping = {
    case Evict(key) => key
  }

  val cache: ActorRef = system.actorOf(ConsistentHashingPool(10, hashMapping = hashMapping).props(Props[Cache]), name = "cacheRouter")

  cache ! ConsistentHashableEnvelope(    message = Entry("hello", "HELLO"), hashKey = "hello")
  cache ! ConsistentHashableEnvelope(    message = Entry("hi", "HI"), hashKey = "hi")

 val responseForHello =  blocking((cache ? Get("hello")).mapTo[Option[String]])
  log.info(s"Response for hello : [$responseForHello] ")


  val responseForHi =  blocking((cache ? Get("hi")).mapTo[Option[String]])
  log.info(s"Response for hi : [$responseForHi] ")


  cache ! Evict("hi")

  val responseForHiAfterEviction =  blocking((cache ? Get("hi")).mapTo[Option[String]])
  log.info(s"Response for hi after eviction : [$responseForHiAfterEviction] ")



  system.terminate()
  /**
    * Never use on production
    * this is only for demo and testing
    */
  def blocking[T](f: Future[T]): T =
    Await.result((f), Duration.Inf)

}
