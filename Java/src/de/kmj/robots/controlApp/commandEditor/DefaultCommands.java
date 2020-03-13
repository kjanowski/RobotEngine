/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.kmj.robots.controlApp.commandEditor;

import de.kmj.robots.messaging.CommandMessage;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Kathrin
 */
public class DefaultCommands {
    private final TreeMap<String, CommandMessage> commands;
    
    public DefaultCommands()
    {
        commands = new TreeMap<String, CommandMessage>();
        
        addSpeechCommands();
        addAnimationCommands();
        addLEDCommands();
        addAudioCommands();
        addSetVoiceCommands();        
    }
    
    
    protected final void addSpeechCommands()
    {
        CommandMessage speechCmd = new CommandMessage("defaultSpeech", "speech");
        speechCmd.addParameter("text", "Bla bla bla.");
        speechCmd.addParameter("lipSync", "true");
        commands.put("speech", speechCmd);
        
        CommandMessage stopSpeechCmd = new CommandMessage("defaultStopSpeech", "stopSpeech");
        commands.put("stopSpeech", stopSpeechCmd);
    }
    
    
    protected final void addAnimationCommands()
    {
        CommandMessage animCmd = new CommandMessage("defaultAnim", "anim");
        animCmd.addParameter("name", "Face/happy");
        commands.put("anim", animCmd);
        
        CommandMessage gazeCmd_time = new CommandMessage("defaultGaze_time", "gaze");
        gazeCmd_time.addParameter("x", "0.3");
        gazeCmd_time.addParameter("y", "0.0");
        gazeCmd_time.addParameter("z", "0.0");
        gazeCmd_time.addParameter("time", "500");
        commands.put("gaze (time)", gazeCmd_time);
        
        CommandMessage gazeCmd_speed = new CommandMessage("defaultGaze_speed", "gaze");
        gazeCmd_speed.addParameter("x", "0.3");
        gazeCmd_speed.addParameter("y", "0.0");
        gazeCmd_speed.addParameter("z", "0.0");
        gazeCmd_speed.addParameter("speed", "100.0");
        commands.put("gaze (speed)", gazeCmd_speed);
        
        CommandMessage pointCmd = new CommandMessage("defaultPoint", "point");
        pointCmd.addParameter("x", "0.3");
        pointCmd.addParameter("y", "0.0");
        pointCmd.addParameter("z", "0.0");
        commands.put("point", pointCmd);

        CommandMessage moveCmd = new CommandMessage("defaultMove", "move");
        moveCmd.addParameter("x", "0.3");
        moveCmd.addParameter("y", "0.0");
        moveCmd.addParameter("angle", "0.0");
        commands.put("move", moveCmd);

        CommandMessage stopMoveCmd = new CommandMessage("defaultStopMove", "stopMove");
        commands.put("stopMove", stopMoveCmd);
        
        CommandMessage poseCmd_time = new CommandMessage("defaultPose_time", "pose");
        poseCmd_time.addParameter("neckRotat", "0.3");
        poseCmd_time.addParameter("time", "500");
        commands.put("pose (time)", poseCmd_time);
        
        CommandMessage poseCmd_speed = new CommandMessage("defaultPose_speed", "pose");
        poseCmd_speed.addParameter("neckRotat", "0.3");
        poseCmd_speed.addParameter("speed", "100.0");
        commands.put("pose (speed)", poseCmd_speed);
        
        CommandMessage facsCmd_time = new CommandMessage("defaultFACS_time", "facs");
        facsCmd_time.addParameter("au05", "0.0");
        facsCmd_time.addParameter("au10", "0.0");
        facsCmd_time.addParameter("au12", "0.0");
        facsCmd_time.addParameter("au15", "0.0");
        facsCmd_time.addParameter("au22", "0.0");
        facsCmd_time.addParameter("au25", "0.0");
        facsCmd_time.addParameter("au43", "0.0");
        facsCmd_time.addParameter("time", "500");
        commands.put("facs (time)", facsCmd_time);

        CommandMessage facsCmd_speed = new CommandMessage("defaultFACS_speed", "facs");
        facsCmd_speed.addParameter("au05", "0.0");
        facsCmd_speed.addParameter("au10", "0.0");
        facsCmd_speed.addParameter("au12", "0.0");
        facsCmd_speed.addParameter("au15", "0.0");
        facsCmd_speed.addParameter("au22", "0.0");
        facsCmd_speed.addParameter("au25", "0.0");
        facsCmd_speed.addParameter("au43", "0.0");
        facsCmd_speed.addParameter("speed", "100.0");
        commands.put("facs (speed)", facsCmd_speed);
    }
    
