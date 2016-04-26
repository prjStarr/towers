package templates

import behaviours.statemachine.StateMachineBehaviour
import behaviours.agent._
import demoteam.BuildGeometry.BuildGeometry
import demoteam._
import improbable.corelib.natures.rigidbody.RigidbodyTransformNature
import improbable.corelib.natures.{NatureApplication, RigidbodyEntity, NatureDescription}
import improbable.math.Coordinates
import improbable.papi.EntityId
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.behaviour.EntityBehaviourDescriptor

object BuilderTemplate extends NatureDescription {
  override def dependencies: Set[NatureDescription] = Set(RigidbodyTransformNature)

  override def activeBehaviours: Set[EntityBehaviourDescriptor] = Set(
    descriptorOf[SubscribeToBuildBehaviour],
    descriptorOf[TargetingBehaviour],
    descriptorOf[CarryingBehaviour],
    descriptorOf[StateMachineBehaviour],
    descriptorOf[NavigationBehaviour],
    descriptorOf[SleepBehaviour],
    descriptorOf[BecomeABomberBehaviour]
  )

  def apply(position: Coordinates, geo:BuildGeometry, secondsBeforeMakingNewBuild:Double, refereeId:EntityId): NatureApplication = application(
    natures = Seq(RigidbodyTransformNature(EntityPrefab("Truck"), position, mass = 50.0f)),
    states = Seq(
      Targeting(None, 0.0, None),
      PhysicalTargeting(Nil),
      Carrying(None),
      Geometry(geo),
      Builder(None, 0.0, secondsBeforeMakingNewBuild),
      Navigation(None, None, 0.0),
      Sleep(0.0),
      Participant(refereeId),
      Bomber(0.0)
    )
  )
}