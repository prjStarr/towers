using UnityEngine;
using System.Collections;
using Improbable.Unity;
using Improbable.Unity.Visualizer;
using Improbable.Corelibrary.Physical;
using Improbable.Unity.Common.Core.Math;
using Improbable.Corelib.Util;

public class ClientPositionVisualizer : MonoBehaviour {

    [Require] public TransformWriter ImprTransform;

	// Use this for initialization
	void OnEnable () 
    {
	
	}
	
	// Update is called once per frame
	void Update () 
    {
        ImprTransform.Update.Position(transform.position.ToCoordinates()).Rotation(transform.rotation.ToNativeQuaternion()).FinishAndSend();
	}
}
