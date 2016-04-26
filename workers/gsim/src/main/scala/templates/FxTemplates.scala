package templates

import behaviours.{ExplosionBehaviour, DestroyAtEndOfPathBehaviour, PathBehaviour}
import demoteam.{PathFollower, Path}
import improbable.corelib.natures.{StaticBodyEntity, NatureApplication, NatureDescription}
import improbable.corelib.natures.base.BaseTransformNature
import improbable.math.{Vector3d, Coordinates}
import improbable.papi.entity.EntityPrefab
import improbable.papi.entity.behaviour.EntityBehaviourDescriptor
import improbable.papi.entity.state.EntityStateDescriptor

object TornadoTemplate extends NatureDescription {
  override val dependencies = Set[NatureDescription](BaseTransformNature)

  override def activeBehaviours: Set[EntityBehaviourDescriptor] = {
    Set(
      descriptorOf[PathBehaviour],
      descriptorOf[DestroyAtEndOfPathBehaviour]
    )
  }

  def apply(pos: Coordinates, path:List[Coordinates]=Nil): NatureApplication = {
    application(
      states = Seq(
        Path(if(path.nonEmpty) { path } else { List(pos) }),
        PathFollower(0.0)
      ),
      natures = Seq(
        BaseTransformNature(entityPrefab = EntityPrefab("Tornado"), initialPosition = pos, isPhysical = true)
      )
    )
  }
}

object ExplosionNature extends NatureDescription {
  override def dependencies = Set(StaticBodyEntity)
  override def activeBehaviours = Set(descriptorOf[ExplosionBehaviour])

  def states():Seq[EntityStateDescriptor] = Seq.empty
  def natures(pos:Coordinates, rot:Vector3d):Seq[NatureApplication] = Seq(StaticBodyEntity(prefab=EntityPrefab("Explosion"), position=pos, rotation=rot))

  def apply(pos:Coordinates, rot:Vector3d) : NatureApplication = application(states = states(), natures = natures(pos, rot))
}