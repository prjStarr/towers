using UnityEngine;
using System.Collections;
using Improbable;
using Improbable.Unity.Visualizer;
using Demoteam;

public class BuildPieceVisualizer : MonoBehaviour {

    [Require] public BuildingPieceReader Piece;
    [Require] public ExplosiveReader Explosive;

    public bool IsPartOfBuild()
    {
        return Piece != null && Piece.BuildId.HasValue;
    }

    public Material[] BuildMaterials;
    public Material[] ExplosiveMaterials;
    private Material[] DefaultMaterials;

    private Material[] MaterialsCache;

    void Awake()
    {
        var mr = GetComponent<MeshRenderer>();
        if (mr != null)
        {
            DefaultMaterials = mr.materials;
        }
        MaterialsCache = new Material[1];
    }

	// Use this for initialization
	void OnEnable () 
    {
        Piece.BuildIdUpdated += BuildIdUpdated;
		Piece.GeometryUpdated += GeometryUpdated;
        Explosive.ArmedByUpdated += ArmedByUpdated;
	}

    void ArmedByUpdated(BuildGeometry? obj)
    {
        if(obj.HasValue)
        {
            MaterialsCache[0] = ExplosiveMaterials[(int)obj.Value];
            SetMaterials(MaterialsCache);
        }

    }
    
    void SetMaterials(Material[] mats)
    {
        ChangeMaterial.ChangeMaterialsOnObject(gameObject, mats);
    }

    void UpdateMaterials()
    {
        if(Explosive.ArmedBy.HasValue)
        {
            ArmedByUpdated(Explosive.ArmedBy.Value);
        }
        else if(Piece.BuildId.HasValue)
        {
            MaterialsCache[0] = BuildMaterials[(int)Piece.Geometry];
            SetMaterials(MaterialsCache);
        }
        else
        {
            SetMaterials(DefaultMaterials);
        }        
    }

    void BuildIdUpdated(EntityId? builderId)
    {
        UpdateMaterials();
    }
	void GeometryUpdated(BuildGeometry geo)
	{
		UpdateMaterials();
	}
    void TimerUpdated(double time)
    {
        UpdateMaterials();
    }
}
