using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Improbable.Unity.Visualizer;
using Demoteam;
using Improbable.Math;
using Improbable.Unity.Common.Core.Math;
using Improbable.Unity;
using Improbable.Util.Collections;
using Vector3 = UnityEngine.Vector3;

[EngineType(EnginePlatform.FSim)]
public class PathFollowerVisualizer : MonoBehaviour {

    [Require] public PathReader Path;
    [Require] public PathFollowerWriter PathFollower;

    public float DistanceStep = 0.02f;
    public float DelaySeconds = 1.0f;
    private float PathLength = 0.0f;
    private bool Moving;

	// Use this for initialization
	void OnEnable () {
        Moving = false;
        Path.PathWaypointsUpdated += PathWaypointsUpdated;
        //StartCoroutine(FollowPath());
        Invoke("StartMoving", DelaySeconds);
	}

    void StartMoving()
    {
        Moving = true;
    }

    public static float CalculatePathLength(IReadOnlyList<Coordinates> path)
    {
        float length = 0.0f;

        for (var cpath = 1; cpath < path.Count; ++cpath)
        {
            var lengthFragment = (path[cpath] - path[cpath - 1]).ToUnityVector().magnitude;
            length += lengthFragment;
        }

        return length;
    }

    static Vector3 CoordToVector(Coordinates c)
    {
        return (c - Coordinates.ZERO).ToUnityVector();
    }

    static Vector3 GetPathPosForDistance(IReadOnlyList<Coordinates> path, float distance)
    {
        float length = 0.0f;

        for (var cpath = 1; cpath < path.Count; ++cpath)
        {
            var lengthFragment = (path[cpath] - path[cpath - 1]).ToUnityVector().magnitude;

            if(length+lengthFragment>distance)
            {
                float interp = (distance - length) / lengthFragment;

                return Vector3.Lerp(CoordToVector(path[cpath - 1]), CoordToVector(path[cpath]), interp);
            }
            length += lengthFragment;
        }

        if (path.Count > 0)
        {
            return CoordToVector(path[path.Count - 1]);
        }

        return Vector3.zero; // error
    }

    void PathWaypointsUpdated(IReadOnlyList<Coordinates> path)
    {
        PathLength = CalculatePathLength(path);
    }
    IEnumerator FollowPath()
    {
        yield return new WaitForSeconds(DelaySeconds);

        float distanceStep = DistanceStep;
        float distance = distanceStep;

        while(!Mathf.Approximately(distance, PathLength))
        {
            transform.position = GetPathPosForDistance(Path.PathWaypoints, distance);

            distance = Mathf.Min(distance + distanceStep, PathLength);

            yield return null;
        }
    }
    void FixedUpdate()
    {
        if(!Moving)
        {
            return;
        }

        var newDistance = Mathf.Min((float)PathFollower.PathDistance + DistanceStep * Time.fixedDeltaTime, PathLength);

        transform.position = GetPathPosForDistance(Path.PathWaypoints, newDistance);

        PathFollower.Update.PathDistance(newDistance).FinishAndSend();
    }
}
