using UnityEngine;
using System.Collections;
using Improbable;
using Improbable.Unity;
using Improbable.Unity.Visualizer;
using IoC;
using Improbable.Core.Entity;
using Demoteam;

[EngineType(EnginePlatform.FSim)]
public class CarryVisualizer : MonoBehaviour {

    [Require] public CarryingReader Carrying;

    [Inject] public IUniverse Universe { get; set; }

    public float RangeMin = 1.75f;
    public float RangeMax = 3.0f;
    private Transform CarryPoint;
    private FixedJoint Joint;

    void Awake()
    {
        CarryPoint = transform.FindChild("CarryPoint");
    }

    void OnEnable()
    {
        Carrying.CarryingIdUpdated += CarryingIdUpdated;
    }

    void OnDisable()
    {
        if (Joint != null)
        {
            Component.Destroy(Joint);
            Joint = null;
        }
    }

    static float Square(float a)
    {
        return a * a;
    }

    void CarryingIdUpdated(EntityId? entId)
    {
        if (Joint != null)
        {
            if (Joint.connectedBody != null)
            {
                Joint.connectedBody.velocity = Vector3.zero;
                Joint.connectedBody.angularVelocity = Vector3.zero;
            }
            Component.Destroy(Joint);
        }

        if(entId.HasValue)
        {
            var entObj = Universe.Get(entId.Value);
            if (entObj != null && entObj.UnderlyingGameObject!=null)
            {
                var carriedBody = entObj.UnderlyingGameObject.GetComponent<Rigidbody>();
                if (carriedBody != null)
                {
                    carriedBody.transform.position = GetCarryAnchor();
                    carriedBody.transform.rotation = GetCarryOrientation();

                    var joint = carriedBody.gameObject.AddComponent<FixedJoint>();
                    joint.connectedBody = GetComponent<Rigidbody>();
                    joint.connectedAnchor = CarryPoint.localPosition;
                    joint.enableCollision = false;

                    Joint = joint;
                }
            }
        }
    }

    private Vector3 GetCarryAnchor()
    {
        if(CarryPoint!=null)
        {
            return CarryPoint.position;
        }
        return transform.position;
    }
    private Quaternion GetCarryOrientation()
    {
        if(CarryPoint!=null)
        {
            return CarryPoint.rotation;
        }
        return transform.rotation;
    }
}