    protected final void addLEDCommands()
    {
        CommandMessage ledCmd_color = new CommandMessage("defaultLED_color", "led");
        ledCmd_color.addParameter("color", "red");
        ledCmd_color.addParameter("id", "both");
        commands.put("led (color)", ledCmd_color);
        
        CommandMessage ledCmd_rgb = new CommandMessage("defaultLED_RGB", "led");
        ledCmd_rgb.addParameter("red", "1.0");
        ledCmd_rgb.addParameter("green", "0.0");
        ledCmd_rgb.addParameter("blue", "0.0");
        ledCmd_rgb.addParameter("id", "both");
        commands.put("led (RGB)", ledCmd_rgb);
    }
    
    protected final void addAudioCommands()
    {
        CommandMessage audioCmd = new CommandMessage("defaultAudio", "audio");
        audioCmd.addParameter("name", "Sounds/Computer");
        commands.put("audio", audioCmd);
        
        CommandMessage stopAudioCmd = new CommandMessage("defaultStopAudio", "stopAudio");
        commands.put("stopAudio", stopAudioCmd);        
    }
    
    
    protected final void addSetVoiceCommands(){
        CommandMessage setVoiceCmd_aisoy = new CommandMessage("defaultSetVoice_aisoy", "setVoice");
        setVoiceCmd_aisoy.addParameter("language", "en");
        setVoiceCmd_aisoy.addParameter("volume", "0.5");
        commands.put("setVoice (Aisoy)", setVoiceCmd_aisoy);

        CommandMessage setVoiceCmd_loquendo = new CommandMessage("defaultSetVoice_loquendo", "setVoice");
        setVoiceCmd_loquendo.addParameter("name", "Stefan");
        setVoiceCmd_loquendo.addParameter("language", "de");
        setVoiceCmd_loquendo.addParameter("volume", "50");
        setVoiceCmd_loquendo.addParameter("pitch", "50");
        setVoiceCmd_loquendo.addParameter("rate", "50");
        commands.put("setVoice (Loquendo)", setVoiceCmd_loquendo);
        
        CommandMessage setVoiceCmd_maryTTS = new CommandMessage("defaultSetVoice_maryTTS", "setVoice");
        setVoiceCmd_maryTTS.addParameter("name", "bits1-hsmm");
        setVoiceCmd_maryTTS.addParameter("language", "de");
        setVoiceCmd_maryTTS.addParameter("volume", "1.0");
        setVoiceCmd_maryTTS.addParameter("pitch", "normal");
        setVoiceCmd_maryTTS.addParameter("pitchAdd", "20.0");
        setVoiceCmd_maryTTS.addParameter("range", "0.5");
        setVoiceCmd_maryTTS.addParameter("rate", "1.0");
        setVoiceCmd_maryTTS.addParameter("robotise", "50.0");
        setVoiceCmd_maryTTS.addParameter("channel", "left");
        commands.put("setVoice (MaryTTS)", setVoiceCmd_maryTTS);
                
        CommandMessage setVoiceCmd_midi = new CommandMessage("defaultSetVoice_midi", "setVoice");
        setVoiceCmd_midi.addParameter("vowelBank", "0");
        setVoiceCmd_midi.addParameter("vowelProgram", "53");
        setVoiceCmd_midi.addParameter("humBank", "2048");
        setVoiceCmd_midi.addParameter("humProgram", "63");
        setVoiceCmd_midi.addParameter("pitch", "50");
        setVoiceCmd_midi.addParameter("pitchRange", "2");
        setVoiceCmd_midi.addParameter("downVelocity", "127");
        setVoiceCmd_midi.addParameter("upVelocity", "127");
        commands.put("setVoice (MIDI)", setVoiceCmd_midi);
    }
     
    
    public final CommandMessage get(String key)
    {
        if(key!= null)
            return commands.get(key);
        else return null;
    }
    
    public final Set<String> getKeys(){
        return commands.keySet();
    }
}
