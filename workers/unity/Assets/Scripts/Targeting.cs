using UnityEngine;
using System.Collections;
using System.Reflection;
using Improbable.Core.Entity;
using UnityEngine.UI;
using Improbable;

public class Targeting : MonoBehaviour {

    public GameObject Target;
    public Color Col;
    private Canvas Canvas;

    void Awake()
    {
        Canvas = Component.FindObjectOfType<Canvas>();
    }

    float ShortestDistanceToLine(Vector3 origin, Vector3 unitDir, Vector3 pos)
    {
        var toPos = pos - origin;
        var proj = Vector3.Dot(toPos, unitDir);
        var perp = toPos - unitDir * proj;
        return perp.magnitude;
    }

    GameObject GetBestTarget(out float interp)
    {
        var lineBase = transform.position;
        var lineDir = transform.forward;

        var builders = Component.FindObjectsOfType<BuilderVisualizer>();

        var mostShortest = 1e10f;
        GameObject bestTarget = null;

        for (var ibuilder = 0; ibuilder < builders.Length; ++ibuilder)
        {
            var builder = builders[ibuilder];
            var lineTo = builder.transform.position - lineBase;

            if (Vector3.Dot(lineTo, lineDir) > 0.0f)
            {
                var shortest = ShortestDistanceToLine(lineBase, lineDir, builder.transform.position);
                if (shortest < mostShortest)
                {
                    mostShortest = shortest;
                    bestTarget = builder.gameObject;
                }
            }
        }

        interp = Mathf.Exp(-0.46f * mostShortest);

        return bestTarget;
    }

    MeshRenderer GetHighlightRenderer(GameObject g)
    {
        var h = g.transform.FindChild("Highlight");
        if (h != null)
        {
            return h.GetComponent<MeshRenderer>();
        }
        return null;
    }

    void ClearHighlight(GameObject g)
    {
        var mr = GetHighlightRenderer(g);
        if (mr != null)
        {
            mr.enabled = false;
        }
    }

    void SetHighlight(GameObject g)
    {
        var mr = GetHighlightRenderer(g);
        if (mr != null)
        {
            mr.enabled = true;
        }
    }

    void UpdateInterp(float interp)
    {
        if (Target != null)
        {
            Col.a = Mathf.Lerp(0.0f, 88.0f / 255, interp);
            var mr = GetHighlightRenderer(Target);
            if (mr != null)
            {
                mr.materials[0].color = Col;
            }
        }
    }

    static string EntityIdToString(EntityId entId)
    {
        var fields = entId.GetType().GetFields(BindingFlags.NonPublic | BindingFlags.Instance);
        foreach(var f in fields)
        {
            if(f.Name=="Id")
            {
                var idObj = f.GetValue(entId);
                return idObj.ToString();
            }
        }

        return "<Unknown>";
    }

	
	// Update is called once per frame
	void Update () 
    {
        float interp;
        GameObject bestTarget = GetBestTarget(out interp);

        if(bestTarget!=Target)
        {
            if(Target!=null)
            {
                ClearHighlight(Target);
            }
            if (bestTarget != null)
            {
                SetHighlight(bestTarget);

                if (Canvas != null)
                {
                    var tr = Canvas.transform.FindChild("TargetId");
                    if (tr != null)
                    {
                        var txt = tr.GetComponent<Text>();
                        if (txt != null)
                        {
                            var stor = bestTarget.GetComponent<EntityObjectStorage>();
                            if (stor != null)
                            {
                                txt.text = EntityIdToString(stor.entityId);
                            }
                        }
                    }
                }
            }
            Target = bestTarget;
        }

        UpdateInterp(interp);
	}
}
