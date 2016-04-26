package behaviours.controls

import demoteam.TeamScoresWriter
import improbable.papi.entity.EntityBehaviour
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg

case class SetScores(scores:Map[Int, Int]) extends CustomMsg

class TeamScoresReplicatorBehaviour(world:World, state:TeamScoresWriter) extends EntityBehaviour {

  override def onReady() : Unit = {
    world.messaging.onReceive {
      case SetScores(scores) => state.update.scores(scores).finishAndSend()
    }
  }
}
