using UnityEngine;
using System.Collections;
using Improbable.Unity;
using Improbable.Unity.Visualizer;

[EngineType(EnginePlatform.Client)]
public class TurnWheels : MonoBehaviour {

    public Transform[] WheelRigs;

    public float WheelDiameter = 0.333f;

    struct PositionEntry
    {
        public Vector3 Pos;
        public float TimeStamp;
    }
    private PositionEntry[] PositionCache;
    private int PositionCacheHead = 0;

    void CachePos()
    {
        var entry = new PositionEntry();
        entry.Pos = transform.position;
        entry.TimeStamp = Time.time;

        PositionCacheHead = (PositionCacheHead + 1) % PositionCache.Length;
        PositionCache[PositionCacheHead] = entry;
    }

    void Awake()
    {
        PositionCache = new PositionEntry[5];
        
        for(int ientry=0; ientry<PositionCache.Length; ++ientry)
        {
            CachePos();
        }
    }

    float GetForwardSpeedMaxXZ()
    {
        var entry1 = PositionCache[PositionCacheHead];
        var entry0 = PositionCache[(PositionCacheHead + PositionCache.Length - 1) % PositionCache.Length];

        var vel = Vector3.Scale(entry1.Pos-entry0.Pos, Vector3.forward+Vector3.right) / (entry1.TimeStamp - entry0.TimeStamp);

        return Vector3.Dot(vel, transform.forward);
    }
		
	// Update is called once per frame
	void Update () {

        CachePos();

        float fwdSpeed = GetForwardSpeedMaxXZ();
        float fwdDistance = fwdSpeed * Time.deltaTime;

        float radsThisFrame = fwdDistance * 2 / WheelDiameter;

        for(int iwheels = 0; iwheels<WheelRigs.Length; ++iwheels)
        {
            WheelRigs[iwheels].localRotation *= Quaternion.AngleAxis(radsThisFrame * Mathf.Rad2Deg, Vector3.right);
        }

	}
}
