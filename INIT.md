### Initializing your project

In order to rename your project you will need to do the following steps:

* Go to `\project\BuildConfiguration.scala` edit `projectName`, `organisation` and `version` to match your project name
and organisation. Your Organisation my have spaces but the ProjectName cannot. For example:

```
import improbable.build._
import improbable.build.fabric._
import improbable.build.unity._
import improbable.build.util.Versions
import improbable.sdk.SdkInfo

object BuildConfiguration extends improbable.build.ImprobableBuild(
  projectName = "MyNewProject",
  organisation = "My Organisation",
  version = Versions.fetchVersion("MyNewProject"),
  isLibrary = false,
  buildSettings = Seq(FabricBuildSettings(), UnityPlayerProject()),
  dependencies = List(new SimulationLibrary("improbable", "core-library", SdkInfo.version))
)
```

* You must move your migration files. The package that they must be in is `myorganisation.mynewproject.migrations` (with
your project name and organisation). Currently the example migration file is situated in the package
`improbable.blankproject.migrations` at `\Spec\src\main\scala\improbable\blankproject\migrations\ExampleMigration.scala`.
Move it to `\Spec\src\main\scala\myorganisation\mynewproject\migrations\ExampleMigration.scala` and change the first
line of the file to: `package myorganisation.mynewproject.migrations`

* Go to `Gamelogic\src\main\resources\game.properties` and edit the first line of the file to `game.name = MyNewProject`

* Go to `Gamelogic\src\main\scala\improbable\apps\BlankProjectWorldAppList.scala` rename the file to:
`Gamelogic\src\main\scala\improbable\apps\MyNewProjectWorldAppList.scala` and change object in the package to:
`MyNewProjectWorldAppList`.

* You may move the `improbable.blankproject.launcher` package to your own project's launcher directory.

* Open `Engines\Unity\Editor\Assets\Bootstrap.cs` and change `AppName = "BlankProject"` to `AppName = "MyNewProject"`