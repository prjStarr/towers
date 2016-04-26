package templates

import behaviours._
import behaviours.cube.{TeleportBehaviour, TargetPositionBehaviour, BuildingPieceBehaviour, CarriedBehaviour}
import demoteam._
import improbable.corelib.natures.rigidbody.{RigidbodyTransformNature, RigidbodyComposedTransformNature}
import improbable.corelib.natures.{NatureApplication, RigidbodyEntity, NatureDescription}
import improbable.entity.physical.TagsData
import improbable.math.Coordinates
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.behaviour.EntityBehaviourDescriptor
import improbable.util.{WorkerAuthority, WorkerAuthorityBehaviour}

object CubeNature extends NatureDescription {
  override def dependencies: Set[NatureDescription] = Set(RigidbodyTransformNature)

  override def activeBehaviours: Set[EntityBehaviourDescriptor] = Set(
    descriptorOf[BuildingPieceBehaviour],
    descriptorOf[CarriedBehaviour],
    descriptorOf[TeleportBehaviour],
    descriptorOf[TargetPositionBehaviour],
    descriptorOf[ExplosiveBehaviour],
    descriptorOf[WorkerAuthorityBehaviour]
  )

  def apply(position: Coordinates): NatureApplication = application(
    natures = Seq(RigidbodyTransformNature(EntityPrefab("Cube"), position)),
    states = Seq(
      TagsData(List("cube")),
      Carried(None),
      BuildingPiece(None, BuildGeometry.CUBOID),
      Teleporter(),
      TargetPosition(None),
      Explosive(0.0, None),
      WorkerAuthority(-1)
    )
  )
}