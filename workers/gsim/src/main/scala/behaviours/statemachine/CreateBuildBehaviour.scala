package behaviours.statemachine

import behaviours.agent.{SubscribeToBuildInterface, NavigationInterface}
import improbable.corelib.math.Quaternion
import improbable.corelib.util.QuaternionUtils
import improbable.math.Vector3d
import improbable.papi.entity.Entity
import improbable.papi.world.World
import demoteam.{Participant, Geometry, Builder}
import templates.BuildNature

class CreateBuildBehaviour(entity: Entity,
                           world: World,
                           navigation: NavigationInterface,
                           subscribeToBuild: SubscribeToBuildInterface)
                                                extends StateMachineBehaviourBase(entity, world, navigation) {

  override def updateAction(): Unit = {
    val geoOption = entity.watch[Geometry].geometryType
    val participantOption = entity.watch[Participant].refereeId
    (for(a <- geoOption; b <- participantOption) yield (a,b)).foreach {
      pair =>
        val buildId = world.entities.spawnEntity(BuildNature(entity.position, QuaternionUtils.identity, pair._1, pair._2))
        subscribeToBuild.subscribe(buildId)
    }
  }
}

object CreateBuildBehaviour {
  def staticShouldRun(entity: Entity, world: World): Boolean = {
    val builder = entity.watch[Builder]
    builder.buildId.flatten.isEmpty &&
      (for (a <- builder.secondsLookingForBuild; b <- builder.secondsBeforeMakingNewBuild) yield (a, b)).exists {
        tuple => tuple._1 > tuple._2
      }
  }

  implicit val _ = new ShouldRunTester[CreateBuildBehaviour] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}
