package behaviours.agent

import demoteam.{Sleep, SleepWriter}
import improbable.Cancellable
import improbable.papi.entity.behaviour.EntityBehaviourInterface
import improbable.papi.entity.{EntityBehaviour, Entity}
import improbable.papi.world.World
import scala.concurrent.duration._

/**
  * Created by joss on 16/02/2016.
  */

trait SleepInterface extends EntityBehaviourInterface {

  def forTime(seconds: Double): Unit
}

class SleepBehaviour(entity: Entity, world: World, state: SleepWriter) extends EntityBehaviour with SleepInterface {

  override def forTime(seconds: Double): Unit = {
    state.update.sleepTimer(seconds).finishAndSend()
  }

  override def onReady(): Unit = {

    var update: Option[Cancellable] = None

    entity.watch[Sleep].bind.sleepTimer {
      timer =>
        if (timer > 0.0) {
          update.foreach(_.cancel())
          update = Some(world.timing.every(1.second) {
            val newTime = state.sleepTimer - 1.0
            if (newTime > 0.001) {
              state.update.sleepTimer(newTime).finishAndSend()
            }
            else {
              state.update.sleepTimer(0.0).finishAndSend()
            }
          })
        }
        else {
          update.foreach(_.cancel())
          update = None
        }
    }

  }
}
