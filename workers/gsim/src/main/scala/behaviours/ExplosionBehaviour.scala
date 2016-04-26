package behaviours

import improbable.math.Vector3d
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import ExplosionBehaviour._
import scala.concurrent.duration._

class ExplosionBehaviour(world:World, entity:Entity) extends EntityBehaviour {

  def sqrMag(v: Vector3d): Double = {
    v.dot(v)
  }

  override def onReady() : Unit = {
    val cubes = world.entities.find(entity.position, explosiveRange, Set.empty).filter(_.entityId != entity.entityId)
    cubes.foreach {
      snap =>
        val line = snap.position - entity.position
        if (line.magnitude > 0.01) {
          val impulse = line * explosiveRange*explosiveImpulse / sqrMag(line)

          world.messaging.sendToEntity(snap.entityId, ApplyImpulse(impulse))
        }
    }

    world.timing.after(7.seconds) {
      entity.destroy()
    }
  }
}

object ExplosionBehaviour {
  val explosiveRange = 5.0
  val explosiveImpulse = 7.0
}