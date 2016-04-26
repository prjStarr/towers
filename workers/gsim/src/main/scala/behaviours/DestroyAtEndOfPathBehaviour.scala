package behaviours

import demoteam.Path
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import scala.concurrent.duration._

class DestroyAtEndOfPathBehaviour(entity: Entity, world: World) extends EntityBehaviour {

  override def onReady(): Unit = {

    val path = entity.watch[Path]

    world.timing.every(3.0.seconds) {

      path.pathWaypoints.foreach {
        wp =>
          val pos = entity.position
          val line = pos - wp.last
          if (line.magnitude < 0.1) {
            entity.destroy()
          }
      }
    }
  }

}
