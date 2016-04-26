package behaviours.cube

import behaviours.cube.TargetPositionBehaviour._
import demoteam.{TargetPosition, TargetPositionWriter}
import improbable.Cancellable
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg

import scala.concurrent.duration._

case class SetTargetPosition(pos: Coordinates) extends CustomMsg

trait TargetPositionInterface extends EntityBehaviourInterface {
  def setTarget(pos:Coordinates) : Unit
}

class TargetPositionBehaviour(world: World, entity: Entity, state: TargetPositionWriter) extends EntityBehaviour with TargetPositionInterface {

  override def setTarget(pos:Coordinates) : Unit = {
    state.update.target(Some(pos)).finishAndSend()
  }

  override def onReady(): Unit = {

    world.messaging.onReceive {
      case SetTargetPosition(pos) => setTarget(pos)
    }

    var update: Option[Cancellable] = None

    entity.watch[TargetPosition].bind.target {
      case Some(pos) =>

        update = Some(world.timing.every(5.seconds) {
          if(((pos-entity.position)*(Vector3d.unitX+Vector3d.unitZ)).magnitude>breakDistance) {
            // break!
            state.update.target(None).finishAndSend()
          }
        })
      case _ =>
        update.foreach(_.cancel())
        update = None
    }
  }
}

object TargetPositionBehaviour {
  val breakDistance = 0.125
}
