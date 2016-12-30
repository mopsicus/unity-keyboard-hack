using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;
using System;
using UnityEngine.UI;
using NiceJson;

public class CustomInput : MonoBehaviour {

	public delegate void CustomInputEnd (string data = null);				
	private static CustomInputEnd CustomInputCallBack;  		

	#if UNITY_ANDROID
		private AndroidJavaObject _input;
	#elif UNITY_IOS
		[DllImport ("__Internal")]
		private static extern void inputLaunch (string text, bool mode);
		[DllImport ("__Internal")]
		private static extern void inputClose ();
	#endif 

	private static CustomInput _instance;

	void Awake () {
		_instance = GetComponent<CustomInput> ();
	}

	public static void Show (string text, bool multiLines, CustomInputEnd callBack) {
		CustomInputCallBack = callBack;
		#if UNITY_ANDROID
			if (_instance._input != null) 
				Close ();
			_instance._input = new AndroidJavaObject("ru.mopsicus.custominput.Plugin");
			_instance._input.Call("show", text, multiLines);
		#elif UNITY_IOS	
			inputLaunch (text, multiLines);
		#endif
	}
		
	public static void Close () {
		#if UNITY_ANDROID
			_instance._input.Call ("close");
			_instance._input.Dispose ();
			_instance._input = null;
		#elif UNITY_IOS
			inputClose ();
		#endif
	}	

	void OnCustomInputAction (string data) {
		JsonObject info = (JsonObject)JsonNode.ParseJsonString (data);		
		int code = info ["code"];
		switch (code) {
		case 0: // close or return for singleline
			//
			// info["data"] in null
			// can send event to save text in PlayerPrefs or send to server
			//
			if (CustomInputCallBack != null)
				CustomInputCallBack ();	
			break;
		case 1: // keyboard height change
			//
			// in info["data"] contains keyboard height 
			// can send event to move smth when keyboard hide or show
			// e.g. you can use my simple event manager http://mopsicus.ru/all/unity-event-manager-with-parameters/
			//
			Debug.Log ("keyboard height = " + info["data"]);
			break;
		case 2: // text input
			if (CustomInputCallBack != null)
				CustomInputCallBack (info["data"]);			
			break;
		default:
			break;
		}
	}	
		

}