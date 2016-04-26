using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System;
using Demoteam;
using Improbable.Unity.Visualizer;
using UnityEngine.UI;
using Improbable.Util.Collections;

public class TeamScoreVisualizer : MonoBehaviour {

    [Require]
    public TeamScoresReader TeamScore;

    private Canvas Canvas;

    void Awake()
    {
        Canvas = Component.FindObjectOfType<Canvas>();
    }

	void OnEnable () 
    {
        TeamScore.ScoresUpdated += ScoresUpdated;
	}

    Transform GetCanvasElement(string n)
    {
        return Canvas != null ? Canvas.transform.FindChild(n) : null;
    }

    void ScoresUpdated(IReadOnlyDictionary<int, int> scores)
    {
        foreach(var val in Enum.GetValues(typeof(BuildGeometry)))
        {
            var canvasElement = GetCanvasElement("Scores"+val.ToString());

            int score;
            if(canvasElement!=null && scores.TryGetValue((int)val, out score))
            {
                var txt = canvasElement.GetComponent<Text>();
                if (txt != null)
                {
                    txt.text = score.ToString();
                }
            }

        }
    }
}
