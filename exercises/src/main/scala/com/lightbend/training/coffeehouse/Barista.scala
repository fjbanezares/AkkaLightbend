package com.lightbend.training.coffeehouse

import scala.util.Random

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration._


class Barista(finishCoffeeDuration: FiniteDuration, accuracy: Int) extends Actor with ActorLogging  {


  import Barista._
  // self ! CoffeeFinished
  override def receive: Receive = {
    case PrepareCoffee(coffee, guest) =>

      busy(finishCoffeeDuration)
      sender() ! CoffeePrepared(pickCoffee(coffee),guest)

    //      if (Math.random()*100.intValue() < accuracy) {
//        sender() ! CoffeePrepared(coffee,guest)
//      } else {
//        sender() ! CoffeePrepared(Coffee.anyOther(coffee),guest)
//      }
//      context.system.scheduler.scheduleOnce(
//        finishCoffeeDuration,
//        sender(),
//        CoffeePrepared(coffee, guest))
  }

  private def pickCoffee(coffee: Coffee): Coffee = {
    if (Random.nextInt(100)<accuracy) coffee else Coffee.anyOther(coffee)
  }
 }

object Barista {
  def props(prepareCoffeeDuration: FiniteDuration, accuracy: Int): Props =
    Props(new Barista(prepareCoffeeDuration, accuracy))
 // def props: Props = Props(new Guest)
  case class PrepareCoffee(coffee: Coffee, guest: ActorRef)
  case class CoffeePrepared(coffee: Coffee, guest: ActorRef)

}
