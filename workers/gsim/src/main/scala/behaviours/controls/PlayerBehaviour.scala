package behaviours.controls

import behaviours.SetTrigger
import demoteam._
import improbable.corelib.util.EntityOwnerDelegation.entityOwnerDelegation
import improbable.corelibrary.physical.Transform
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World

class PlayerBehaviour(entity: Entity, world:World) extends EntityBehaviour {

  entity.delegateStateToOwner[Player]
  entity.delegateStateToOwner[Transform]
  entity.delegateStateToOwner[ExplosiveTrigger]
  entity.delegateStateToOwner[TornadoPainter]

  override def onReady() : Unit = {

    entity.watch[ExplosiveTrigger].onSetTimer {
      msg =>
        world.messaging.sendToEntity(msg.targetId, SetTrigger(5.0, BuildGeometry.PYRAMID))
    }
  }
}
