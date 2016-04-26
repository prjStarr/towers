using UnityEngine;
using System.Collections;

public class PlayFxOnStart : MonoBehaviour {

	// Use this for initialization
	void OnEnable() 
    {
        var fx = GetComponent<PKFxFX>();
        if(fx!=null)
        {
            fx.StartEffect();
        }
	}

    void Update()
    {
        if (!(gameObject.activeSelf || gameObject.activeInHierarchy))
        {
            StopFx();
        }
    }

    void StopFx()
    {
        var fx = GetComponent<PKFxFX>();
        if (fx != null)
        {
            fx.StopEffect();
        }
    }

    void OnDisable()
    {
        StopFx();
    }
}
