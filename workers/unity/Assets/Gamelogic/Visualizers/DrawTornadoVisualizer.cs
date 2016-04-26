using UnityEngine;
using System.Collections;
using Improbable;
using Improbable.Unity.Visualizer;
using Demoteam;
using Improbable.Unity;

public class DrawTornadoVisualizer : MonoBehaviour {

    [Require] public TornadoPainterWriter Painter;

    public float DistanceThreshold = 0.5f;
    public float MaxRange = 1000.0f;

    private Camera Cam;
    private Vector3? LastPos;
    private int LayerIdx;

    void Awake()
    {
        LayerIdx = LayerMask.NameToLayer("Ground");
    }

	// Use this for initialization
	void OnEnable() {
        Cam = GetComponentInChildren<Camera>();
	}

    static float Square(float f)
    {
        return f * f;
    }
	
	// Update is called once per frame
	void Update () {
        if(Input.GetMouseButtonDown(0))
        {
            var ray = Cam.ScreenPointToRay(Input.mousePosition);

            RaycastHit hit;
            if (Physics.Raycast(ray, out hit, MaxRange) && hit.collider.gameObject.layer == LayerIdx)
            {
                Painter.Update.TriggerCreateTornado(hit.point.ToCoordinates()).FinishAndSend();
                LastPos = hit.point;
            }
        }
        else if (LastPos!=null)
        {
            if(Input.GetMouseButton(0))
            {
                var ray = Cam.ScreenPointToRay(Input.mousePosition);
                RaycastHit hit;
                if (Physics.Raycast(ray, out hit, MaxRange, 1<<LayerIdx))
                {
                    if ((hit.point - LastPos.Value).sqrMagnitude > Square(DistanceThreshold))
                    {
                        Painter.Update.TriggerUpdatePath(hit.point.ToCoordinates()).FinishAndSend();
                        LastPos = hit.point;
                    }
                }
            }
            else if(Input.GetMouseButtonUp(0))
            {
                Painter.Update.TriggerFinishPath().FinishAndSend();
                LastPos = null;
            }
        }
	}
}
