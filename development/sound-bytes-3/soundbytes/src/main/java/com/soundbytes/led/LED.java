package com.soundbytes.led;

import javax.sound.sampled.AudioFormat;
import com.soundbytes.sound.Recorder;
import java.util.ArrayList;
import com.soundbytes.Constants;
import java.lang.Math.max;

import com.ctre.phoenix.led.CANdle;
import com.team254.frc2023.Constants;
import com.team254.frc2023.led.LEDStateContainer;
import com.team254.frc2023.led.TimedLEDState;
import com.team254.lib.drivers.Subsystem;
import com.team254.lib.loops.ILooper;
import com.team254.lib.loops.Loop;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/*
 * The robot's LEDs reflect the sound it hears that most resembles
 * a given preset sound (A1, B1, or C1)
 * 
 * Most code here is reused from FRC-2023 with the new state and logic 
 * for DISPLAY_MATCHING_SOUND.
 */

public class LED extends Subsystem {

    public enum WantedAction {
        DISPLAY_MATCHING_SOUND,
        DISPLAY_BATTERY_LOW,
        DISPLAY_GOOD_BATTERY,
        DISPLAY_SUPERSTRUCTURE,
        DISPLAY_CONFIGURE_FAULT,
        OFF
    }

    private enum SystemState {
        DISPLAYING_MATCHING_SOUND,
        DISPLAYING_BATTERY_LOW,
        DISPLAYING_GOOD_BATTERY,
        DISPLAYING_SUPERSTRUCTURE,
        DISPLAYING_CONFIGURE_FAULT,
        OFF
    }

    private static LED mInstance;

    private CANdle mCANdle;
    private SystemState mSystemState = SystemState.OFF;
    private WantedAction mWantedAction = WantedAction.OFF;

    private LEDStateContainer mDesiredLEDState = new LEDStateContainer();

    private TimedLEDState mSuperstructureLEDState = TimedLEDState.StaticLEDState.kStaticOff;

    public synchronized static LED getInstance() {
        if (mInstance == null) {
            mInstance = new LED();
        }
        return mInstance;
    }

    private LED() {
        mCANdle = new CANdle(Constants.kCANdleId, Constants.kCANivoreCANBusName);
    }

    public synchronized void setSuperstructureLEDState(TimedLEDState intakeLEDState) {
        mSuperstructureLEDState = intakeLEDState;
    }

    public synchronized void setWantedAction(WantedAction wantedAction) {
        mWantedAction = wantedAction;
    }

    @Override
    public void registerEnabledLoops(ILooper enabledLooper) {
        enabledLooper.register(new Loop() {
            double stateStartTime;

            @Override
            public void onStart(double timestamp) {
                stateStartTime = timestamp;
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized (LED.this) {
                    SystemState newState = getStateTransition();
                    if (mSystemState != newState) {
                        System.out.println(timestamp + ": LED changed state: " + mSystemState + " -> " + newState);
                        mSystemState = newState;
                        stateStartTime = timestamp;
                    }
                    double timeInState = timestamp - stateStartTime;
                    //SmartDashboard.putString("System State", mSystemState.toString());
                    switch (mSystemState) {
                        case DISPLAYING_MATCHING_SOUND:
                            setMatchingSound(timeInState);
                            break;
                        case DISPLAYING_SUPERSTRUCTURE:
                            setSuperstructureLEDCommand(timeInState);
                            break;
                        case DISPLAYING_BATTERY_LOW:
                            setBatteryLowCommand(timeInState);
                            break;
                        case DISPLAYING_GOOD_BATTERY:
                            setGoodBattery(timeInState);
                            break;
                        case OFF:
                            setOffCommand(timeInState);
                            break;
                        case DISPLAYING_CONFIGURE_FAULT:
                            setConfigureFault(timeInState);
                            break;
                        default:
                            System.out.println("Fell through on LED commands: " + mSystemState);
                            // setOffCommand(timeInState);
                            break;
                    }
                    mDesiredLEDState.writePixels(mCANdle);
                }
            }

            @Override
            public void onStop(double timestamp) {}
        });
    }

    private void setSuperstructureLEDCommand(double timeInState) {
        mSuperstructureLEDState.getCurrentLEDState(mDesiredLEDState, timeInState);
    }

    private void setOffCommand(double timeInState) {
        TimedLEDState.StaticLEDState.kStaticOff.getCurrentLEDState(mDesiredLEDState, timeInState);
    }

    private void setConfigureFault(double timeInState) {
        TimedLEDState.BlinkingLEDState.kConfigureFail.getCurrentLEDState(mDesiredLEDState, timeInState);
    }

    private void setMatchingSound (double timeInState) {
        Recorder recorder = Recorder.getInstance(buildAudioFormatInstance());

        ArrayList<Float> similarityScores = recorder.getSimilarityScores();

        // index 0 is matching a, index 1 is matching b, index 2 is matching c
        // size is of the array is just 3 in this case since the sound is only being cross-checked to three preset sounds
        float max = similarityScores.get(0);
        for (int i = 0; i < similarityScores.size(); i++) {
            max = Math.max(max, similarityScores.get(i));
        }

        if (max == similarityScores.get(0)) {
            TimedLEDState.StaticLEDState.kMatchingA.getCurrentLEDState(mDesiredLEDState, timeInState);
        } else if (max == similarityScores.get(1)) {
            TimedLEDState.StaticLEDState.kMatchingB.getCurrentLEDState(mDesiredLEDState, timeInState);
        } else if (max == similarityScores.get(2)) {
            TimedLEDState.StaticLEDState.kMatchingC.getCurrentLEDState(mDesiredLEDState, timeInState);
        }
    }

    private void setGoodBattery(double timeInState) {
        TimedLEDState.StaticLEDState.kStaticRobotZeroedWithGoodBattery.getCurrentLEDState(mDesiredLEDState, timeInState);
    }

    private void setBatteryLowCommand(double timeInState) {
        TimedLEDState.StaticLEDState.kStaticBatteryLow.getCurrentLEDState(mDesiredLEDState, timeInState);
    }

    private boolean configure_fault = false;
    public synchronized void setConfigureFault(boolean fault){
        configure_fault = fault;
    }

    private SystemState getStateTransition() {
        if (configure_fault) return SystemState.DISPLAYING_CONFIGURE_FAULT;
        switch (mWantedAction) {
            case DISPLAY_MATCHING_SOUND:
                return SystemState.DISPLAYING_MATCHING_SOUND;
            case DISPLAY_SUPERSTRUCTURE:
                return SystemState.DISPLAYING_SUPERSTRUCTURE;
            case DISPLAY_GOOD_BATTERY:
                return SystemState.DISPLAYING_GOOD_BATTERY;
            case DISPLAY_BATTERY_LOW:
                return SystemState.DISPLAYING_BATTERY_LOW; 
            case OFF:
                return SystemState.OFF;
            default:
                System.out.println("Fell through on LED wanted action check: " + mWantedAction);
                return SystemState.OFF;
        }
    }

    @Override
    public boolean checkSystem() {
        return false;
    }

    @Override
    public synchronized void outputTelemetry(boolean disabled) {}

    @Override
    public void stop() {}

    public static AudioFormat buildAudioFormatInstance() {
        AudioFormat.Encoding encoding = Constants.ENCODING;
        float rate = Constants.RATE;
        int channels = Constants.CHANNELS;
        int sampleSize = Constants.SAMPLE_SIZE;
        boolean bigEndian = Constants.BIG_ENDIAN;

        return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
    }
}