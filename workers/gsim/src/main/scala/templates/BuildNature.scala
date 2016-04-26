package templates

import behaviours.BuildConstants._
import behaviours.{BuildDataReplicatorBehaviour, BuilderBehaviour}
import demoteam._
import demoteam.BuildGeometry.BuildGeometry
import improbable.corelib.math.Quaternion
import improbable.corelib.natures.base.BaseTransformNature
import improbable.corelib.natures.physical.PhysicalNature
import improbable.corelib.natures.transform.composed.ComposedTransformNature
import improbable.corelib.natures.{NatureApplication, NatureDescription, StaticBodyEntity}
import improbable.entity.physical.TagsData
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.EntityId
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.state.EntityStateDescriptor

object BuildNature extends NatureDescription {
  override def dependencies = Set(BaseTransformNature)
  override def activeBehaviours = Set(descriptorOf[BuilderBehaviour], descriptorOf[BuildDataReplicatorBehaviour])

  def states(buildGeometry: BuildGeometry, refId:EntityId):Seq[EntityStateDescriptor] = Seq(
    Geometry(buildGeometry),
    Build(0, 0, 12, 6, 1.05f, 1.05f, Nil),
    BuildMutable(0,0),
    TagsData(List(buildTag)),
    CubeCollector(),
    Participant(refId)
  )
  def natures(pos:Coordinates, rot:Quaternion):Seq[NatureApplication] = Seq(BaseTransformNature(entityPrefab=EntityPrefab("Build"), initialPosition=pos, initialRotation=rot, isPhysical = true))

  def apply(pos:Coordinates, rot:Quaternion, buildGeometry: BuildGeometry, refId:EntityId) : NatureApplication = application(states = states(buildGeometry, refId), natures = natures(pos, rot))
}
