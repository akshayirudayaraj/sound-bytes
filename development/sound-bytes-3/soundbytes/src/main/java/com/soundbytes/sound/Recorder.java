package com.soundbytes.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;

import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.fingerprint.FingerprintSimilarityComputer;
import com.soundbytes.Constants;

import java.util.ArrayList;

/*
 * This file contains the logic that allows the audio input to be
 * written to a .wav file and also the logic that reads
 * from the .wav file.
 * 
 * Additionally, the sound comparison logic done with musicg is performed here.
 */

public class Recorder implements Runnable {
    private static Recorder mInstance;
    private AudioInputStream audioInputStream;
    private AudioFormat format;
    public Thread thread;
    private ArrayList<Float> similarityScores;

    public Recorder(AudioFormat format) {
        super();
        this.format = format;
    }
    
    public Recorder() {
        super();
    }

    public static Recorder getInstance(AudioFormat format) {
        if (mInstance == null) {
            mInstance = new Recorder(format);
        }

        return mInstance;
    }

    public AudioInputStream getAudioInputStream() {
        return audioInputStream;
    }

    private void setAudioInputStream(AudioInputStream aStream) {
        this.audioInputStream = aStream;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
    }

    public Thread getThread() {
        return thread;
    }

    public ArrayList<Float> getSimilarityScores() {
        return similarityScores;
    }

    public void start() {
        thread = new Thread(this);
        thread.setName("Capture Microphone");
        thread.start();
    }

    public void stop() {
        thread = null;
    }

    @Override
    public void run() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final TargetDataLine line = getTargetDataLineForRecord();) {
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            final int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            buildByteOutputStream(out, line, frameSizeInBytes, bufferLengthInBytes);
            this.audioInputStream = new AudioInputStream(line);
            setAudioInputStream(convertToAudioIStream(out, frameSizeInBytes));

            similarityScores.add(matchSounds(out, Constants.NOTE_A_FILE_PATH));
            similarityScores.add(matchSounds(out, Constants.NOTE_B_FILE_PATH));
            similarityScores.add(matchSounds(out, Constants.NOTE_C_FILE_PATH));

            audioInputStream.reset();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void buildByteOutputStream(final ByteArrayOutputStream out, final TargetDataLine line, int frameSizeInBytes, final int bufferLengthInBytes) throws IOException {
        final byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();
        while (thread != null) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
        }

        out.close();
    }

    // converts preset .wav files to byte[] that can be used to compare to recorded audio
    public static byte[] readAudioFileData(final String filePath) {
        byte[] data = null;
        try {
            final ByteArrayOutputStream baout = new ByteArrayOutputStream();
            final File file = new File(filePath);
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
    
            byte[] buffer = new byte[4096];
            int c;
            while ((c = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                baout.write(buffer, 0, c);
            }
            audioInputStream.close();
            baout.close();
            data = baout.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public float matchSounds(final ByteArrayOutputStream out, final String comparedAudioFileName) {
        byte[] firstFingerPrint = out.toByteArray();
        byte[] secondFingerPrint = readAudioFileData(comparedAudioFileName);

        // gets the similarity between the audio files
        // ouputs a number between 0 and 1, where 0 means no similar feature is found and 1 means in average there is at least one match in every frame
        FingerprintSimilarity fingerprintSimilarity = new FingerprintSimilarityComputer(firstFingerPrint, secondFingerPrint).getFingerprintsSimilarity();
        System.out.println("Similarity score = " + fingerprintSimilarity.getSimilarity());
        return fingerprintSimilarity.getSimilarity();
    }

    public AudioInputStream convertToAudioIStream(final ByteArrayOutputStream out, int frameSizeInBytes) {
        byte audioBytes[] = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
        System.out.println("Recording complete.");
        return audioStream;
    }

    public TargetDataLine getTargetDataLineForRecord() {
        TargetDataLine line;
        Info info = new Info(TargetDataLine.class, format);
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (final Exception ex) {
            return null;
        }
        return line;
    }
}