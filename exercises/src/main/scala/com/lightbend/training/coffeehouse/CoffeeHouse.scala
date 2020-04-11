package com.lightbend.training.coffeehouse

import java.util.concurrent.TimeUnit
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import scala.concurrent.duration._


class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging {

  log.info("pepito   "+caffeineLimit)

  import CoffeeHouse._


  override val supervisorStrategy: SupervisorStrategy = {


    val decider: SupervisorStrategy.Decider = {
      case Guest.CaffeineException  => SupervisorStrategy.Stop
      case Waiter.FrustratedException(coffeeType, guest) => {
        barista.forward(Barista.PrepareCoffee(coffeeType, guest))

        SupervisorStrategy.Restart
      }
    }

    OneForOneStrategy()(decider.orElse(super.supervisorStrategy.decider))
  }


  private val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration",
      TimeUnit.MILLISECONDS).millis

  //private val finishCoffeeDuration: FiniteDuration = FiniteDuration(context.system.settings.config.getLong("coffee-house.guest.finish-coffee-duration"),scala.concurrent.duration.MILLISECONDS)
  private val finishCoffeeDuration: FiniteDuration = context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", TimeUnit.MILLISECONDS).millis
  private var guestBook: Map[ActorRef, Int] = Map.empty.withDefaultValue(0)

  private val accuracy = context.system.settings.config.getInt("coffee-house.barista.accuracy")
  private val maxComplaintCount  = context.system.settings.config.getInt("coffee-house.waiter.max-complaint-count")
  private val barista: ActorRef = createBarista()
  private val waiter: ActorRef = createWaiter()


  override def receive: Receive = {
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
      barista.tell(Barista.PrepareCoffee(coffee, guest),context.sender())
      log.info(s"Guestbook $guestBook total limit is $caffeineLimit")



    //guestBook.updated(guest,guestBook(guest)+1)

    case CoffeeHouse.ApproveCoffee(coffee, guest) if guestBook(guest) == caffeineLimit =>
      context.stop(guest)
      log.info(s"Sorry, $guest, but you have reached your limit.")


    case a: Any => log.info("Esta señal no mola..." + a.toString)
  }

  protected def createGuest(favouriteCoffee: Coffee, caffeineLimit: Int): ActorRef = context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration, caffeineLimit))

  override def preStart(): Unit = {
    super.preStart()
    log.debug("CoffeHouse Open")
  }

  protected def createWaiter(): ActorRef = context.actorOf(Waiter.props(self,barista,maxComplaintCount), "waiter")

  protected def createBarista(): ActorRef = context.actorOf(Barista.props(prepareCoffeeDuration, accuracy), "barista")


}


object CoffeeHouse {
  def props(caffeineLimit: Int): Props = Props(new CoffeeHouse(caffeineLimit)) // def props: Props = Props(new CoffeeHouse

  // Communications Protocol
  case class CreateGuest(favCoffee: Coffee, caffeineLimit: Int)

  case class ApproveCoffee(favCoffee: Coffee, guest: ActorRef)


}
