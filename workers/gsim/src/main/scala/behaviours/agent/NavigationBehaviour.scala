package behaviours.agent

import demoteam.NavigationWriter
import improbable.math.Coordinates
import improbable.papi.EntityId
import improbable.papi.entity.EntityBehaviour
import improbable.papi.entity.behaviour.EntityBehaviourInterface

trait NavigationInterface extends EntityBehaviourInterface {
  def moveToPosition(pos: Coordinates, range: Double): Unit

  def moveToEntity(entId: EntityId, range: Double): Unit

  def clear(): Unit
}

class NavigationBehaviour(state: NavigationWriter) extends EntityBehaviour with NavigationInterface {

  override def onReady(): Unit = {
  }

  override def moveToPosition(pos: Coordinates, range: Double): Unit = {
    state.update.targetPos(Some(pos)).targetRange(range).finishAndSend()
  }

  override def moveToEntity(entId: EntityId, range: Double): Unit = {
    state.update.targetEntity(Some(entId)).targetRange(range).finishAndSend()
  }

  override def clear() : Unit = {
    state.update.targetEntity(None).targetPos(None).finishAndSend()
  }
}
