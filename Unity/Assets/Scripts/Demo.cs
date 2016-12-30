using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Demo : MonoBehaviour {

	public void SetSingleListner (Text singleText) {
		CustomInput.Show (singleText.text, false, (string data) => {
			if (data != null)
				singleText.text = data;
			else
				Debug.Log ("save or send singleText");
		});
	}

	public void SetMultiListner (Text multiText) {
		CustomInput.Show (multiText.text, true, (string data) => {
			if (data != null)
				multiText.text = data;
			else
				Debug.Log ("save or send multiText");
		});		
	}

}
