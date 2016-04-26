package behaviours.statemachine

import behaviours.agent.NavigationInterface
import demoteam.Sleep
import improbable.papi.entity.Entity
import improbable.papi.world.World

/**
  * Created by joss on 16/02/2016.
  */
class IdleBehaviour(entity:Entity, world: World, navigation:NavigationInterface) extends StateMachineBehaviourBase(entity, world, navigation) {
  override def updateAction() : Unit = {}
}

object IdleBehaviour {
  def staticShouldRun(entity: Entity, world: World): Boolean = {
    val sleep = entity.watch[Sleep]
    sleep.sleepTimer.exists(_>0.0)
  }

  implicit val _ = new ShouldRunTester[IdleBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}