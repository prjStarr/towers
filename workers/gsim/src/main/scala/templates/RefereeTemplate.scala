package templates

import behaviours.BuildConstants._
import behaviours.{RefereeBehaviour, BuildDataReplicatorBehaviour, BuilderBehaviour}
import demoteam._
import demoteam.BuildGeometry.BuildGeometry
import improbable.corelib.math.Quaternion
import improbable.corelib.natures.base.BaseTransformNature
import improbable.corelib.natures.physical.PhysicalNature
import improbable.corelib.natures.transform.composed.ComposedTransformNature
import improbable.corelib.natures.{NatureApplication, NatureDescription, StaticBodyEntity}
import improbable.entity.physical.{Position, TagsData}
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.state.EntityStateDescriptor

object RefereeTemplate extends NatureDescription {
  override def dependencies = Set()
  override def activeBehaviours = Set(descriptorOf[RefereeBehaviour])

  def states():Seq[EntityStateDescriptor] = Seq(
    Referee(Map()),
    Position(0.0f, Coordinates.zero)
  )
  def natures():Seq[NatureApplication] = Seq()

  def apply() : NatureApplication = application(states = states(), natures = natures())
}
