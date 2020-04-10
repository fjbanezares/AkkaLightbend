package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration._


class Barista(finishCoffeeDuration: FiniteDuration) extends Actor with ActorLogging  {
  import context.dispatcher
  import Barista._
  // self ! CoffeeFinished
  override def receive: Receive = {
    case PrepareCoffee(coffee, guest) =>
      busy(finishCoffeeDuration)
      sender() ! CoffeePrepared(coffee,guest)
//      context.system.scheduler.scheduleOnce(
//        finishCoffeeDuration,
//        sender(),
//        CoffeePrepared(coffee, guest))
  }
 }

object Barista {
  def props(prepareCoffeeDuration: FiniteDuration): Props =
    Props(new Barista(prepareCoffeeDuration))
 // def props: Props = Props(new Guest)
  case class PrepareCoffee(coffee: Coffee, guest: ActorRef)
  case class CoffeePrepared(coffee: Coffee, guest: ActorRef)

}
