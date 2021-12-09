# Introduction
The C# implementation is primarily intended for use in Unity projects.
*Caution:* This is still a work in progress.

# Recommended Unity Assets
* RT-Voice Pro
** https://assetstore.unity.com/packages/tools/audio/rt-voice-pro-41068
** Provides Text-to-Speech functionality which is not included with the RobotEngine.
** However, the RobotEngine does include the *MySpeaker* component which makes use of the RT-Voice classes.

* SALSA Lipsync Suite
** https://assetstore.unity.com/packages/tools/animation/salsa-lipsync-suite-148442
** Provides approximated real-time lipsyncing, as well as components for gaze management and blinking.
** SALSA's *Eyes* component works well in combination with the *TargetHeadIK* that comes with the RobotEngine.

# How to set up an Agent
* Preparation:
** Get the recommended assets and import them in your project.
** Set them up for your agent according to their documentation.
* Add an implementation of the *HeadIK* class to your agent.
** If you are using SALSA's *Eyes* Component:
*** Add an Empty to your agent's hierarchy and place it where the neck meets the head.
*** Add a *TargetHeadIK* component to your agent and assign the Empty as the gaze target.
** If you are not using SALSA:
*** Add a *BoneHeadIK* component to your agent and assign the bones of the head, the left eye and the right eye.
*** If necessary, adjust the rotation limits of the bones.
* Add a *FACSAnimator* component to your agent.
** Assign the agent's *SkinnedMeshRenderer* component.
* Set up an Animator for your agent.
** Add an integer parameter named *gestureIndex* with the default value of -1.
** Add an empty state.
** Add states for your gesture animations.
** Add transitions between the empty state and the gesture animation states, depending on the value of the *gestureIndex* parameter.
* Add a *MySpeaker* component to your agent.
** Enter the name of the voice you want to use, depending on those accessible via RT-Voice.
** If necessary, adjust the pitch, rate and volume for your agent. 
* Add a *UnityBotEngine* component to your agent.
** Assign the *MySpeaker*, *HeadIK*, *FACSAnimator* and *Animator* components of your agent in the respective fields.
** Set up the IP address and port for the RobotEngine's MessageServer.

# Adapting the code
As this is still being developed, several things are still hardcoded and may need to be changed to fit your agent.

* "anim" Commands
** Go to the method *PlayAnimation()* in *UnityBotEngine.cs*.
** Alter the *switch* command to translate your animation names to the required value of the *gestureIndex* parameter.
* "facs" Commands
** Go to the method *Start()* in *FACSAnimator.cs*.
** Alter the *GetBlendShapeIndex()* calls to use the names of the blendshapes as they appear on your agent. You can find those when you select the *SkinnedMeshRenderer* component of your agent and expand the *Blend Shapes* section.
