package behaviours.statemachine

import improbable.Cancellable
import improbable.papi.entity.behaviour.{EntityBehaviourDescriptor, EntityBehaviourConverter}
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import scala.reflect._
import scala.concurrent.duration._

trait ShouldRunTester[T] {
  def shouldRun(entity: Entity, world: World): Boolean
}

class StateMachineBehaviour(entity: Entity, world: World) extends EntityBehaviour {

  case class StatePredicate(descriptor: EntityBehaviourDescriptor, priority: Int, predicate: () => Boolean) {
  }

  val behaviours = List[StatePredicate](
    makeStatePredicate[IdleBehaviour](20),
    //makeStatePredicate[SetTrapBehaviour](15),
    makeStatePredicate[ReturnToBuildBehaviour](10),
    makeStatePredicate[GoToTargetBehaviour](9),
    makeStatePredicate[CreateBuildBehaviour](5),
    //makeStatePredicate[SmashTowerBehaviour](4),
    makeStatePredicate[RandomWander](0)
  )

  case class RunningBehaviour(statePredicate: StatePredicate, behaviour: Cancellable)

  private var activeBehaviour: Option[RunningBehaviour] = None

  private def descriptorOf[T <: EntityBehaviour : ClassTag]: EntityBehaviourDescriptor = {
    EntityBehaviourConverter.behaviourToDescriptor(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  private def shouldRun[T: ShouldRunTester]: () => Boolean = {
    () => implicitly[ShouldRunTester[T]].shouldRun(entity, world)
  }

  private def makeStatePredicate[T <: EntityBehaviour : ClassTag : ShouldRunTester](pri: Int): StatePredicate = {
    StatePredicate(descriptorOf[T], pri, shouldRun[T])
  }

  private def getBehaviourToRun: Option[StatePredicate] = {
    behaviours.find(_.predicate())
  }

  private def runBehaviour(statePredicate: StatePredicate): Unit = {
    activeBehaviour.foreach(_.behaviour.cancel())
    activeBehaviour = Some(RunningBehaviour(statePredicate, entity.addBehaviour(statePredicate.descriptor)))
  }

  override def onReady(): Unit = {
    world.timing.every(1.second) {
      val behaviourToRun = getBehaviourToRun
      if (behaviourToRun.nonEmpty && activeBehaviour.map(_.statePredicate) != behaviourToRun) {
        behaviourToRun.foreach(runBehaviour)
      }
    }
  }
}
