using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public abstract class HeadIK : MonoBehaviour
{
	protected bool _isAnimating;
	protected float _animStartTime;
	protected float _animEndTime;
	protected float _animDuration;

	protected string _currentTask;
	
	
    // Start is called before the first frame update
    void Start()
    {
	    _isAnimating = false;
	    InitIK();
    }

	protected abstract void InitIK();
	
	
	public bool GazeAt(string taskID, double x, double y, double z, double time){
		
		if(_currentTask!=null)
			SendMessage("AnimationFinished", _currentTask);
		
		_currentTask = taskID;
		
		bool different = PrepareAnimation(x, y, z, time);
		if(different)
		{
			_animStartTime = (float)Time.time;
			_animDuration = (float)(time/1000.0);
			_animEndTime = _animStartTime+_animDuration;
			_isAnimating = true;
		}else _isAnimating = false;
		
		return true;
	}
	
	protected abstract bool PrepareAnimation(double x, double y, double z, double time);
	
	// Update is called once per frame
	void Update()
	{
		if(_isAnimating)
			UpdateAnimation();
	}
    
	protected abstract void UpdateAnimation();
		
}
