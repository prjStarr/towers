package behaviours.agent

import java.util.Random

import behaviours.{BuildFinished, AddWorker}
import behaviours.BuildConstants._
import behaviours.cube.ClearBuild
import demoteam.{Targeting, Builder, BuilderWriter}
import improbable.Cancellable
import improbable.papi.EntityId
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg
import SubscribeToBuildBehaviour._

import scala.concurrent.duration._

trait SubscribeToBuildInterface extends EntityBehaviourInterface {
  def subscribe(buildId:EntityId) : Unit
}

class SubscribeToBuildBehaviour(world: World, entity: Entity, state: BuilderWriter) extends EntityBehaviour with SubscribeToBuildInterface {

  override def subscribe(buildId:EntityId) : Unit = {
    state.update.buildId(Some(buildId)).secondsLookingForBuild(0.0).finishAndSend()
    world.messaging.sendToEntity(buildId, AddWorker(entity.entityId))
  }

  def entityIsBuild(entityId:Option[EntityId]) : Boolean = {
    entityId.nonEmpty && world.entities.find(entityId.get).exists(_.tags.contains(buildTag))
  }

  def clearUpdate(update:Option[Cancellable]) : Option[Cancellable] = {
    update.foreach(_.cancel())
    None
  }

  def clearBuild() : Unit = {
    state.update.buildId(None).finishAndSend()
  }

  override def onReady(): Unit = {

    entity.watch[Targeting].bind.targetId {
      targetId=>
        if(state.buildId.isEmpty && entityIsBuild(targetId)) {
          subscribe(targetId.get)
        }
    }

    world.timing.every(3.seconds) {
      val buildSnapshot = state.buildId.flatMap(world.entities.find)
      if(buildSnapshot.isEmpty) {
        state.update.buildId(None).finishAndSend()
      }
    }

    var update: Option[Cancellable] = None

    entity.watch[Builder].bind.buildId {
      case Some(buildId) =>
        update = clearUpdate(update)
      case _ =>
        clearUpdate(update)
        update = Some(world.timing.every(WaitForBuildInterval.seconds) {
          state.update.secondsLookingForBuild(state.secondsLookingForBuild+WaitForBuildInterval).finishAndSend()
        })
    }
    world.messaging.onReceive {
      case ClearBuild(entId) => clearBuild()
      case BuildFinished(entId) => clearBuild()
    }
  }
}

object  SubscribeToBuildBehaviour {
  val WaitForBuildInterval = 5.0
}
