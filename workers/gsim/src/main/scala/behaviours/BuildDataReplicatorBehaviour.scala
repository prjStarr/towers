package behaviours

import demoteam.{Build, BuildMutableWriter}
import improbable.papi.entity.{Entity, EntityBehaviour}

class BuildDataReplicatorBehaviour(entity:Entity, state:BuildMutableWriter) extends EntityBehaviour {

  def setNumBlocks(n:Int) : Unit = {
    state.update.numBlocks(n).finishAndSend()
  }

  def setMaxBlocks(n:Int) : Unit = {
    state.update.maxBlocks(n).finishAndSend()
  }

  override def onReady() : Unit = {
    val build = entity.watch[Build]
    build.bind.numBlocks(setNumBlocks)
    build.bind.maxBlocks(setMaxBlocks)
  }

}
