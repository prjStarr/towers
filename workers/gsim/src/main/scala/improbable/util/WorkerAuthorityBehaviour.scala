package improbable.util

import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.unity.fabric.PhysicsEngineConstraint

class WorkerAuthorityBehaviour(entity:Entity, world:World) extends EntityBehaviour {

  override def onReady() : Unit = {
    entity.delegateState[WorkerAuthority](PhysicsEngineConstraint)
  }
}
