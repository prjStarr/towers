using UnityEngine;
using System.Collections;

public class Spinner : MonoBehaviour {

    public float Frequency;
    	
	// Update is called once per frame
	void Update () {
	
        float degreesPerSecond = 360 * Frequency;
        transform.rotation *= Quaternion.AngleAxis(degreesPerSecond * Time.deltaTime, Vector3.up);
	}
}
