using UnityEngine;
using System.Collections;

public class FollowTarget : MonoBehaviour {

    private GameObject Target;
    public float StringLength = 5.0f;

	// Use this for initialization
	void Start () {
	
	}

    void UpdateToggle()
    {
        if (Input.GetKeyUp(KeyCode.F))
        {
            if (Target != null)
            {
                Target = null;
            }
            else
            {
                var targeting = GetComponent<Targeting>();
                if (targeting != null)
                {
                    Target = targeting.Target;
                }
            }
        }
    }

    static float Square(float f)
    {
        return f * f;
    }
	
	// Update is called once per frame
	void LateUpdate () {

        UpdateToggle();
        

        if(Target!=null)
        {
            var lineTo = transform.position-Target.transform.position;
            var yOffset = Vector3.Dot(lineTo, Vector3.up);
            var lineToXZ = Vector3.Scale(lineTo, Vector3.forward+Vector3.right);

            if(lineToXZ.sqrMagnitude>Square(StringLength))
            {
                transform.position = Target.transform.position + lineToXZ.normalized * StringLength + Vector3.up * yOffset;
            }

            transform.rotation = Quaternion.LookRotation((Target.transform.position - transform.position).normalized);
        }
	}
}
