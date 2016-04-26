using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Demoteam;

public class TeamColourVisualizer : MonoBehaviour {

	public Material[] CylinderMaterials;
	public Material[] CuboidMaterials;
    public Material[] PyramidMaterials;

	[Require] public GeometryReader Geo;

	// Use this for initialization
	void OnEnable () 
	{
        Geo.GeometryTypeUpdated += GeometryTypeUpdated;
	}

    void GeometryTypeUpdated(BuildGeometry obj)
	{
		switch(obj)
		{
		case BuildGeometry.Cuboid:
			SetMaterials(CuboidMaterials);
			break;
		case BuildGeometry.Cylinder:
			SetMaterials(CylinderMaterials);
			break;
        case BuildGeometry.Pyramid:
            SetMaterials(PyramidMaterials);
            break;
		default:
			break;
		}
	}

	void SetMaterials(Material[] m)
	{
		var rs = GetComponentsInChildren<Renderer>();
		foreach(var r in rs)
		{
			r.materials = m;
		}
	}	
}
