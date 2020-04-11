package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props, Timers}

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

//        case Waiter.CoffeeServed(`favouriteCoffee`) =>
//          coffeeCount += 1
//          log.info(s"Enjoying my $coffeeCount yummy $favouriteCoffee!")
//          timers.startSingleTimer("pepito", Guest.CoffeeFinished, finishCoffeeDuration )
//        case Waiter.CoffeeServed(otherCoffee) =>
//          log.info(s"Expected a $favouriteCoffee, but got a $otherCoffee!")
//          waiter ! Waiter.Complaint(favouriteCoffee)


            case Waiter.CoffeeServed(c) =>

      if (c == favouriteCoffee) {
        coffeeCount += 1
        log.info(s"Enjoying my $coffeeCount yummy $c!")
        timers.startSingleTimer("pepito", Guest.CoffeeFinished, finishCoffeeDuration )
      } else {
        log.info(s"Expected a $favouriteCoffee, but got a $c! reply to $waiter and $sender()")
        waiter ! Waiter.Complaint(favouriteCoffee)
      }


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
