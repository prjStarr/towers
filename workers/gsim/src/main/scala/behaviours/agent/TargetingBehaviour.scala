package behaviours.agent

import behaviours.BuildConstants._
import behaviours.agent.TargetingBehaviour._
import demoteam.BuildGeometry.BuildGeometry
import demoteam._
import improbable.Cancellable
import improbable.papi.EntityId
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour, EntitySnapshot}
import improbable.papi.world.World
import improbable.unity.fabric.PhysicsEngineConstraint

import scala.concurrent.duration._

trait TargetingInterface extends EntityBehaviourInterface {
  def clearTarget() : Unit
}

class TargetingBehaviour(world: World, entity: Entity, state: TargetingWriter) extends EntityBehaviour with TargetingInterface {

  val geometryWatcher = entity.watch[Geometry]

  val builder = entity.watch[Builder]
  def buildId = builder.buildId.flatten

  def geometry : BuildGeometry = {
    geometryWatcher.geometryType.getOrElse(BuildGeometry.CUBOID)
  }

  def cubeInBuild(cubeSnap: EntitySnapshot) : Boolean = {
    cubeSnap.get[BuildingPiece].exists(_.buildId.nonEmpty)
  }

  def cubeCarried(cubeSnap: EntitySnapshot) : Boolean = {
    cubeSnap.get[Carried].exists(_.carrierId.nonEmpty)
  }

  def cubeIsTrap(cubeSnap: EntitySnapshot) : Boolean = {
    cubeSnap.get[Explosive].exists(_.armedBy.contains(geometry))
  }

  def cubeAvailable(cubeSnap: EntitySnapshot): Boolean = {
    !cubeInBuild(cubeSnap) && !cubeCarried(cubeSnap) && !cubeIsTrap(cubeSnap)
  }

  def buildHasGeometry(buildSnap: EntitySnapshot, buildGeometry: BuildGeometry): Boolean = {
    buildSnap.get[Geometry].map(_.geometryType).contains(buildGeometry)
  }

  def worthAttacking(buildSnap: EntitySnapshot) : Boolean = {
    buildSnap.get[BuildMutable].exists(bm => bm.numBlocks>percentageCompleteThresholdForAttack*bm.maxBlocks)
  }

  def entityClosest(a: Option[EntitySnapshot], b: EntitySnapshot): Option[EntitySnapshot] = {
    val aDist = a.map(snap => (snap.position - entity.position).magnitude).getOrElse(Double.MaxValue)
    val bDist = (b.position - entity.position).magnitude
    if (aDist < bDist) {
      a
    }
    else {
      Some(b)
    }
  }

  override def clearTarget() : Unit = {
    state.update.targetId(None).finishAndSend()
  }

  def getBestTarget : Option[EntityId] = {

    if (buildId.isEmpty) {
      val buildsAll = world.entities.find(entity.position, buildRange, Set(buildTag))
      val buildsAvailable = buildsAll.filter(build => buildHasGeometry(build, geometry) && build.get[BuildMutable].exists(bm=>bm.numBlocks<bm.maxBlocks))
      val closest = buildsAvailable.foldLeft(buildsAvailable.headOption)(entityClosest)
      closest.map(_.entityId)
    }
    else {
      val cubeIds = entity.watch[PhysicalTargeting].targetCandidates.getOrElse(Nil)
      val cubesAll = cubeIds.flatMap( entId => world.entities.find(entId))
      val cubesAvailable = cubesAll.filter(cubeAvailable)
      val closest = cubesAvailable.foldLeft(cubesAvailable.headOption)(entityClosest)
      closest.map(_.entityId)
    }
  }

  override def onReady(): Unit = {

    entity.delegateState[PhysicalTargeting](PhysicsEngineConstraint)

    val targeting = entity.watch[Targeting]

    world.timing.every(checkTargetFrequency.second) {
      val targetId = getBestTarget
      if (targetId != state.targetId) {
        state.update.targetId(targetId).finishAndSend()
      }
    }

    var timeWithoutTarget: Option[Cancellable] = None

    targeting.bind.targetId {
      targetId =>
        if (targetId.nonEmpty) {
          timeWithoutTarget.foreach(_.cancel())
          timeWithoutTarget = None

          state.update.timeWithoutTarget(0.0).finishAndSend()
        }
        else {
          state.update.timeWithoutTarget(0.0).finishAndSend()

          timeWithoutTarget = Some(world.timing.every(noTargetAccrualTimeInterval.seconds) {
            state.update.timeWithoutTarget(state.timeWithoutTarget + noTargetAccrualTimeInterval).finishAndSend()
          })
        }
    }

    world.timing.every(checkRivalTowerFrequency.seconds) {
      val buildsAll = world.entities.find(entity.position, buildRange, Set(buildTag))
      val buildsAvailable = buildsAll.filter(build => !buildHasGeometry(build, geometry)).filter(build => worthAttacking(build))
      val closest = buildsAvailable.foldLeft(buildsAvailable.headOption)(entityClosest)
      val rivalTowerId = closest.map(_.entityId)
      state.update.rivalTowerId(rivalTowerId).finishAndSend()
    }
  }
}

object TargetingBehaviour {
  val cubeSearchRange = 16.0
  val checkTargetFrequency = 3.0
  val checkRivalTowerFrequency = 15.0
  val cubeTag = "cube"
  val noTargetAccrualTimeInterval = 5.0
  val percentageCompleteThresholdForAttack = 0.15
}