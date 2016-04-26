package behaviours.cube

import demoteam.TeleporterWriter
import improbable.math.{Vector3f, Coordinates, Vector3d}
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg

case class TeleportTo(pos:Coordinates, rot:Vector3d) extends CustomMsg

trait TeleportInterface extends EntityBehaviourInterface {
  def teleportTo(pos:Coordinates, ori:Vector3d) : Unit
}

class TeleportBehaviour(world:World, entity:Entity, state:TeleporterWriter) extends EntityBehaviour with TeleportInterface {

  override def teleportTo(pos:Coordinates, ori:Vector3d) : Unit = {
    state.update.triggerTeleport(pos, ori.toVector3f).finishAndSend()
  }

  override def onReady() : Unit = {
    world.messaging.onReceive {
      case TeleportTo(pos, rot) => teleportTo(pos, rot)
    }
  }
}
