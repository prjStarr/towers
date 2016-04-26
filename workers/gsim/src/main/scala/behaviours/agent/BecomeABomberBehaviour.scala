package behaviours.agent

import demoteam.BomberWriter
import improbable.Cancellable
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import BecomeABomberBehaviour._

import scala.concurrent.duration._

trait BomberInterface extends EntityBehaviourInterface {
  def clear() : Unit
}

class BecomeABomberBehaviour(entity:Entity, world:World, state:BomberWriter) extends EntityBehaviour with BomberInterface  {

  var update : Option[Cancellable] = None

  override def clear() : Unit = {
    state.update.bomberTimer(0.0).finishAndSend()
    update.foreach(_.cancel())
    update = None
  }

  override def onReady() : Unit = {

    world.timing.every(timeAsBomber.seconds) {

      if(math.random>(1.0-chanceToBomb)) {
        state.update.bomberTimer(timeAsBomber).finishAndSend()

        update = Some(world.timing.every(timerPeriod.seconds) {

          val newTimer = math.max(state.bomberTimer - timerPeriod, 0.0)
          state.update.bomberTimer(newTimer).finishAndSend()

          if(newTimer<0.001) {
            clear()
          }
          else {
            state.update.bomberTimer(newTimer).finishAndSend()
          }
        })
      }
    }
  }
}

object BecomeABomberBehaviour {
  val chanceToBomb = 0.05
  val timeAsBomber = 30.0
  val timerPeriod = 5.0
}
