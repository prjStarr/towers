using UnityEngine;
using System.Collections;

public class RopeConstraint : MonoBehaviour {

    public float LengthXZ = 3.0f;

    private Transform Parent;
    private Vector3 LastPosition;

    void Awake()
    {
        Parent = transform.parent;
    }


    static float Square(float f)
    {
        return f * f;
    }

	// Use this for initialization
	void OnEnable() 
    {
        LastPosition = transform.position;
	}
	
	// Update is called once per frame
	void Update () {
        if(Parent!=null)
        {
            //var lineY = Vector3.Scale(LastPosition - Parent.position, Vector3.up);
            var lineXZ = Vector3.Scale(LastPosition - Parent.position, Vector3.forward + Vector3.right);
            if(lineXZ.sqrMagnitude>Square(LengthXZ))
            {
                LastPosition = Parent.position + lineXZ.normalized * LengthXZ;// +lineY;
                transform.position = LastPosition;
            }
        }
	}
}
