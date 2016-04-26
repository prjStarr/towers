using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public interface MaterialChangeListener
{
    void OnMaterialsChange(Renderer ren, Material[] newMaterials);
}

public class ChangeMaterial : MonoBehaviour {

    private List<MaterialChangeListener> Listeners;

    public void AddListener(MaterialChangeListener l)
    {
        if(Listeners==null)
        {
            Listeners = new List<MaterialChangeListener>();
        }
        Listeners.Add(l);
    }
    public void RemoveListener(MaterialChangeListener l)
    {
        Listeners.Remove(l);
    }

    public void ChangeMaterials(Renderer ren, Material[] newMaterials)
    {
        if (Listeners != null)
        {
            foreach (var listener in Listeners)
            {
                var component = listener as MonoBehaviour;

                if (component != null && component.enabled)
                {
                    listener.OnMaterialsChange(ren, newMaterials);
                }
            }
        }

        ren.materials = newMaterials;
    }

    public static void ChangeMaterialsOnObject(GameObject obj, Material[] newMaterials)
    {
        var renderer = obj.GetComponent<Renderer>();
        if (renderer != null)
        {
            var changer = obj.GetComponent<ChangeMaterial>();
            if (changer != null)
            {
                changer.ChangeMaterials(renderer, newMaterials);
            }
        }
    }
}
