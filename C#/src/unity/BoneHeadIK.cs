using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BoneHeadIK : HeadIK
{
	public Transform rootTransform;
	
	[Header("Head")]
	public Transform headBone;
	[Range(-90.0f, 0.0f)]
	public float minHeadYaw=-90.0f;
	[Range(0.0f, 90.0f)]
	public float maxHeadYaw=90.0f;
	[Range(-90.0f, 0.0f)]
	public float minHeadPitch=-60.0f;
	[Range(0.0f, 90.0f)]
	public float maxHeadPitch=60.0f;
	
	private Quaternion neutralHeadRotation;
	private Quaternion oldHeadRotation;
	private Quaternion newHeadRotation;
	
	[Space(10)]
	[Header("Eyes")]
	public Transform leftEyeBone;
	public Transform rightEyeBone;
	[Range(-90.0f, 0.0f)]
	public float minEyeYaw=-30.0f;
	[Range(0.0f, 90.0f)]
	public float maxEyeYaw=30.0f;
	[Range(-90.0f, 0.0f)]
	public float minEyePitch=-30.0f;
	[Range(0.0f, 90.0f)]
	public float maxEyePitch=30.0f;
	
	private Vector3 leftEyeOffset;
	private Quaternion neutralLeftEyeRotation;
	private Quaternion neutralRightEyeRotation;
	private Quaternion oldLeftEyeRotation;
	private Quaternion newLeftEyeRotation;
	private Quaternion oldRightEyeRotation;
	private Quaternion newRightEyeRotation;



	

	protected override void InitIK()
	{
		neutralHeadRotation = headBone.transform.localRotation;
		Debug.Log("Neutral Head Orientation: "+neutralHeadRotation.eulerAngles);
        
		leftEyeOffset = rootTransform.worldToLocalMatrix.MultiplyVector(leftEyeBone.position - headBone.position);
		Debug.Log("Left Eye Offset: "+leftEyeOffset);
    	
		neutralLeftEyeRotation = leftEyeBone.transform.localRotation;
		neutralRightEyeRotation = rightEyeBone.transform.localRotation;
    }

	
	
	protected override bool PrepareAnimation(double x, double y, double z, double time){
		//rotate right-hand Z-up input vector to match Unity's left-hand Y-up coordinate system
		Vector3 delta = new Vector3((float)(-y),(float)z,(float)x);				
		
		//-----------------------------------------
		// calculate the target angles
		//-----------------------------------------
		
		// head -----------------------------------
		
		float headYaw = Mathf.Atan2(delta.x, delta.z)*Mathf.Rad2Deg;
		
		headYaw = Mathf.Min(maxHeadYaw, headYaw);
		headYaw = Mathf.Max(minHeadYaw, headYaw);
		
		float c = Mathf.Sqrt(delta.x*delta.x+delta.z*delta.z);
		float headPitch = -(Mathf.Atan2(delta.y,c)*Mathf.Rad2Deg);
		
		headPitch=Mathf.Min(maxHeadPitch, headPitch);
		headPitch=Mathf.Max(minHeadPitch, headPitch);
		
		oldHeadRotation = headBone.localRotation;
		newHeadRotation = neutralHeadRotation*Quaternion.Euler(headPitch, headYaw, 0.0f);
		
		// eyes -----------------------------------
		
		
		float eyeDist = Mathf.Sqrt(c*c+delta.y*delta.y)-leftEyeOffset.z;
		float eyeYaw = Mathf.Atan2(leftEyeOffset.x, eyeDist)*Mathf.Rad2Deg;
		Debug.Log("eyeYaw: "+eyeYaw);
		
		oldLeftEyeRotation = leftEyeBone.localRotation;
		oldRightEyeRotation = rightEyeBone.localRotation;
		
		newLeftEyeRotation = neutralLeftEyeRotation*Quaternion.Euler(0.0f, -eyeYaw, 0.0f);
		newRightEyeRotation = neutralRightEyeRotation*Quaternion.Euler(0.0f, eyeYaw, 0.0f);
		
		
		bool different = !(oldHeadRotation.Equals(newHeadRotation) && oldLeftEyeRotation.Equals(newLeftEyeRotation));
		return different;
	}
	
	
	protected override void UpdateAnimation()
	{
		float now = Time.time;
		if(now <= _animEndTime)
		{
			float fraction = (now-_animStartTime)/_animDuration;
			
			Quaternion intermediateHeadPose = Quaternion.Lerp(oldHeadRotation, newHeadRotation, fraction);
			Quaternion intermediateLEyePose = Quaternion.Lerp(oldLeftEyeRotation, newLeftEyeRotation, fraction);
			Quaternion intermediateREyePose = Quaternion.Lerp(oldRightEyeRotation, newRightEyeRotation, fraction);
			
			headBone.localRotation = intermediateHeadPose;
			leftEyeBone.localRotation = intermediateLEyePose;
			rightEyeBone.localRotation = intermediateREyePose;
			
		}else{
			_isAnimating = false;
			SendMessage("AnimationFinished", _currentTask);
			_currentTask=null;
		}		
	}
}
