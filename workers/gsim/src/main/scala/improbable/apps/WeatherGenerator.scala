package improbable.apps

import com.typesafe.scalalogging.Logger
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.world.AppWorld
import improbable.papi.worldapp.{WorldAppLifecycle, WorldApp}
import templates.TornadoTemplate

import scala.concurrent.duration._

/**
  * Created by joss on 23/02/2016.
  */
class WeatherGenerator(val world: AppWorld, val logger: Logger, val lifecycle: WorldAppLifecycle) extends WorldApp {

  val interval = 300.0

  world.timing.after(interval.seconds) {
    world.timing.every(interval.seconds) {

      val startLocal = Respawner.randomPosWithinRadius(50.0, 0.0)
      val endLocal = Respawner.randomPosWithinRadius(50.0, 0.0)

      val start = startLocal + Vector3d.unitZ * 50.0
      val end = endLocal - Vector3d.unitZ * 50.0

      world.entities.spawnEntity(TornadoTemplate(start, List(start, end)))
    }
  }
}
