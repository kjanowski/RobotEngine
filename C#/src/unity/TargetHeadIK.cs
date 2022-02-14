using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TargetHeadIK : HeadIK
{
	public GameObject target;
	
	private Vector3 _targetNeutralPos;
	private Vector3 _oldPos;
	private Vector3 _newPos;
	
	protected override void InitIK()
    {
	    _targetNeutralPos = target.transform.position;
    }


	protected override bool PrepareAnimation(double x, double y, double z, double time){
		_oldPos = target.transform.position;
		_newPos = new Vector3(
			_targetNeutralPos.x + (float)y,
			_targetNeutralPos.y + (float)z,
			_targetNeutralPos.z - (float)x); //translate Z-up to Y-up
		
		bool different = Vector3.Distance(_oldPos, _newPos)>0.0f;
		return different;
	}
	
	protected override void UpdateAnimation()
	{
		float now = Time.time;
		if(now <= _animEndTime)
		{
			float fraction = (now-_animStartTime)/_animDuration;
			
			Vector3 intermediateTargetPos = Vector3.Lerp(_oldPos, _newPos, fraction);
			
			target.transform.SetPositionAndRotation(intermediateTargetPos, target.transform.rotation);
			
		}else{
			_isAnimating = false;
			SendMessage("AnimationFinished", _currentTask);
			_currentTask=null;
		}		
	}
}
