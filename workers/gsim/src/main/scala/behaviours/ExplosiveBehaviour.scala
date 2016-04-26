package behaviours

import com.typesafe.scalalogging.Logger
import demoteam.BuildGeometry.BuildGeometry
import demoteam.ExplosiveWriter
import improbable.Cancellable
import improbable.entity.physical.RigidbodyInterface
import improbable.math.Vector3d
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{EntityBehaviour, Entity}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg
import ExplosiveBehaviour._
import templates.ExplosionNature
import scala.concurrent.duration._

case class SetTrigger(time: Double, geo:BuildGeometry) extends CustomMsg

case class ApplyImpulse(impulse:Vector3d) extends CustomMsg

case class ItsATrap(geo:BuildGeometry) extends CustomMsg

trait ExplosiveInterface extends EntityBehaviourInterface {
  def trigger(fuseTime:Double) : Unit
  def explode() : Unit
}

class ExplosiveBehaviour(world: World, entity: Entity, logger:Logger, state: ExplosiveWriter, rigidbodyInterface: RigidbodyInterface) extends EntityBehaviour with ExplosiveInterface {

  var update: Option[Cancellable] = None

  override def explode(): Unit = {
    world.entities.spawnEntity(ExplosionNature(entity.position, Vector3d.zero))
    entity.destroy()
  }

  override def trigger(fuseTime:Double) : Unit = {
    state.update.timer(fuseTime).finishAndSend()

    update = Some(world.timing.every(timerUpdatePeriod.seconds) {
      val newTime = state.timer - timerUpdatePeriod
      if (newTime < 0.0) {
        update.foreach(_.cancel())
        explode()
      }
      else {
        state.update.timer(newTime).finishAndSend()
      }
    })
  }

  override def onReady(): Unit = {

    world.messaging.onReceive {
      case SetTrigger(t, geo) =>
        trigger(t)
        state.update.armedBy(Some(geo)).finishAndSend()

      case ItsATrap(geo) =>
        logger.info("It's a trap! Entity: " + entity.entityId + "; Position: " + entity.position)
        state.update.armedBy(Some(geo)).finishAndSend()

      case ApplyImpulse(impulse) => rigidbodyInterface.applyImpulse(impulse)
    }
  }
}

object ExplosiveBehaviour {
  val timerUpdatePeriod = 0.2
}