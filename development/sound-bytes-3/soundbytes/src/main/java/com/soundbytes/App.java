package com.soundbytes;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import com.soundbytes.sound.Recorder;
import com.soundbytes.sound.WavUtil;

/* 
 * As the 2024 Game is called 'Crescendo,' I thought it might be interesting
 * to maybe do some basic preliminary work in case the game requires the robots to 
 * hear and interpret sound.
 * 
 * For now, this implementation is for the system microphone, not for external hardware.
 * 
 * I used https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-os/src/main/java/com/baeldung/example/soundapi/App.java
 * to be able to listen to the audio and save it to a .wav format.
 * 
 * I used https://docs.oracle.com/javase/8/docs/technotes/guides/sound/programmer_guide/chapter7.html#a114527 to 
 * learn how to read the .wav file.
 * 
 * Different preset .wav files can be compared using musicg (https://code.google.com/archive/p/musicg/).
 * Assuming the robot was to respond to different sounds in next year's game, in this implementation, we compare the audio the robot hears
 * to a set of previously recorded .wav files to determine its response.
 * 
 * As of right now, the implementation is such that the robot has to check which preset sound most matches the input, then it displays the color
 * that corresponds to the sound.
 * 
 * Unfortunely, because of some deprecation issues, the similarity score is unable to produce a definite result sometimes.
 * https://stackoverflow.com/questions/71705980/class-test-in-unnamed-module-0x33f88ab-cannot-access-class-com-sun-media-soun
 * I have tried changing the way the audio is compared by using different methods from the library but have yet to reach a solution
 * in which the comparison delivers a finite response. Maybe a different, more maintained library is necessary?
 * 
 * The robot changes the LED it displays based sound it hears's resemblance to the given preset sounds:
 *      Note A1: red
 *      Note B1: green
 *      Note C1: blue
 * 
 * Overall, using Java's sound API and the musicg API by Google, we can start to envision how our robot
 * might be able to react to preset sounds--something it might experience in the 2024 game.
*/

public class App {
    public static void main(String[] args) throws Exception {

        AudioFormat format = buildAudioFormatInstance();

        Recorder recorder = Recorder.getInstance(format);

        System.out.println("Start recording...");
        recorder.start();
        Thread.sleep(Constants.RECORDING_TIME);
        recorder.stop();

        WavUtil wd = new WavUtil();
        Thread.sleep(3000);
        wd.saveToFile("SoundClip", AudioFileFormat.Type.WAVE, recorder.getAudioInputStream());
    }

    public static AudioFormat buildAudioFormatInstance() {
        AudioFormat.Encoding encoding = Constants.ENCODING;
        float rate = Constants.RATE;
        int channels = Constants.CHANNELS;
        int sampleSize = Constants.SAMPLE_SIZE;
        boolean bigEndian = Constants.BIG_ENDIAN;

        return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
    }
}
