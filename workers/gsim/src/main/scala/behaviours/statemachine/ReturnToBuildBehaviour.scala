package behaviours.statemachine

import behaviours.agent.{SleepInterface, CarryingInterface, NavigationInterface}
import demoteam._
import improbable.math.{Vector3d, Coordinates}
import improbable.papi.EntityId
import improbable.papi.entity.Entity
import improbable.papi.world.World

import behaviours.BuildConstants._

class ReturnToBuildBehaviour(entity: Entity, world: World, navigation: NavigationInterface, carrying: CarryingInterface, sleep:SleepInterface) extends StateMachineBehaviourBase(entity, world, navigation) {

  private var targetingWatcher: Option[TargetingWatcher] = None
  private var builderWatcher: Option[BuilderWatcher] = None

  def buildId: Option[EntityId] = {
    builderWatcher.flatMap(_.buildId).flatten
  }

  def buildPosition: Option[Coordinates] = {
    buildId.flatMap(world.entities.find).map(_.position)
  }

  def updateAction(): Unit = {
    buildPosition.foreach(moveTowards)
  }

  override def stop(): Unit = {
    carrying.dropCarried()
    sleep.forTime(3.0)
    super.stop()
  }

  override def start(): Unit = {

    val targetId = targetingWatcher.flatMap(_.targetId).flatten
    targetId.foreach(carrying.pickUp)
  }

  override def onReady(): Unit = {
    targetingWatcher = Some(entity.watch[Targeting])
    builderWatcher = Some(entity.watch[Builder])
    super.onReady()
  }
}

object ReturnToBuildBehaviour {

  def sqrMag(v: Vector3d): Double = {
    v.dot(v)
  }

  def sqr(d: Double): Double = {
    d * d
  }

  def inRangeOf(entity: Entity, world: World, entId: Option[EntityId], range: Double): Boolean = {
    val osnap = entId.flatMap(world.entities.find)
    osnap.exists(snap => sqrMag(snap.position - entity.position) < sqr(range))
  }

  def entityExists(world: World, entId: Option[EntityId]): Boolean = {
    entId.flatMap(world.entities.find).nonEmpty
  }

  def targetValid(world: World, entId: Option[EntityId]): Boolean = {
    val targetSnap = entId.flatMap(world.entities.find)
    val hasCarrier = targetSnap.flatMap(_.get[Carried]).exists(_.carrierId.nonEmpty)
    !hasCarrier
  }

  def staticShouldRun(entity: Entity, world: World): Boolean = {

    val carriedId = entity.watch[Carrying].carryingId.flatten
    val targetId = entity.watch[Targeting].targetId.flatten
    val buildId = entity.watch[Builder].buildId.flatten

    entityExists(world, buildId) &&
    !inRangeOf(entity, world, buildId, buildPickupRange) &&
      ( (targetValid(world, targetId) && inRangeOf(entity, world, targetId, pickUpRange)) ||
        entityExists(world, carriedId))
  }

  implicit val _ = new ShouldRunTester[ReturnToBuildBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}