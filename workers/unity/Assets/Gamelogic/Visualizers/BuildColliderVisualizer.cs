using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;
using Improbable.Unity.Visualizer;
using Improbable.Unity;
using Demoteam;

[EngineType(EnginePlatform.FSim)]
public class BuildColliderVisualizer : MonoBehaviour {

    //[Require] public BuildReader Build;
    [Require] public GeometryReader Geo;
    
    private Dictionary<BuildGeometry, Action<Transform>> ColliderActivators = new Dictionary<BuildGeometry, Action<Transform>>()
    {
        {BuildGeometry.Cuboid, (Transform transform) => BuildColliderVisualizer.EnableComponent<BoxCollider>(transform)},
        {BuildGeometry.Cylinder, (Transform transform) => BuildColliderVisualizer.EnableComponent<SphereCollider>(transform)}
    };

	void OnEnable()
    {
        Geo.GeometryTypeUpdated += GeometryTypeUpdated;
    }

    static void EnableComponent<T>(Transform tr) where T:Collider
    {
        var comps = tr.GetComponentsInChildren<T>(true);
        if (comps != null)
        {
            foreach (var c in comps)
            {
                //c.gameObject.SetActive(true);
                c.enabled = true;
            }
        }
    }

    void GeometryTypeUpdated(BuildGeometry geo)
    {
        Action<Transform> ac;
        if(ColliderActivators.TryGetValue(geo, out ac))
        {
            ac(transform);
        }
    }
}
