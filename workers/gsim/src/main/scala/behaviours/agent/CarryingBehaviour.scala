package behaviours.agent

import behaviours.cube.{DroppedBy, PickedUpBy}
import demoteam.CarryingWriter
import improbable.papi.EntityId
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World

trait CarryingInterface extends EntityBehaviourInterface {
  def dropCarried(): Unit

  def pickUp(entId: EntityId): Unit

  def isCarrying() : Boolean
}

class CarryingBehaviour(entity:Entity, world:World, state: CarryingWriter) extends EntityBehaviour with CarryingInterface {

  override def pickUp(entId:EntityId) :Unit = {
    world.messaging.sendToEntity(entId, PickedUpBy(entity.entityId))
    state.update.carryingId(Some(entId)).finishAndSend()
  }

  override def dropCarried(): Unit = {
    state.carryingId.foreach {
      entId =>
        world.messaging.sendToEntity(entId, DroppedBy(entity.entityId))
        state.update.carryingId(None).finishAndSend()
    }
  }
  def isCarrying() : Boolean = {
    state.carryingId.nonEmpty
  }
}
