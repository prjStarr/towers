using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Demoteam;
using Improbable.Unity;
using Improbable.Math;
using Vector3 = UnityEngine.Vector3;

[EngineType(EnginePlatform.Client)]
public class PathRenderer : MonoBehaviour {

    public Material Mat;
    private Camera Cam;

    void Awake()
    {
        Cam = GetComponent<Camera>();
    }
    
    public void OnPostRender()
    {
        var pathFollowers = GameObject.FindObjectsOfType<PathDrawerVisualizer>();
        if (pathFollowers.Length > 0 && Cam != null)
        {
            GL.PushMatrix();
            GL.LoadOrtho();
            Mat.SetPass(0);
            
            foreach (var path in pathFollowers)
            {
                if (path.enabled && path.Path != null)
                {
                    GL.Begin(GL.LINES);
                    GL.Color(Color.red);

                    var waypoints = path.Path.PathWaypoints;

                    for (var ipt = 1; ipt < waypoints.Count; ++ipt)
                    {
                        var pos0View = Cam.WorldToViewportPoint(waypoints[ipt - 1].ToUnityVector());
                        var pos1View = Cam.WorldToViewportPoint(waypoints[ipt].ToUnityVector());
                        if (pos0View.z > 0.0f && pos1View.z > 0.0f)
                        {
                            var pos0OrthoXY = Vector3.Scale(pos0View, Vector3.right + Vector3.up);
                            var pos1OrthoXY = Vector3.Scale(pos1View, Vector3.right + Vector3.up);
                            GL.Vertex(pos0OrthoXY);
                            GL.Vertex(pos1OrthoXY);
                        }
                    }
                    GL.End();
                }
            }

            GL.PopMatrix();
        }
    }
}
