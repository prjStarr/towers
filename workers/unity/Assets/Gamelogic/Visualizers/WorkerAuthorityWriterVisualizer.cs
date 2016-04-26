using UnityEngine;
using System.Collections;
using Improbable.Unity.Visualizer;
using Improbable.Util;
using Random = System.Random;

public class WorkerAuthorityWriterVisualizer : MonoBehaviour {

    [Require] public WorkerAuthorityWriter Auth;

    private static int Id=-1;

	// Use this for initialization
	void OnEnable()
    {
        if (Id==-1)
        {
            Id = (new Random()).Next();
        }
        Auth.Update.Id(Id).FinishAndSend();
    }
}
