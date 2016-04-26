package behaviours

import behaviours.cube.{ClearBuild, PieceOf}
import demoteam._
import demoteam.buildingdemo.{CuboidBuildHelper, CylinderBuildHelper, PyramidBuildHelper}
import improbable.corelib.util.QuaternionUtils
import improbable.corelibrary.physical.Transform
import improbable.math.Coordinates
import improbable.papi.EntityId
import improbable.papi.entity.{EntityBehaviour, Entity}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg
import improbable.unity.fabric.PhysicsEngineConstraint
import templates.CubeNature
import scala.concurrent.duration._

case class AddBlockToBuild(entId: EntityId) extends CustomMsg

case class RemoveBlockFromBuild(entId: EntityId) extends CustomMsg

case class AddWorker(workerId: EntityId) extends CustomMsg

case class BuildFinished(buildId: EntityId) extends CustomMsg

class BuilderBehaviour(world: World, entity: Entity, state: BuildWriter) extends EntityBehaviour {

  val geoWatch = entity.watch[Geometry]

  val buildHelper = geoWatch.geometryType.flatMap {
    case BuildGeometry.CUBOID => Some(new CuboidBuildHelper(1.0f, 5, 6, 5))
    case BuildGeometry.CYLINDER => Some(new CylinderBuildHelper(1.0f, 16, 6, state.heightMultiplier, state.radMultipler))
    case BuildGeometry.PYRAMID => Some(new PyramidBuildHelper(1.0f, 6))
  }

  def maxBlocks: Int = {
    buildHelper.map(_.maxBlocks).getOrElse(0)
  }

  def buildNext(): Unit = {

    val blockId = world.entities.spawnEntity(CubeNature(Coordinates.zero))

    world.timing.after(0.5.seconds) {
      world.messaging.sendToEntity(entity.entityId, AddBlockToBuild(blockId))
    }

    if (state.numBlocks + 1 < maxBlocks) {

      world.timing.after(1.5.seconds) {
        buildNext()
      }
    }
  }

  def addBlockToBuild(blockId: EntityId): Unit = {

    if (!state.associatedEntities.contains(blockId) && state.numBlocks < maxBlocks) {
      val iblock = state.numBlocks
      buildHelper.foreach {
        build =>
          val posMs = build.getPositionForBlockAtIndex(iblock)
          val rotMs = build.getRotationForBlockAtIndex(iblock)

          val ori = entity.watch[Transform].rotation.getOrElse(QuaternionUtils.identity)

          // set as part of build
          geoWatch.geometryType.foreach(geo => world.messaging.sendToEntity(blockId, PieceOf(entity.entityId, geo, entity.position + ori*posMs, ori*rotMs)))

          state.update.numBlocks(iblock + 1).associatedEntities(state.associatedEntities :+ blockId).finishAndSend()

          if (buildHelper.exists(_.maxBlocks == iblock + 1)) {
            finishBuild()
          }
      }
    }
  }

  def bothExist[A,B](a:Option[A], b:Option[B]) : Option[(A,B)] = {
    for(aa <- a; bb <- b) yield (aa,bb)
  }

  def finishBuild(): Unit = {
    state.associatedEntities.foreach {
      entId => world.messaging.sendToEntity(entId, BuildFinished(entity.entityId))
    }

    // add score
    bothExist(entity.watch[Participant].refereeId, geoWatch.geometryType).foreach(pair => world.messaging.sendToEntity(pair._1, IncrementScoreFor(pair._2)))
  }

  def terminateBuild(): Unit = {
    state.associatedEntities.foreach {
      entId => world.messaging.sendToEntity(entId, ClearBuild(entity.entityId))
    }

    if(state.numBlocks==state.maxBlocks) {
      bothExist(entity.watch[Participant].refereeId, geoWatch.geometryType).foreach(pair => world.messaging.sendToEntity(pair._1, DecrementScoreFor(pair._2)))
    }

    entity.destroy()
  }

  override def onReady(): Unit = {

    buildHelper.foreach(build => state.update.maxBlocks(build.maxBlocks).finishAndSend())

    entity.delegateState[CubeCollector](PhysicsEngineConstraint)

    entity.watch[CubeCollector].onAddCubeToBuild(msg => addBlockToBuild(msg.cubes))

    world.messaging.onReceive {
      case AddWorker(workerId) => state.update.associatedEntities(state.associatedEntities :+ workerId).finishAndSend()
      case AddBlockToBuild(blockId) => addBlockToBuild(blockId)
      case RemoveBlockFromBuild(blockId) => terminateBuild()
    }
  }
}