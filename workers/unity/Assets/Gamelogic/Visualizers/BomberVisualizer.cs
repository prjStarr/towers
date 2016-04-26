using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Improbable.Core.Entity;
using Improbable.Entity;
using Demoteam;

public class BomberVisualizer : MonoBehaviour {

    [Require] public ExplosiveTriggerWriter Explo;

    private Camera Cam;

    void Awake()
    {
        Cam = GetComponentInChildren<Camera>();
    }

	// Use this for initialization
	void OnEnable () {
	}
	
	// Update is called once per frame
	void Update () {
        if(Input.GetMouseButtonUp(0))
        {
            var ray = Cam.ScreenPointToRay(Input.mousePosition);

            RaycastHit hit;
            if(Physics.Raycast(ray, out hit, 100.0f))
            {
                var entityId = hit.collider.gameObject.EntityId();
                if (entityId != Improbable.EntityId.InvalidEntityId)
                {
                    Explo.Update.TriggerSetTimer(entityId).FinishAndSend();
                }
            }
        }

	}
}
