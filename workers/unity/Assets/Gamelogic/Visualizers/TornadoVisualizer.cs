using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Improbable.Unity;
using Improbable.Unity.Visualizer;
using Improbable.Math;
using Demoteam;
using Improbable.Util.Collections;
using Vector3 = UnityEngine.Vector3;

[EngineType(EnginePlatform.FSim)]
public class TornadoVisualizer : MonoBehaviour {

    [Require] public PathReader Path;
    [Require] public PathFollowerReader Follower;

    public float TangentialForceCoefficient = 250.0f;
    public float RadialForceCoefficient = 1.0f;
    public float VerticalForceCoefficient = 150.0f;
    public float VelocitySpringConstant = 0.05f;

    public float RotationFrequency = 1.0f;

    private List<Rigidbody> Detritus;
    private float Radius;
    private float _PathLength;

    static float Pow4(float f)
    {
        return Square(f) * Square(f);
    }
    static float Tanh(float x)
    {
        var e2x = System.Math.Exp(2 * x);
        return (float)((e2x - 1) / (e2x + 1));
    }

    float PathLength { get { return _PathLength > 0.01f ? _PathLength : 1000.0f; } }

    float GetStrengthFraction()
    {
        var interp = (float)Follower.PathDistance / PathLength;
        var rampf = 100.0f;
        return Pow4(Tanh(rampf * interp)) + Pow4(Tanh(rampf * (interp - 1.0f))) - 1.0f;
    }

    void Awake()
    {
        _PathLength = 1000.0f;

        Detritus = new List<Rigidbody>();
        var sphere = GetComponent<SphereCollider>();
        if(sphere!=null)
        {
            Radius = sphere.radius;
        }
    }

	// Use this for initialization
	void OnEnable () {
        Detritus.Clear();

        Path.PathWaypointsUpdated += PathWaypointsUpdated;
	}

    void PathWaypointsUpdated(IReadOnlyList<Coordinates> waypoints)
    {
        _PathLength = PathFollowerVisualizer.CalculatePathLength(waypoints);
    }

    void OnTriggerEnter(Collider col)
    {
        var rb = col.GetComponent<Rigidbody>();
        if(rb!=null)
        {
            Detritus.Add(rb);
        }
    }

    void OnTriggerExit(Collider col)
    {
        var rb = col.GetComponent<Rigidbody>();
        if (rb != null)
        {
            Detritus.Remove(rb);
        }
    }

    static float Square(float f)
    {
        return f * f;
    }
	
	// Update is called once per frame
	void FixedUpdate () 
    {
        
        var strengthf = GetStrengthFraction();

        for(var idetritus = 0; idetritus<Detritus.Count; ++idetritus)
        {
            var body = Detritus[idetritus];
            if (body != null)
            {
                var lineInXZ = Vector3.Scale(transform.position - body.position, Vector3.forward + Vector3.right);

                var force = Vector3.zero;

                var radsPerSecond = Mathf.PI*2*RotationFrequency;
                var targetSpeed = radsPerSecond * lineInXZ.magnitude;

                // add a little force in
                if (lineInXZ.sqrMagnitude > 1.0f)
                {
                    //var velXZ = Vector3.Scale(body.velocity, Vector3.forward + Vector3.right);
                    //var tangentDir = Vector3.Cross(lineInXZ.normalized, Vector3.up);
                    //var tangentSpeed = Vector3.Dot(tangentDir, velXZ);
                    force += lineInXZ.normalized * Square(targetSpeed) * RadialForceCoefficient / Radius;
                }

                // add a little force up
                force += Vector3.up * VerticalForceCoefficient / (1 + lineInXZ.magnitude);

                // tangential - treat like a spring pulling us to the targetSpeed

                var velXZ = Vector3.Scale(body.velocity, Vector3.forward + Vector3.right);
                var tangentDir = Vector3.Cross(lineInXZ.normalized, Vector3.up);
                var tangentSpeed = Vector3.Dot(tangentDir, velXZ);

                var perpXZ = Vector3.Cross(lineInXZ.normalized, Vector3.up);
                force += perpXZ*(targetSpeed - tangentSpeed)*VelocitySpringConstant;


                
                force += perpXZ * TangentialForceCoefficient / perpXZ.sqrMagnitude;

                body.AddForce(force * strengthf, ForceMode.Force);
            }
        }
    }
}
