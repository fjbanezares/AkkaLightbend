package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.actor.Timers

import scala.concurrent.duration._

class Guest(waiter: ActorRef, favouriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration, caffeineLimit: Int) extends Actor with ActorLogging with Timers {


  override def postStop(): Unit = {
    log.info("Googbye!")


  }


  import Guest._
 // self ! CoffeeFinished
 orderCoffee()
  private var coffeeCount: Int = 0
  override def receive: Receive = {
    case Waiter.CoffeeServed(c) =>
      coffeeCount += 1
      log.info(s"Enjoying my $coffeeCount yummy $c!")
      timers.startSingleTimer("pepito", Guest.CoffeeFinished, finishCoffeeDuration )

    case Guest.CoffeeFinished if coffeeCount <= caffeineLimit => orderCoffee()
    case Guest.CoffeeFinished if coffeeCount > caffeineLimit => throw CaffeineException

  }

  private def orderCoffee(): Unit = waiter ! Waiter.ServeCoffee(favouriteCoffee)
 }

object Guest {
  def props(waiter: ActorRef, favouriteCoffee: Coffee, finishCoffeeDuration: FiniteDuration, caffeineLimit: Int): Props =
    Props(new Guest(waiter,favouriteCoffee, finishCoffeeDuration, caffeineLimit))
 // def props: Props = Props(new Guest)
  case object CoffeeFinished
  case object CaffeineException extends IllegalStateException
}
