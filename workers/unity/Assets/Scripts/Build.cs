using UnityEngine;
using System.Collections;

public class Build : MonoBehaviour {

    public GameObject BlockTemplate;

    [Range(0.0f, 1.0f)]
    public float BuildFraction = 0.0f;

    public float HeightMultiplier = 1.05f;
    public float RadMultiplier = 1.05f;
    
    public int NumBase = 12;
    public int NumHeight = 12;

    private int NumBlocks { get { return (int)(BuildFraction*MaxBlocks); } }
    private int NumBlocksCommitted;

    private GameObject[] Blocks;

    private int MaxBlocks { get { return NumBase * NumHeight; } }

    private float GetAngleForBlockAtIndex(int iblock)
    {
        int iy = iblock / NumBase;
        int ixz = iblock % NumBase;
        return (ixz + (iy % 2 == 1 ? 0.5f : 0.0f)) * 2 * Mathf.PI / NumBase;
    }

    private Vector3 GetPositionForBlockAtIndex(int iblock)
    {
        float cubeSide = 1.0f;

        int iy = iblock / NumBase;

        float phiStep = 2 * Mathf.PI / NumBase;
        float phi = GetAngleForBlockAtIndex(iblock);
        float r = (cubeSide / 2) * (1.0f + 1.0f / (Mathf.Tan(phiStep / 2)));

        return Vector3.up * (cubeSide / 2 + cubeSide * iy) * HeightMultiplier + (Vector3.forward * Mathf.Sin(phi) + Vector3.right * Mathf.Cos(phi)) * r*RadMultiplier;
    }
    private Quaternion GetOrientationForBlockAtIndex(int iblock)
    {
        var vec = Vector3.Scale(GetPositionForBlockAtIndex(iblock), Vector3.forward+Vector3.right);
        return Quaternion.LookRotation(vec.normalized);
    }

    void DestroyBlocks()
    {
        for(int iblock=0; iblock<NumBlocksCommitted; ++iblock)
        {
            GameObject.Destroy(Blocks[iblock]);
        }

        Blocks = null;
    }

    void MakeBlocks(int nblocks)
    {
        Blocks = new GameObject[nblocks];

        for(int iblock = 0; iblock<nblocks; ++iblock)
        {
            var block = GameObject.Instantiate(BlockTemplate, transform.TransformPoint(GetPositionForBlockAtIndex(iblock)), transform.rotation*GetOrientationForBlockAtIndex(iblock));

            Blocks[iblock] = block as GameObject;
        }
    }

	void Update()
    {
        if (NumBlocks != NumBlocksCommitted)
        {
            DestroyBlocks();
            MakeBlocks(NumBlocks);
            NumBlocksCommitted = NumBlocks;
        }
    }
}
