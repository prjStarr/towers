package behaviours

import behaviours.controls.SetScores
import com.typesafe.scalalogging.Logger
import demoteam.BuildGeometry.BuildGeometry
import demoteam.{BuildGeometry, RefereeWriter}
import improbable.apps.PlayerLifeCycleManager
import improbable.papi.entity.{EntityBehaviour, Entity}
import improbable.papi.world.World
import improbable.papi.world.messaging.CustomMsg

case class IncrementScoreFor(geo:BuildGeometry) extends CustomMsg
case class DecrementScoreFor(geo:BuildGeometry) extends CustomMsg

class RefereeBehaviour(entity:Entity, world:World, state:RefereeWriter, logger:Logger) extends EntityBehaviour {

  def printScores() : Unit = {
    logger.info("scores are as follows: " + state.score)
  }

  def shareScores() : Unit = {
    world.messaging.sendToApp(classOf[PlayerLifeCycleManager].getName, SetScores(state.score))
  }

  def updateScoreFor(buildGeometry: BuildGeometry, score: Int) : Unit = {
    val newScores = state.score.updated(buildGeometry.id, score)
    state.update.score(newScores).finishAndSend()
    printScores()
    shareScores()
  }

  override def onReady() : Unit = {

    state.update.score(BuildGeometry.values.map( _.id -> 0).toMap).finishAndSend()

    world.messaging.onReceive {
      case IncrementScoreFor(geo) =>
        val newScore = state.score.getOrElse(geo.id,0) + 1
        updateScoreFor(geo, newScore)
      case DecrementScoreFor(geo) =>
        val newScore = state.score.getOrElse(geo.id,1) - 1
        updateScoreFor(geo, newScore)
    }
  }
}
