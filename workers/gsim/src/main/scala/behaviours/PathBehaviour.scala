package behaviours

import demoteam.{PathFollower, PathWriter}
import improbable.math.Coordinates
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg
import improbable.unity.fabric.PhysicsEngineConstraint

case class AddWaypoint(pos:Coordinates) extends CustomMsg

class PathBehaviour(entity:Entity, world:World, state:PathWriter) extends EntityBehaviour {

  override def onReady() : Unit = {

    entity.delegateState[PathFollower](PhysicsEngineConstraint)


    world.messaging.onReceive {
      case AddWaypoint(pos) =>
        state.update.pathWaypoints(state.pathWaypoints :+ pos).finishAndSend()
    }


  }
}
