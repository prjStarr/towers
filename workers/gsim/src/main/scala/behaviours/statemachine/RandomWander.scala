package behaviours.statemachine

import behaviours.agent.NavigationInterface
import improbable.math.{Coordinates, Vector3d}
import improbable.papi.EntityId
import improbable.papi.entity.Entity
import improbable.papi.world.World
import RandomWander._

/**
 * Copyright (c) 2015 Improbable Worlds Ltd.
 * All Rights Reserved
 * Date: 17/12/2015
 * Time: 11:48
 */

class RandomWander(entity: Entity, world: World, navigation: NavigationInterface) extends StateMachineBehaviourBase(entity, world, navigation) {

  override def actionInterval: Double = wanderInterval

  override def updateAction(): Unit = {

    val dir = (Coordinates.zero-entity.position)*(Vector3d.unitX+Vector3d.unitZ)
    val ndir = if(dir.magnitude>0.01) {
      dir.normalised
    }
    else {
      Vector3d.unitZ
    }

    val newPos = entity.position + biasedRandomDirectionXZ(ndir, 0.95)*distanceToWander

    if(entity.entityId.toString=="10") {
      println("entity10 is updating position target")
    }
    moveTowards(newPos)
  }
}

object RandomWander {
  val distanceToWander = 8.0
  val wanderInterval = 5000

  def staticShouldRun(entity: Entity, world:World): Boolean = true

  implicit val _ = new ShouldRunTester[RandomWander] {
    def shouldRun(entity: Entity, world: World): Boolean = staticShouldRun(entity, world)
  }
}