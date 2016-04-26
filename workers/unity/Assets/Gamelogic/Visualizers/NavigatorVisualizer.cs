using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Demoteam;
using Improbable;
using Improbable.Math;
using Improbable.Unity;
using Improbable.Unity.Common.Core.Math;
using Vector3 = UnityEngine.Vector3;
using IoC;
using Improbable.Core.Entity;

[EngineType(EnginePlatform.FSim)]
public class NavigatorVisualizer : MonoBehaviour {

    [Require]
    public NavigationReader Nav;

	[Inject] public IUniverse Universe { get; set; }

    private bool Updating
    {
        get
        {
            return _Updating || TargetEntity != null;
        }
    }

    private bool _Updating;
    private Rigidbody Body { get { return _Body == null ? (_Body = GetComponent<Rigidbody>()) : _Body; } }
    private Rigidbody _Body;

    public float MaxSpeed = 4.0f;
    public float RotationSpeed = 1.0f;
    public float CorrectiveMoment = 0.0f;
    public float DragCoefficient = 0.0f;
    public float AngularDragCoefficient = 0.0f;

    private Transform TargetEntity
    {
        get
        {
            if (Nav.TargetEntity.HasValue)
            {
                var eobj = Universe.Get(Nav.TargetEntity.Value);
                if (eobj != null && eobj.UnderlyingGameObject != null)
                {
                    return eobj.UnderlyingGameObject.transform;
                }
            }
            return null;
        }
    }

    private static int NumCalls = 0;

	void OnEnable () 
    {
        _Updating = false;
        Nav.TargetPosUpdated += TargetPosUpdated;
	}

    void TargetPosUpdated(Coordinates? obj)
    {
        _Updating = obj.HasValue || TargetEntity!=null;
    }

    private Vector3 TargetPos
    {
        get
        {
            var targetEnt = TargetEntity;
            if (targetEnt != null)
            {
                return targetEnt.position;
            }
            else if(Nav.TargetPos.HasValue)
            {
                return Nav.TargetPos.Value.ToUnityVector();
            }
            return transform.position;
        }
    }

    static Vector3 toXZ(Vector3 v)
    {
        return Vector3.Scale(v, Vector3.forward + Vector3.right);
    }
    static float Square(float f)
    {
        return f * f;
    }

    void UpdateMovement()
    {
        var lineToTargetXZ = toXZ(TargetPos - transform.position);
        if (lineToTargetXZ.sqrMagnitude > Square((float)Nav.TargetRange))
        {
            float envelopeCoefficient = 0.12f;
            float min = 0.1f;
            float cosAngle = Vector3.Dot(lineToTargetXZ.normalized, transform.forward);
            float envelope = Mathf.Exp(-envelopeCoefficient*Square(cosAngle));
            float targetVelMul = (1.0f - min) * cosAngle * envelope;
            targetVelMul += (targetVelMul > 0.0f) ? min : -min;

            // drag
            Body.velocity += -Body.velocity*Body.velocity.magnitude* DragCoefficient * Time.fixedDeltaTime;

            Body.velocity += transform.forward * targetVelMul * MaxSpeed * Time.fixedDeltaTime;
        }
    }

    void UpdateTurning()
    {
        var lineToTargetXZ = toXZ(TargetPos - transform.position);
        if (lineToTargetXZ.sqrMagnitude > Square((float)Nav.TargetRange))
        {
            Body.angularVelocity += -Body.angularVelocity * AngularDragCoefficient * Time.fixedDeltaTime;
            
            var fwdXZ = toXZ(transform.forward);
            if(fwdXZ.sqrMagnitude>0.001f)
            {
                var twist = Vector3.Cross(fwdXZ.normalized, lineToTargetXZ.normalized);
                twist = twist.sqrMagnitude > 0.001f ? twist.normalized : Vector3.zero;

			    var rotationSpeed = RotationSpeed;//*Mathf.Lerp (0.2f, 1.0f, Mathf.Min(Body.velocity.sqrMagnitude/25.0f, 1.0f));
			    Body.angularVelocity += twist * rotationSpeed * Time.fixedDeltaTime;
            }
        }
    }

    void UpdateUprightMoment()
    {
        var myUp = Body.transform.up;
        var axis = Vector3.Cross(myUp, Vector3.up);
        var cosAngle = Vector3.Dot(Vector3.up, myUp);

        Body.angularVelocity += axis * (1.0f - cosAngle) * CorrectiveMoment;
    }
	
	void FixedUpdate () 
    {
        if(Updating && Body!=null)
        {
            UpdateMovement();
            UpdateTurning();
            UpdateUprightMoment();
        }
	}
}
