using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Improbable;
using Improbable.Unity.Visualizer;
using Improbable.Unity.Entity;
using Demoteam;
using Improbable.Entity;
using IoC;
using Improbable.Core.Entity;

public class TargetingVisualizer : MonoBehaviour {

    [Require] public PhysicalTargetingWriter Targeting;

    [Inject] public IUniverse Universe { get; set; }

    private HashSet<EntityId> Entities;
    private int CubeLayer;

    void Awake()
    {
        Entities = new HashSet<EntityId>();
        CubeLayer = LayerMask.NameToLayer("Cube");
    }

	void OnEnable () 
    {	
	}

    void OnDisable()
    {
        CancelInvoke("UpdateTargets");
    }

    bool IsObjectTargetCandidate(IEntityObject obj)
    {
        if (obj == null || obj.UnderlyingGameObject == null)
        {
            return false;
        }
        var bpv = obj.UnderlyingGameObject.GetComponent<BuildPieceVisualizer>();
        if (bpv == null || bpv.IsPartOfBuild())
        {
            return false;
        }
        var cv = obj.UnderlyingGameObject.GetComponent<CarriedVisualizer>();
        if (cv == null || cv.IsCarried())
        {
            return false;
        }
        return true;
    }
	
	void UpdateTargets () 
    {
        List<EntityId> physicalTargets = null;

        Entities.RemoveWhere( (EntityId entId) => {
            return Universe.Get(entId) == null;
        });

        var it = Entities.GetEnumerator();
        do
        {
            var entId = it.Current;
            var obj = Universe.Get(entId);

            if(IsObjectTargetCandidate(obj))
            {
                // then we are a goer!
                if (physicalTargets == null)
                {
                    physicalTargets = new List<EntityId>();
                }
                physicalTargets.Add(entId);
            }
        }
        while(it.MoveNext());

        if (physicalTargets != null && Targeting!=null)
        {
            Targeting.Update.TargetCandidates(physicalTargets).FinishAndSend();
        }
	}

    void OnTriggerEnter(Collider col)
    {
        var entId = col.gameObject.EntityId();
        if (col.gameObject.layer == CubeLayer && entId != Improbable.EntityId.InvalidEntityId)
        {
            int ntargets = Entities.Count;

            Entities.Add(entId);

            if(ntargets==0)
            {
                CancelInvoke("UpdateTargets");
                InvokeRepeating("UpdateTargets", 0.0f, 3.0f);
            }
        }
    }

    void OnTriggerExit(Collider col)
    {
        var entId = col.gameObject.EntityId();
        if (col.gameObject.layer == CubeLayer && entId != Improbable.EntityId.InvalidEntityId)
        {
            Entities.Remove(col.gameObject.EntityId());

            if(Entities.Count==0)
            {
                CancelInvoke("UpdateTargets");
            }
        }
    }
}
