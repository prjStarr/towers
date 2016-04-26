package improbable.apps

import demoteam.BuildGeometry
import improbable.corelib.util.QuaternionUtils
import improbable.logging.DistributedArchLogging
import improbable.math.{Vector3d, Vector3f, Coordinates}
import improbable.papi.EntityId
import improbable.papi.world.AppWorld
import improbable.papi.world.messaging.CustomMsg
import improbable.papi.worldapp.WorldAppDescriptor
import templates.{RefereeTemplate, CubeNature, BuilderTemplate, Parameters}

import scala.collection.mutable
import scala.concurrent.duration._

import Respawner._

case class SpawnStuff(origin:Coordinates) extends CustomMsg

class Respawner(val world: AppWorld) extends DistributedArchLogging {
  import Parameters._

  val cubes = mutable.Set[EntityId]()
  val gatherers = mutable.Set[EntityId]()

  world.messaging.onReceive{
    case SpawnStuff(origin) =>
      spawnThings(origin)
  }

  def restart(): Unit = {
    logger.info("Restarting simulation")

    destroyEverything()
    world.timing.after(2.seconds) {
      destroyEverything()
    }
    world.timing.after(3.seconds) {
      world.messaging.sendToApp(WorldAppDescriptor.forClass[SimulationReloader].name, SpawnStuff(Coordinates.zero))
    }

  }

  def destroyEverything(): Unit = {
    cubes.foreach(s => world.entities.destroyEntity(s))
    cubes.clear()
  }

  def spawnThings(origin:Coordinates): Unit = {

    val r = cube_spawn_radius.get().toDouble
    val d = r*math.tan(math.Pi/6)
    val qrot = QuaternionUtils.fromAngleAxis(120.0f, Vector3d.unitY)

    var line = -Vector3d.unitZ*d + Vector3d.unitX*r

    val refId = world.entities.spawnEntity(RefereeTemplate())

    (0 until 3).foreach {
      i=>

        val orig = origin + line
        spawnCubes(orig)
        spawnGatherers(orig, refId)

        line = qrot*line
    }
  }

  def spawnCubes(origin:Coordinates): Unit ={
    val delay = cube_impulse_interval_millis.get()/number_of_cubes.get.toDouble
    (0 until number_of_cubes.get()).foreach{
      i =>
        Thread.sleep(delay.floor.toLong,((delay - delay.floor)*1000).toInt)
        cubes += world.entities.spawnEntity(CubeNature(origin+cubeSpawnPosition().toVector3d))
    }
  }

  def lerp(a:Double, b:Double, i:Double) : Double = {
    a*(1.0-i)+b*i
  }

  def spawnGatherers(origin:Coordinates, refId:EntityId): Unit ={
    val delay = 7
    (0 until number_of_gatherers.get()).foreach{
      i =>
        Thread.sleep(delay,0)
        val numGeos = BuildGeometry.maxId
        val geo = BuildGeometry(i%numGeos)
        gatherers += world.entities.spawnEntity(BuilderTemplate(origin+cubeSpawnPosition().toVector3d, geo, lerp(41.0, 123.0, Math.random()), refId))
    }
  }

  def cubeSpawnPosition(): Coordinates = {
    val exclusionRadius = 10.0
    randomPosWithinRadius(cube_spawn_radius.get().toDouble, exclusionRadius)
  }

}

object Respawner {
  def randomPosWithinRadius(rMax:Double, rMin:Double): Coordinates = {
    val angle = Math.random()*2*Math.PI
    val radius = rMin + (math.max(rMax, rMin)-rMin)*Math.sqrt(Math.random())
    Coordinates(math.sin(angle)*radius,0,math.cos(angle)*radius)
  }
}