package com.chatApplication.chatClient.gui.handlers;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class AudioHandler {

    private static AudioHandler handler;
    private HashMap<String, Clip> sounds;

    private AudioHandler() {
        sounds = new HashMap<String, Clip>();
    }

    public static AudioHandler getInstance() {
        if (handler == null) {
            handler = new AudioHandler();
        }
        return handler;
    }
    private int position = 0;

    /** Website too look at: www.javalobby.org/java/forums/t18465.html */
    public void load(String resourcePath, String name) {
        // Establish path to file
        URL resource = AudioHandler.class.getResource(resourcePath);

        // Get the audio input from the file and base format
        AudioInputStream input = null;
        try {
            input = AudioSystem.getAudioInputStream(resource);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        AudioFormat baseFormat = input.getFormat();

        if(baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED){
            try{
                Clip c = AudioSystem.getClip();
                c.open(input);
                sounds.put(name, c);
                return;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        // Decode format
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

        // Create new input stream for new format
        AudioInputStream decodedIn = AudioSystem.getAudioInputStream(decodedFormat, input);

        Clip c = null;
        try {
            c = AudioSystem.getClip();
            c.open(decodedIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sounds.put(name, c);
    }

    public void play(String name, int loopCount) {
        if(sounds.get(name).isRunning()){
            sounds.get(name).stop();
        }
        sounds.get(name).setFramePosition(0);
        sounds.get(name).loop(loopCount);
    }

    public void stop(String name){
        if (sounds.get(name).isRunning()){
            sounds.get(name).stop();
        }
    }

    public void pause (String name){
        if (sounds.get(name).isRunning()){
            position = sounds.get(name).getFramePosition();
            sounds.get(name).stop();
            System.out.println("frame position -------> " + position);
        }
    }

    public void resume (String name, int loopCount) {
        if (!sounds.get(name).isRunning()){
            sounds.get(name).setFramePosition(position);
            sounds.get(name).loop(loopCount);
        }
    }

    public void adjustVolume(String name, int value){
        FloatControl control = (FloatControl)sounds.get(name).getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(value);
    }

    public boolean isRunning(String name){
        return sounds.get(name).isRunning();
    }

    public int getFrameposition(String name){
        return sounds.get(name).getFramePosition();
    }

    public void setFramePosition(String name, int position){
        sounds.get(name).setFramePosition(position);
    }
}
