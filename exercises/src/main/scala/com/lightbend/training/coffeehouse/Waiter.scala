package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Waiter(coffeHouse: ActorRef, barista: ActorRef, maxComplaintCount: Int) extends Actor with ActorLogging{

//  override def postRestart(reason: Throwable): Unit =  reason match {
//    case Waiter.FrustratedException(coffee, guest) =>
//      barista ! Barista.PrepareCoffee(coffee, guest)
//      super.postRestart(reason)
//    case _ => super.postRestart(reason)
//  }

  import Waiter._

  var numComplains: Int = 0

  override def receive: Receive = {

    case Barista.CoffeePrepared(coffeType, guest) => guest ! CoffeeServed(coffeType)
    case Waiter.Complaint(coffee) => {
      numComplains += 1
      log.info(s"the number of complaints is $numComplains out of a $maxComplaintCount")
      if (numComplains <= maxComplaintCount) {
        barista ! Barista.PrepareCoffee(coffee,sender())
      } else {
        throw Waiter.FrustratedException(coffee, sender() )
      }
    }
    case ServeCoffee(coffeType) =>  coffeHouse ! CoffeeHouse.ApproveCoffee(coffeType,sender())
      // if we don't specify Barista, it is another Case Class from other Class

  }
}


object Waiter {
  def props(coffeeHouse: ActorRef, barista: ActorRef, maxComplains: Int): Props = Props(new Waiter(coffeeHouse, barista, maxComplains))  // def props: Props = Props(new CoffeeHouse

  // Communications Protocol
  case class CoffeeServed(coffee: Coffee)
  case class ServeCoffee(coffee: Coffee)
  case class Complaint(coffee: Coffee)
  case class FrustratedException(coffee: Coffee, guest: ActorRef) extends IllegalStateException("Too many Complaints")




}
