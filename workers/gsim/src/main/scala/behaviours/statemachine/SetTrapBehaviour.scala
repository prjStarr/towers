package behaviours.statemachine

import behaviours.BuildConstants._
import behaviours.ItsATrap
import behaviours.agent.{SleepInterface, TargetingInterface, BomberInterface, NavigationInterface}
import demoteam.{Geometry, Bomber, Targeting}
import improbable.papi.entity.Entity
import improbable.papi.world.World

import behaviours.agent.TargetingBehaviour._
import SetTrapBehaviour._

class SetTrapBehaviour(entity: Entity, world: World, nav: NavigationInterface, bomber: BomberInterface, sleepInterface: SleepInterface) extends StateMachineBehaviourBase(entity, world, nav) {

  override def updateAction(): Unit = {
    // make cube explosive!
    entity.watch[Geometry].geometryType.foreach {
      geo =>
        val targ = entity.watch[Targeting]
        targ.targetId.flatten.foreach(targId => world.messaging.sendToEntity(targId, ItsATrap(geo)))
    }

    // stop bombing
    bomber.clear()

    // make sure we don't accidentally pick up our own bomb
    sleepInterface.forTime(timeToSleepSoWeDontRetargetOurOwnTrap)
  }
}

object SetTrapBehaviour {

  val timeToSleepSoWeDontRetargetOurOwnTrap = 5.0

  def square[T](a: T)(implicit num: Numeric[T]): T = {
    import num._
    a * a
  }

  def staticShouldRun(entity: Entity, world: World): Boolean = {
    val bomberTimer = entity.watch[Bomber].bomberTimer.filter(_ > 0.0)
    val targets = bomberTimer.flatMap ( _ => {
        val targ = entity.watch[Targeting]
        for (cubeId <- targ.targetId.flatten; towerId <- targ.rivalTowerId.flatten) yield (cubeId, towerId)
      })
    targets.flatMap(pair => world.entities.find(pair._1)).filter(_.tags.contains(cubeTag)).exists(snap => (snap.position - entity.position).magnitudeSquared < square(pickUpRange))
  }

  implicit val _ = new ShouldRunTester[SetTrapBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}
