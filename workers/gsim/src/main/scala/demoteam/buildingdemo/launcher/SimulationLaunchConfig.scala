package demoteam.buildingdemo.launcher

import improbable.apps.{BuildingDemoWorldAppList, SetupWorld}
import improbable.dapi.{Launcher, LaunchConfig}
import improbable.fapi.bridge._
import improbable.fapi.engine.CompositeEngineDescriptorResolver
import improbable.fapi.network.RakNetLinkSettings
import improbable.papi.worldapp.WorldApp
import improbable.unity.fabric.AuthoritativeEntityOnly
import improbable.unity.fabric.bridge.{FSimAssetContextDiscriminator, UnityClientBridgeSettings, UnityFSimBridgeSettings}
import improbable.unity.fabric.engine.{EnginePlatform, DownloadableUnityConstraintToEngineDescriptorResolver}
import improbable.unity.fabric.satisfiers.SatisfyPhysics

/**
 * These are the engine startup configs.
 *
 * ManualEngineStartup will not start an engines when you start the game.
 * AutomaticEngineStartup will automatically spool up engines as you need them.
 */
object SimulationLaunchWithManualEngineStartupConfig extends SimulationLaunchConfigWithApps(dynamicallySpoolUpEngines = false)

object SimulationLaunchWithAutomaticEngineStartupConfig extends SimulationLaunchConfigWithApps(dynamicallySpoolUpEngines = true)

/**
 * Use this class to specify the list of apps you want to run when the game starts.
 */
class SimulationLaunchConfigWithApps(dynamicallySpoolUpEngines: Boolean) extends SimulationLaunchConfig(BuildingDemoWorldAppList.apps, dynamicallySpoolUpEngines)

class SimulationLaunchConfig(appsToStart: Seq[Class[_ <: WorldApp]],
                             dynamicallySpoolUpEngines: Boolean) extends LaunchConfig(
  appsToStart,
  dynamicallySpoolUpEngines,
  DefaultBridgeSettingsResolver,
  DefaultConstraintEngineDescriptorResolver)

object DefaultBridgeSettingsResolver extends CompositeBridgeSettingsResolver(
  UnityClientBridgeSettings,
  UnityFSimBridgeSettings,
  PhysXWorkerBridgeSettings
)

object PhysXWorkerBridgeSettings extends BridgeSettingsResolver {

  override def engineTypeToBridgeSettings(engineType: String, metadata: String): Option[BridgeSettings] = {

    if (engineType == "PhysXWorker") {
      Some(BridgeSettings(
        FSimAssetContextDiscriminator(),
        RakNetLinkSettings(),
        EnginePlatform.UNITY_FSIM_ENGINE,
        SatisfyPhysics(maxAffinity = 9),
        AuthoritativeEntityOnly(),
        MetricsEngineLoadPolicy,
        PerEntityOrderedStateUpdateQos))
    }
    else {
      None
    }
  }
}

object DefaultConstraintEngineDescriptorResolver extends CompositeEngineDescriptorResolver(
  DownloadableUnityConstraintToEngineDescriptorResolver
)

object SimulationLauncherWithManualEngines extends SimulationLauncher(SimulationLaunchWithManualEngineStartupConfig)

object SimulationLauncherWithAutomaticEngines extends SimulationLauncher(SimulationLaunchWithAutomaticEngineStartupConfig)

class SimulationLauncher(launchConfig: LaunchConfig) extends App {
  val options = Seq(
    "--entity_activator=improbable.corelib.entity.CoreLibraryEntityActivator",
    "--resource_based_config_name=one-gsim-one-jvm"
    ,"--engine_automatic_scaling_enabled=true"
//    ,"--game_chunk_size=100"
//    ,"--engine_range=1000"
//    ,"--spatial_index_grid_size=5"
//    ,"--snapshot_write_period_seconds=0"
//    ,"--use_immutable_entity_search_space=true"
  )
  Launcher.startGame(launchConfig, options: _*)
}
