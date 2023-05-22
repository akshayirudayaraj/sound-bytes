package com.soundbytes;

import javax.sound.sampled.AudioFormat;

public class Constants {
    // AUDIO FORMAT CONSTANTS
    public static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    public static final float RATE = 44100.0f;
    public static final int CHANNELS = 1;
    public static final int SAMPLE_SIZE = 8;
    public static final boolean BIG_ENDIAN = true;

    // NOTES
    public static final String NOTE_A_FILE_PATH = "Piano.ff.A1.wav";
    public static final String NOTE_B_FILE_PATH = "Piano.ff.B1.wav";
    public static final String NOTE_C_FILE_PATH = "Piano.ff.C1.wav";

    // WAIT
    public static final int RECORDING_TIME = 5 * 1000; // milliseconds
}
