using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Improbable.Unity.Visualizer;
using Demoteam;
using IoC;
using Improbable.Core.Entity;
using Improbable;

public class CaptureCubesVisualizer : MonoBehaviour {

    private int CubeLayer;
    private List<EntityId> CandidateCubes;

    [Require] public CubeCollectorWriter CubeCollect;
    [Inject] public IUniverse Universe { get; set;  }

	void Awake () 
    {
        CubeLayer = LayerMask.NameToLayer("Cube");
        
	}

    void OnEnable()
    {
        CandidateCubes = new List<EntityId>();
    }
    void OnDisable()
    {
        CancelInvoke("UpdateCandidates");
        CandidateCubes = null;
    }

    GameObject GetObjectFromId(EntityId entId)
    {
        var entObj = Universe.Get(entId);
        if(entObj!=null)
        {
            return entObj.UnderlyingGameObject;
        }
        return null;
    }

    void UpdateCandidates()
    {
        ICubeCollectorUpdater updater = null;

        for(int icube = 0; icube<CandidateCubes.Count; )
        {
            var entId = CandidateCubes[icube];

            var obj = GetObjectFromId(entId);
            if(obj==null)
            {
                // gone missing
                CandidateCubes.RemoveAt(icube);
            }
            else 
            {
                var bpv = obj.GetComponent<BuildPieceVisualizer>();
                if (bpv == null || bpv.IsPartOfBuild())
                {
                    // already part of a build
                    CandidateCubes.RemoveAt(icube);
                }
                else
                {
                    var cv = obj.GetComponent<CarriedVisualizer>();
                    if(cv!=null && cv.IsCarried())
                    {
                        // unable to use
                        ++icube;
                    }
                    else
                    {
                        // add me!
                        if(updater==null)
                        {
                            updater = CubeCollect.Update;
                        }
                        updater.TriggerAddCubeToBuild(entId);

                        // don't add me again!
                        CandidateCubes.RemoveAt(icube);
                    }
                }
            }
        }

        if(updater!=null)
        {
            updater.FinishAndSend();
        }

        if(CandidateCubes.Count==0)
        {
            CancelInvoke("UpdateCandidates");
        }
    }

    void OnTriggerEnter(Collider col)
    {
        // grab the block
        if (CandidateCubes != null && col != null && col.gameObject.layer == CubeLayer)
        {
            var bpv = col.gameObject.GetComponent<BuildPieceVisualizer>();
            if (bpv != null && !bpv.IsPartOfBuild())
            {
                var isEmpty = CandidateCubes.Count == 0;
                CandidateCubes.Add(col.gameObject.EntityId());
                if(isEmpty)
                {
                    CancelInvoke("UpdateCandidates");
                    InvokeRepeating("UpdateCandidates", 0.0f, 1.0f);
                }
            }
        }
    }
}
