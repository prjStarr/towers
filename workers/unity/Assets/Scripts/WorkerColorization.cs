using UnityEngine;
using System.Collections;

public class WorkerColorization : MonoBehaviour {

    public Shader ColorizationShader;
    private Camera Cam;

	// Use this for initialization
    void Awake()
    {
        Cam = GetComponent<Camera>();
    }
	
	// Update is called once per frame
	void Update () {

        if(Input.GetKeyUp(KeyCode.F1))
        {
            if (!Cam.enabled)
            {
                //Cam.CopyFrom(Camera.main);
                Cam.SetReplacementShader(ColorizationShader, "");
                Cam.enabled = true;
            }
            else
            {
                Cam.ResetReplacementShader();
                Cam.enabled = false;
            }
        }
	}
}
