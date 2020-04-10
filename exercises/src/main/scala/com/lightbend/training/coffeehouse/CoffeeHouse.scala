package com.lightbend.training.coffeehouse

import java.util.concurrent.TimeUnit
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import scala.concurrent.duration._


class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging {

  log.info("pepito   "+caffeineLimit)

  import CoffeeHouse._

  private val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration",
      TimeUnit.MILLISECONDS).millis

  private val barista: ActorRef = createBarista()
  private val waiter: ActorRef = createWaiter()
  //private val finishCoffeeDuration: FiniteDuration = FiniteDuration(context.system.settings.config.getLong("coffee-house.guest.finish-coffee-duration"),scala.concurrent.duration.MILLISECONDS)
  private val finishCoffeeDuration: FiniteDuration = context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", TimeUnit.MILLISECONDS).millis
  private var guestBook: Map[ActorRef, Int] = Map.empty.withDefaultValue(0)

  override def receive: Receive = {
    case s: String => sender() ! s
    case Terminated(guest) =>
      guestBook = guestBook.removed(guest)
      log.info(s"Thanks $guest, for being our guest!")
    case CreateGuest(f, limit) =>
      val newGuest = createGuest(f, limit)
      guestBook += newGuest -> 0
      log.info(s"Guest $newGuest added to guest book")
      context.watch(newGuest)

    case CoffeeHouse.ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented.")
      barista.forward(Barista.PrepareCoffee(coffee, guest))



    //guestBook.updated(guest,guestBook(guest)+1)

    case CoffeeHouse.ApproveCoffee(coffee, guest) if guestBook(guest) == caffeineLimit =>
      log.info(s"Sorry, $guestBook being $caffeineLimit")
      context.stop(guest)
      log.info(s"Sorry, $guest, but you have reached your limit.")


    case a: Any => log.info("Esta señal no mola..." + a.toString)
  }

  protected def createGuest(favouriteCoffee: Coffee, caffeineLimit: Int): ActorRef = context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration, caffeineLimit))

  override def preStart(): Unit = {
    super.preStart();
    log.debug("CoffeHouse Open")
  }

  protected def createWaiter(): ActorRef = context.actorOf(Waiter.props(self), "waiter")

  protected def createBarista(): ActorRef = context.actorOf(Barista.props(finishCoffeeDuration), "barista")


}


object CoffeeHouse {
  def props(caffeineLimit: Int): Props = Props(new CoffeeHouse(caffeineLimit)) // def props: Props = Props(new CoffeeHouse

  // Communications Protocol
  case class CreateGuest(favCoffee: Coffee, caffeineLimit: Int)

  case class ApproveCoffee(favCoffee: Coffee, guest: ActorRef)


}
