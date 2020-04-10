package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, Props}

class CoffeeHouse extends Actor with ActorLogging{
  override def receive: Receive = {case _ => log.info("Coffee Brewing")}

  override def preStart(): Unit = {super.preStart(); log.debug("CoffeHouse Open")}


  }


object CoffeeHouse {
  def props(): Props = Props(classOf[CoffeeHouse])

 // def props: Props = Props(new CoffeeHouse

}
