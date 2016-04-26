using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Improbable.Unity.Visualizer;
using Improbable.Unity;
using Demoteam;
using Improbable.Core.Entity;

[EngineType(EnginePlatform.FSim)]
public class ExclusionVisualizer : MonoBehaviour {

    private int GathererLayer;
    private List<Rigidbody> Objects;
    private Collider CachedCollider;

    [Require] public GeometryReader Geo;

    static float Square(float f)
    {
        return f * f;
    }

	void Awake () {

        GathererLayer = LayerMask.NameToLayer("Gatherer");
        Objects = new List<Rigidbody>();
        CachedCollider = GetComponent<Collider>();
	}

    float GetRadius()
    {
        var col = CachedCollider as SphereCollider;
        return col!=null?col.radius:1.0f;
    }

    Vector3 GetSize()
    {
        var col = CachedCollider as BoxCollider;
        return col != null ? col.size : Vector3.one;
    }

    GameObject GetImprobableObject(GameObject obj)
    {
        if(obj.GetComponent<EntityObjectStorage>()!=null)
        {
            return obj;
        }
        if(obj.transform.parent!=null)
        {
            return GetImprobableObject(obj.transform.parent.gameObject);
        }
        return null;        
    }
        
    void OnTriggerEnter(Collider other)
    {
		var rb = other.GetComponentInParent<Rigidbody>();
        if (rb != null && rb.gameObject.layer == GathererLayer && !Objects.Contains(rb))
        {
			var builderv = rb.GetComponent<BuilderVisualizer>();
			if(builderv && Geo.GeometryType==builderv.GetGeometry())
			{
				Objects.Add(rb);
			}
        }
    }
    void OnTriggerExit(Collider other)
    {
        var rb = other.GetComponentInParent<Rigidbody>();
        if (rb != null && rb.gameObject.layer == GathererLayer)
        {
            Objects.Remove(rb);
        }
    }

    void UpdateCylinderExclusion()
    {
        foreach (var rb in Objects)
        {
            var line = Vector3.Scale(rb.position - transform.position, Vector3.right + Vector3.forward);
            var rad = GetRadius();
            if (line.sqrMagnitude < Square(rad))
            {
                rb.MovePosition(Vector3.Scale(transform.position + line.normalized * rad, Vector3.right + Vector3.forward) + Vector3.Scale(rb.position, Vector3.up));
            }
        }
    }

    Vector3 GetExclusionDirectionMs(Vector3 lineMs)
    {
        var cosAngle = Vector3.Dot(lineMs.normalized, Vector3.forward);
        var perp = Vector3.Cross(lineMs, Vector3.forward);

        var oneOnRootTwo = 0.707f;

        if (cosAngle > oneOnRootTwo)
        {
            return Vector3.forward;
        }
        else if (cosAngle < -oneOnRootTwo)
        {
            return Vector3.back;
        }
        else if (Vector3.Dot(perp, Vector3.up) > 0.0f)
        {
            return Vector3.left;
        }
        else
        {
            return Vector3.right;
        }
    }

    void UpdateCuboidExclusion()
    {
        foreach (var rb in Objects)
        {
            var lineWs = Vector3.Scale(rb.position - transform.position, Vector3.right + Vector3.forward);
            if (lineWs.sqrMagnitude > 0.001f)
            {
                var lineMs = transform.InverseTransformVector(lineWs);
                var exclusionDirMs = GetExclusionDirectionMs(lineMs);
                var distance = (Mathf.Abs(Vector3.Dot(GetSize() / 2, exclusionDirMs)) - Vector3.Dot(lineMs, exclusionDirMs));
                if (distance > 0.0f)
                {
                    rb.MovePosition(Vector3.Scale(rb.position + exclusionDirMs * distance, Vector3.right + Vector3.forward) + Vector3.Scale(rb.transform.position, Vector3.up));
                }
            }
        }
    }

    void FixedUpdate()
    {
        Objects.Remove(null);

        switch (Geo.GeometryType)
        {
        case BuildGeometry.Cuboid:
        case BuildGeometry.Pyramid:
            UpdateCuboidExclusion();
            break;
        case BuildGeometry.Cylinder:
            UpdateCylinderExclusion();
            break;
        default:
            break;
        };
    }
}
