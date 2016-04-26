package behaviours.cube

import demoteam.CarriedWriter
import improbable.papi.EntityId
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg

case class PickedUpBy(carrierId:EntityId) extends CustomMsg
case class DroppedBy(carrierId:EntityId) extends CustomMsg

class CarriedBehaviour(world:World, entity:Entity, state:CarriedWriter) extends EntityBehaviour {

  override def onReady() : Unit = {

    world.messaging.onReceive {
      case PickedUpBy(carrierId) =>
        if(state.carrierId.isEmpty) {
          state.update.carrierId(Some(carrierId)).finishAndSend()
        }
      case DroppedBy(carrierId) =>
        if(state.carrierId.contains(carrierId)) {
          state.update.carrierId(None).finishAndSend()
        }
    }
  }
}
