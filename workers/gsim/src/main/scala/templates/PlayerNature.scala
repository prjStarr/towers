package templates

import behaviours._
import behaviours.controls.{TeamScoresReplicatorBehaviour, PlayerBehaviour, TornadoPainterBehaviour}
import improbable.corelib.natures.{NatureApplication, NatureDescription}
import improbable.corelib.natures.base.BaseTransformNature
import improbable.corelib.util.EntityOwner
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.engine.EngineId
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.behaviour.EntityBehaviourDescriptor
import demoteam._

object PlayerNature extends NatureDescription {

  override val dependencies = Set[NatureDescription](BaseTransformNature)

  override def activeBehaviours: Set[EntityBehaviourDescriptor] = {
    Set(
      descriptorOf[PlayerBehaviour],
      descriptorOf[TornadoPainterBehaviour],
      descriptorOf[TeamScoresReplicatorBehaviour]
    )
  }

  def apply(engineId: EngineId): NatureApplication = {
    application(
      states = Seq(
        EntityOwner(ownerId = Some(engineId)),
        Player(Vector3d.zero),
        ExplosiveTrigger(),
        TornadoPainter(),
        TeamScores(Map())
      ),
      natures = Seq(
        BaseTransformNature(entityPrefab = EntityPrefab("Player"), initialPosition = Coordinates(0, 0.5, 0))
      )
    )
  }

}
