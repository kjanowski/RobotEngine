using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using UnityEngine;
using RobotEngine.Messaging;


public class FACSAnimator : MonoBehaviour
{
	public SkinnedMeshRenderer renderer;
	private Dictionary<string, int> _blendshapeMap;
	
	private bool _isAnimating;
	private string _currentTask;
	protected float _animStartTime;
	protected float _animEndTime;
	protected float _animDuration;
	private Dictionary<int, float> _oldValues;
	private Dictionary<int, float> _newValues;
	
	
    // Start is called before the first frame update
    void Start()
    {
	    _isAnimating = false;
	    
	    _blendshapeMap = new Dictionary<string, int>();
	    
	    
	    int index = renderer.sharedMesh.GetBlendShapeIndex("1_inner_brow_raise");
	    _blendshapeMap.Add("au01", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("2_outer_brow_raise");	    
	    _blendshapeMap.Add("au02", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("4_brow_lower");	    
	    _blendshapeMap.Add("au04", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("5_upper_lid_raise");
	    _blendshapeMap.Add("au05", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("6_cheek_raise");
	    _blendshapeMap.Add("au06", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("7_lid_tightener");
	    _blendshapeMap.Add("au07", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("9_nose_wrinkle");
	    _blendshapeMap.Add("au09", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("10_upper_lip_raiser");
	    _blendshapeMap.Add("au10", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("12_lip_corner_puller");
	    _blendshapeMap.Add("au12", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("13_cheek_puffer");
	    _blendshapeMap.Add("au13", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("14_dimpler");
	    _blendshapeMap.Add("au14", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("15_lip_corner_depressor");
	    _blendshapeMap.Add("au15", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("16_lower_lip_depressor");
	    _blendshapeMap.Add("au16", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("17_chin_raiser");
	    _blendshapeMap.Add("au17", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("18_lip_pucker");
	    _blendshapeMap.Add("au18", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("20_lip_stretcher");
	    _blendshapeMap.Add("au20", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("22_lip_funneler");
	    _blendshapeMap.Add("au22", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("23_lip_tightener");
	    _blendshapeMap.Add("au23", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("24_lip_pressor");
	    _blendshapeMap.Add("au24", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("25_lip_part");
	    _blendshapeMap.Add("au25", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("26_jaw_drop");
	    _blendshapeMap.Add("au26", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("28_lip_suck");
	    _blendshapeMap.Add("au28", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("41_lid_droop");
	    _blendshapeMap.Add("au41", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("42_slit");
	    _blendshapeMap.Add("au42", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("43_eyes_closed");
	    _blendshapeMap.Add("au43", index);
	    
	    index = renderer.sharedMesh.GetBlendShapeIndex("44_squint");
	    _blendshapeMap.Add("au44", index);

	    index = renderer.sharedMesh.GetBlendShapeIndex("46_wink_l");
	    _blendshapeMap.Add("au46", index);
	    
	    _oldValues = new Dictionary<int, float>();
	    _newValues = new Dictionary<int, float>();
    }

    // Update is called once per frame
    void Update()
    {
	    if(_isAnimating)
	    {
	    	UpdateAnimation();
	    }
    }
    
    
	public void ShowFACS(CommandMessage msg){
		bool different = PrepareFACSAnimation(msg);
		
		if(different)
		{
			string timeStr = msg.GetParam("time");
			double time = 0.0;
			double.TryParse(timeStr, NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out time);
			
			_animStartTime = (float)Time.time;
			_animDuration = (float)(time/1000.0);
			_animEndTime = _animStartTime+_animDuration;
			_currentTask = msg.GetTaskID();
			_isAnimating = true;
		}else
		{
			_isAnimating = false;
			SendMessage("AnimationFinished", _currentTask);
			_currentTask=null;
		}
	}
    
    
	public bool PrepareFACSAnimation(CommandMessage msg){
		
		bool different = false;
		
		foreach(string key in msg.GetCommandParams().Keys){
			if(key.StartsWith("au"))
			{
				//get the blendshape name
				int shapeIdx;
				_blendshapeMap.TryGetValue(key, out shapeIdx);
				if(shapeIdx > 0)
				{
					_oldValues.Remove(shapeIdx);
					_newValues.Remove(shapeIdx);
					
					//check for difference
					double value = 0.0;
					double.TryParse(msg.GetParam(key), NumberStyles.Any, CultureInfo.GetCultureInfo("en-US"), out value);
					value = value*100; //map [0.0; 1.0] parameter to [0.0, 100.0] blendshape weight
					
					float currValue = renderer.GetBlendShapeWeight(shapeIdx);
					Debug.Log(key +" (blendshape "+shapeIdx+") changing from "+currValue+" to "+value);
					
					if(currValue != (float)value)
					{
						different = true;
						
						//store the current value	
						_oldValues.Add(shapeIdx, currValue);
						//store the target value
						_newValues.Add(shapeIdx, (float)value);
					}
				}else Debug.LogWarning("no blendshape known for "+key+"!");
			}
			//otherwise ignore	
		}
		return different;
	}
    
    
    
	private void UpdateAnimation(){
		float now = Time.time;
		if(now <= _animEndTime)
		{
			float fraction = (now-_animStartTime)/_animDuration;
		
		
			foreach(int index in _newValues.Keys){
				float newValue, oldValue;
				_newValues.TryGetValue(index, out newValue);
				_oldValues.TryGetValue(index, out oldValue);
				
				float intermediateValue = oldValue + fraction*(newValue-oldValue);
				
				renderer.SetBlendShapeWeight(index, intermediateValue);
			}
		}else
		{
			_newValues.Clear();
			_oldValues.Clear();
			_isAnimating = false;
			SendMessage("AnimationFinished", _currentTask);
			_currentTask=null;
		}
	}
}
