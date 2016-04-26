﻿using UnityEngine;
using System.Collections;
using Improbable;
using Improbable.Unity.Visualizer;
using System;
using Improbable.Core;
using Improbable.Unity;
using Improbable.Unity.Entity;
using Improbable.Unity.Core;
using Improbable.Core.Network;
using System.Collections.Generic;
using Improbable.Fapi.Receptionist;

public class Bootstrap : MonoBehaviour, IBootstrapHandler {
    
    public string ReceptionistIp = "localhost";
    public int ReceptionistPort = 7777;
    public EnginePlatform EngineType = EnginePlatform.Client;
    public int FixedUpdateRate = 20;
    public int TargetFps = 120;
    public bool UsePrefabPooling = true;
    public LinkProtocol LinkProtocol = LinkProtocol.Tcp;
    public bool UseInstrumentation = true;

    public void Start()
    {
        var engineConfiguration = new EngineConfiguration
        {
            Ip = ReceptionistIp,
            Port = ReceptionistPort,
            TargetFps = TargetFps,
            FixedUpdateRate = FixedUpdateRate,
            UsePrefabPooling = UsePrefabPooling,
            PrefabToPool = Prepool(),
            EngineType = EngineTypeUtils.ToEngineName(EngineType),
            UseInstrumentation = UseInstrumentation,
            IsDebugMode = true,
            LinkProtocol = LinkProtocol,
            AppName = "buildingdemo",
            AssemblyName = "",
            MsgProcessLimitPerFrame = 0,
            Log4netConfigXml = "log4net-local.xml"
        };
        EngineLifecycleManager.StartGame(this, gameObject, engineConfiguration);
    }

    private static Dictionary<string, int> Prepool()
    {
        return new Dictionary<string, int>();
    }

    private static Dictionary<string, string> AdditionalMetaData()
    {
        return new Dictionary<string, string>();
    }

    /** IBoostrapHandler methods **/

    public void OnDeploymentListRetrieved(IList<IDeployment> deployments, Action<IDeployment> handleChosenDeployment)
    {
        handleChosenDeployment(deployments[0]);
    }

    public void OnQueuingStarted()
    {
        Debug.Log("Queueing started");
    }

    public void OnQueuingUpdate(IQueueStatus status)
    {
        Debug.Log(status);
    }

    public void OnQueuingCompleted(IQueueStatus status)
    {
        Debug.Log("Queueing complete");
    }

    public void OnBootstrapError(Exception exception)
    {
        Debug.LogError("Exception: " + exception.Message);
    }

    public void BeginPreconnectionTasks(IDeployment deployment, IContainer container, Action onCompletedPreconnectionTasks)
    {
        onCompletedPreconnectionTasks();
    }
}
