package behaviours.cube

import behaviours.{ExplosiveInterface, ExplosionBehaviour, RemoveBlockFromBuild}
import com.typesafe.scalalogging.Logger
import demoteam.BuildGeometry.BuildGeometry
import demoteam.{Explosive, BuildGeometry, BuildingPieceWriter, TargetPosition}
import improbable.math.{Vector3d, Coordinates}
import improbable.papi.EntityId
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg
import BuildingPieceBehaviour._

case class PieceOf(buildId: EntityId, geo:BuildGeometry, pos:Coordinates, ori:Vector3d) extends CustomMsg
case class ClearBuild(buildId: EntityId) extends CustomMsg

class BuildingPieceBehaviour(world: World, entity: Entity, state: BuildingPieceWriter, teleport:TeleportInterface, target:TargetPositionInterface, explosive:ExplosiveInterface) extends EntityBehaviour {

  override def onReady(): Unit = {

    world.messaging.onReceive {
      case PieceOf(buildId, geo, pos, ori) =>
        state.update.buildId(Some(buildId)).geometry(geo).finishAndSend()
        teleport.teleportTo(pos, ori)
        target.setTarget(pos)

        entity.watch[Explosive].armedBy.flatten.foreach {
          geo =>
            explosive.trigger(trapFuseLength)
        }
      case ClearBuild(entId) =>
        state.update.buildId(None).finishAndSend()
    }

    entity.watch[TargetPosition].bind.target {
      case None => state.buildId.foreach(buildId => world.messaging.sendToEntity(buildId, RemoveBlockFromBuild(entity.entityId)))
      case _ =>
    }
  }
}

object BuildingPieceBehaviour {
  val trapFuseLength = 30.0
}
