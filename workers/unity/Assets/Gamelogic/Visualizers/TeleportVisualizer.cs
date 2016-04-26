using UnityEngine;
using System.Collections;
using Demoteam;
using Improbable.Unity.Visualizer;
using Improbable.Math;
using Improbable.Unity;
using Improbable.Unity.Common.Core.Math;
using Vector3 = UnityEngine.Vector3;

public class TeleportVisualizer : MonoBehaviour {

    [Require] public TeleporterReader Tele;

	// Use this for initialization
	void OnEnable () {
	
        Tele.Teleport += Teleport;
	}

    void Teleport(Teleport msg)
    {
        transform.position = msg.Pos.ToUnityVector();
        transform.rotation = Quaternion.Euler(msg.Rot.ToUnityVector());

        var rb = GetComponent<Rigidbody>();
        if(rb!=null)
        {
            rb.angularVelocity = Vector3.zero;
            rb.velocity = Vector3.zero;
        }
    }
}
