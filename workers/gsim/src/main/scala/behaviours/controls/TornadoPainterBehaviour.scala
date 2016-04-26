package behaviours.controls

import behaviours.AddWaypoint
import demoteam.TornadoPainter
import improbable.papi.EntityId
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import templates.TornadoTemplate

class TornadoPainterBehaviour(entity:Entity, world: World) extends EntityBehaviour {

  override def onReady() : Unit = {

    var tornadoId : Option[EntityId] = None

    entity.watch[TornadoPainter].onCreateTornado {
      msg =>
        tornadoId = Some(world.entities.spawnEntity(TornadoTemplate(msg.position)))
    }

    entity.watch[TornadoPainter].onUpdatePath {
      msg=>
        tornadoId.foreach(tornId=>world.messaging.sendToEntity(tornId, AddWaypoint(msg.position)))
    }

    entity.watch[TornadoPainter].onFinishPath {
      msg=>
        tornadoId = None
    }
  }
}
