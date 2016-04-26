using UnityEngine;
using System.Collections;
using Improbable.Unity;
using Improbable.Unity.Visualizer;
using Demoteam;
using IoC;
using Improbable.Core.Entity;

[EngineType(EnginePlatform.Client)]
public class CarryIKVisualizer : MonoBehaviour {

    [Require] public CarryingReader Carry;

    [Inject] public IUniverse Universe { get; set; }

    private Transform CarryPoint;

	// Use this for initialization
	void Awake()
    {
        CarryPoint = transform.FindChild("CarryPoint");
    }

    void OnEnable()
    {

    }

    void LateUpdate()
    {
        if(Carry.CarryingId.HasValue && CarryPoint!=null)
        {
            var entObj = Universe.Get(Carry.CarryingId.Value);
            if (entObj != null && entObj.UnderlyingGameObject!=null)
            {
                entObj.UnderlyingGameObject.transform.position = CarryPoint.position;
                entObj.UnderlyingGameObject.transform.rotation = CarryPoint.rotation;
            }
        }
    }
}
