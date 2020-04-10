package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Waiter(coffeHouse: ActorRef) extends Actor with ActorLogging{

  import Waiter._

  override def receive: Receive = {

    case Barista.CoffeePrepared(coffeType, guest) => guest ! CoffeeServed(coffeType)
    case ServeCoffee(coffeType) =>  coffeHouse ! CoffeeHouse.ApproveCoffee(coffeType,sender())
      // if we don't specify Barista, it is another Case Class from other Class

  }
}


object Waiter {
  def props(coffeeHouse: ActorRef): Props = Props(new Waiter(coffeeHouse))  // def props: Props = Props(new CoffeeHouse

  // Communications Protocol
  case class CoffeeServed(coffee: Coffee)
  case class ServeCoffee(coffee: Coffee)




}
