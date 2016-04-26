package templates

import org.flagz.{FlagInfo, FlagContainer, ScalaFlagz}

object Parameters extends FlagContainer {

  @FlagInfo(help="")
  val number_of_cubes=ScalaFlagz.valueOf(1000)

  @FlagInfo(help="")
  val number_of_gatherers=ScalaFlagz.valueOf(25)

  @FlagInfo(help="")
  val cube_spawn_radius = ScalaFlagz.valueOf(15)

  @FlagInfo(help="")
  val cube_up_impulse = ScalaFlagz.valueOf(1)

  @FlagInfo(help="")
  val cube_lateral_impulse = ScalaFlagz.valueOf(5)

  @FlagInfo(help="")
  val cube_impulse_interval_millis = ScalaFlagz.valueOf(1500)


}
