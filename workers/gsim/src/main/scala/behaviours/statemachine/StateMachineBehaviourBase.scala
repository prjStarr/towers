package behaviours.statemachine

import behaviours.agent.NavigationInterface
import improbable.Cancellable
import improbable.corelib.util.QuaternionUtils
import improbable.entity.physical.RigidbodyInterface
import improbable.math.{Vector3d, Coordinates}
import improbable.papi.entity.{EntityBehaviour, Entity}
import improbable.papi.world.World
import StateMachineBehaviourBase._

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait StateMachineVectorHelper {
  def directionToXZ(pos: Coordinates): Vector3d

  def withinRangeOfXZ(pos: Coordinates, range: Double): Boolean

  val filterXZ = Vector3d.unitX + Vector3d.unitZ

  def square(d: Double): Double = d * d

  def sqrMag(v: Vector3d): Double = v.dot(v)

  def randomDirectionXZ: Vector3d = {
    val angle = Math.random().toFloat * 360.0f
    QuaternionUtils.fromAngleAxis(angle, Vector3d.unitY) * Vector3d.unitZ
  }

  def factorial(i: Int): Double = {
    if (i == 0) {
      1
    }
    else {
      i * factorial(i - 1)
    }
  }

  def sign(n:Int) : Double = {
    if ((n % 2) == 0) {
      1.0
    } else {
      -1.0
    }
  }

  val sqrtPi = math.sqrt(math.Pi)

  def errorFunction(x: Double, ep: Double): Double = {

    val oi = (0 to 12).find(i => ep * factorial(i) > 1.0)

    val res = oi.map {
      i=>
        (0 to i).foldLeft(0.0) {
          (sum, n) => sum + sign(n)*math.pow(x, 2*n+1)/(factorial(n)*(2*n+1))
        }
    }

    res.getOrElse(0.0)*2.0/math.sqrt(math.Pi)
  }

  def integralOfGaussian(sqrtk:Double, x0:Double, x1:Double, ep:Double) : Double = {
    (sqrtPi/(2*sqrtk)) * (errorFunction(sqrtk*x1, ep)-errorFunction(sqrtk*x0, ep))
  }

  def biasedRandomDirectionXZ(cardinalAxis: Vector3d, bias: Double): Vector3d = {
    val x = Math.random()
    val angle = 360.0*math.pow(x, 2/bias-1)
    QuaternionUtils.fromAngleAxis(angle.toFloat, Vector3d.unitY) * cardinalAxis
  }
}

abstract class StateMachineBehaviourBase(entity: Entity, world: World, navigation: NavigationInterface) extends EntityBehaviour with StateMachineVectorHelper {

  import templates.Parameters._

  private var update: Option[Cancellable] = None

  def updateAction(): Unit

  def start(): Unit = {

  }

  def actionInterval: Double = cube_impulse_interval_millis.get.toDouble

  protected def clearUpdate(): Unit = {
    update.foreach(_.cancel())
    update = None
  }

  override def onReady(): Unit = {
    attemptToRun()
  }

  override def onRemove(): Unit = {
    stop()
  }

  protected def stop(): Unit = {
    clearUpdate()
    navigation.clear()
  }

  def attemptToRun(): Unit = {
    if (update.isEmpty) {

      start()

      update = Some(world.timing.every(actionInterval.millis) {
        updateAction()
      })
    }
  }

  def moveTowards(pos: Coordinates): Unit = {
    navigation.moveToPosition(pos, defaultNavigationRange)
  }

  def moveInDirection(dir: Vector3d): Unit = {
    //rigidbody.applyImpulse(dir * cube_lateral_impulse.get.toDouble)
  }

  override def directionToXZ(pos: Coordinates): Vector3d = {

    val lineXZ = (pos - entity.position) * filterXZ
    if (sqrMag(lineXZ) > epsilon) {
      lineXZ.normalised
    }
    else {
      Vector3d.unitZ
    }
  }

  override def withinRangeOfXZ(pos: Coordinates, range: Double): Boolean = {
    sqrMag((pos - entity.position) * filterXZ) < square(range)
  }
}

trait AIBehaviour {
  def staticShouldRun(entity: Entity, world: World): Boolean
}

object StateMachineBehaviourBase {
  val epsilon = 0.001
  val defaultNavigationRange = 0.1
}