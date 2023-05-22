package com.soundbytes.sound;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavUtil {
    // writes input stream data to .wav file
    public boolean saveToFile(String name, AudioFileFormat.Type fileType, AudioInputStream audioInputStream) {
        System.out.println("Saving...");

        if (null == name || null == fileType || audioInputStream == null) {
            return false;
        }

        File myFile = new File(name + "." + fileType.getExtension());
        
        try {
            audioInputStream.reset();
        } catch (Exception e) {
            return false;
        }

        int i = 0;
        while (myFile.exists()) {
            String temp = i + myFile.getName();
            myFile = new File(temp);
            // TODO: fix file increment naming?
        }

        try {
            AudioSystem.write(audioInputStream, fileType, myFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        System.out.println("Saved " + myFile.getAbsolutePath());
        return true;
    }
}