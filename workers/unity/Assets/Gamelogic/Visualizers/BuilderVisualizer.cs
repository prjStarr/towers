using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Demoteam;

public class BuilderVisualizer : MonoBehaviour {

	[Require] public GeometryReader Geo;

	void OnEnable () 
	{	
	}

	public BuildGeometry GetGeometry()
	{
        return (enabled ? Geo.GeometryType : BuildGeometry.Cuboid);
	}
}
