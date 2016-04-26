using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Demoteam;

public class CarriedVisualizer : MonoBehaviour {

    [Require] public CarriedReader Carried;

	// Use this for initialization
	void OnEnable () {
	
	}

    public bool IsCarried()
    {
        return Carried != null && Carried.CarrierId.HasValue;
    }
}
