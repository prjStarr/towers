package behaviours.statemachine

import behaviours.agent.NavigationInterface
import demoteam.Targeting
import improbable.papi.entity.Entity
import improbable.papi.world.World
import SmashTowerBehaviour._


class SmashTowerBehaviour(entity: Entity, world: World, navigation: NavigationInterface) extends StateMachineBehaviourBase(entity, world, navigation) {

  val targeting = entity.watch[Targeting]

  override def updateAction(): Unit = {
    targeting.rivalTowerId.flatten.foreach(targetId => navigation.moveToEntity(targetId, towerSmashRange))
  }
}

object SmashTowerBehaviour {
  val timeIdleBeforeSmash = 120.0
  val towerSmashRange = 0.1f

  def staticShouldRun(entity: Entity, world: World): Boolean = {

    val wat = entity.watch[Targeting]
    wat.timeWithoutTarget.exists(_ > timeIdleBeforeSmash) && wat.rivalTowerId.flatten.flatMap(world.entities.find).exists(snap => (snap.position - entity.position).magnitude > 0.5)
  }

  implicit val _ = new ShouldRunTester[SmashTowerBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}
