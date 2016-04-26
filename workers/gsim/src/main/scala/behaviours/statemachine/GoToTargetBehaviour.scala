package behaviours.statemachine

import behaviours.statemachine.GoToTargetBehaviour._
import behaviours.agent.{NavigationInterface, NavigationBehaviour}
import demoteam.{TargetingWatcher, Targeting}
import improbable.entity.physical.RigidbodyInterface
import improbable.math.Coordinates
import improbable.papi.entity.Entity
import improbable.papi.world.World

class GoToTargetBehaviour(world: World, entity: Entity, navigation: NavigationInterface) extends StateMachineBehaviourBase(entity, world, navigation) {

  private var targetingWatcher: Option[TargetingWatcher] = None

  private def targetId = targetingWatcher.flatMap(_.targetId).flatten

  private def targetPos: Option[Coordinates] = {
    targetId.flatMap(world.entities.find).map(_.position)
  }

  override def updateAction(): Unit = {
    targetPos.foreach(moveTowards)
  }

  override def onReady(): Unit = {
    targetingWatcher = Some(entity.watch[Targeting])
    super.onReady()
  }
}

object GoToTargetBehaviour {
  def staticShouldRun(entity: Entity, world: World): Boolean = {
    entity.watch[Targeting].targetId.flatten.nonEmpty
  }

  implicit val _ = new ShouldRunTester[GoToTargetBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}