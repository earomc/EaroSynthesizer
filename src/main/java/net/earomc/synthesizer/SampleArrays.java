package net.earomc.synthesizer;

import net.earomc.synthesizer.waveform.Waveform;

import java.io.FileNotFoundException;

import static net.earomc.synthesizer.EaroSynthesizer.*;
import static net.earomc.synthesizer.SimpleAudioConversion.decode;

public class SampleArrays {
    public static float[][] getFrequencyModSamples() {
        return waveSamplesFrequencyMod(Waveform.SINE, 0.04f, 3f, 300, 150);
    }

    public static float[] getTestSamples() throws FileNotFoundException {
        //float[] sineWaves = sineWaveSamples(0, 3000);
        float[] samplesTaunt = decode(getAudioFileBytes("taunt.wav"), SAMPLE_SIZE, AUDIO_FORMAT);
        //float[] waveSamples = waveSamples(Waveform.SINE, 27.5f , VOLUME, 2);

        FloatArrayConcatenator concatenator = new FloatArrayConcatenator();

        Waveform[] waveforms = {Waveform.NOISE};
        for (Waveform waveForm : Waveform.WAVEFORMS) {
            concatenator.append(waveSamples(waveForm, 110, VOLUME, 1f, Util.phase01ToRadians(0.5f)));
        }

        concatenator.append(waveSamplesFrequencyMod(Waveform.TRIANGLE, 0.04f, 3, 300, 14)[0]);

        float[] waveformSamples1 = waveSamples(Waveform.TRIANGLE, 25, 0.02f, 2, 0);
        float[] waveformSamples2 = waveSamples(Waveform.SAW, 80, 0.05f, 8, 0);
        float[] waveformSamples3 = waveSamples(Waveform.SAW, 160, 0.05f, 8, 0);


        concatenator.append(waveformSamples1, waveformSamples2, waveformSamples3);

        concatenator.append(getAlleMeineEntchenSamples(Waveform.TRIANGLE));
        concatenator.append(silentSamples(0.5f));
        concatenator.append(getAlleMeineEntchenSamples(Waveform.SAW));
        concatenator.append(silentSamples(0.5f));
        concatenator.append(getAlleMeineEntchenSamples(Waveform.SINE));

        return concatenator.concat();
    }

    public static float[] getAlleMeineEntchenSamples(Waveform waveform) {
        FloatArrayConcatenator concatenator = new FloatArrayConcatenator();

        concatenator.append(silentSamples(1));
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(28), VOLUME, 0.5f, 0)); // C
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(30), VOLUME, 0.5f, 0)); // D
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(32), VOLUME, 0.5f, 0)); // E
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(33), VOLUME, 0.5f, 0)); // F

        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(35), VOLUME, 0.5f, 0)); // G
        concatenator.append(silentSamples(0.5f));
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(35), VOLUME, 0.5f, 0)); // G
        concatenator.append(silentSamples(0.5f));

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(37), VOLUME, 0.25f, 0)); // A
                concatenator.append(silentSamples(0.25f));
            }
            concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(35), VOLUME, 0.5f, 0)); // G
            concatenator.append(silentSamples(1.5f));
        }


        for (int i = 0; i < 4; i++) {
            concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(33), VOLUME, 0.25f, 0)); // F
            concatenator.append(silentSamples(0.25f));
        }
        for (int i = 0; i < 2; i++) {
            concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(32), VOLUME, 0.5f, 0)); // E
            concatenator.append(silentSamples(0.5f));
        }

        for (int i = 0; i < 4; i++) {
            concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(35), VOLUME, 0.25f, 0)); // G
            concatenator.append(silentSamples(0.25f));
        }
        concatenator.append(waveSamples(waveform, Util.calculateNoteFreq(28), VOLUME, 1f, 0)); // C

        return concatenator.concat();
    }

    public static float[] mix(float[] samples1, float[] samples2) {
        int max = Math.max(samples1.length, samples2.length);
        float[] longerSamples;
        if (samples1.length == max) {
            longerSamples = samples1;
        } else {
            longerSamples = samples2;
        }
        float[] mixedSamples = new float[max];
        for (int i = 0; i < mixedSamples.length; i++) {
            if (i < samples1.length && i < samples2.length) {
                mixedSamples[i] = 0.5f * (samples1[i] + samples2[i]);
            } else {
                mixedSamples[i] = longerSamples[i];
            }
        }
        return mixedSamples;
    }

    public static float[] silentSamples(float durationSeconds) {
        return Util.createEmptySampleArray(SAMPLE_RATE, durationSeconds);
    }

    public static float[] waveSamples(Waveform waveform, float freq, float amp, float durationSeconds, float phaseRadians) {
        float[] samples = Util.createEmptySampleArray(SAMPLE_RATE, durationSeconds);
        double periodSeconds = Util.freqToPeriod(freq);
        for (int i = 0; i < samples.length; i++) {
            float timeSeconds = ((float) i) / SAMPLE_RATE;
            samples[i] = waveform.sample(timeSeconds, periodSeconds, amp, phaseRadians);
        }
        return samples;
    }

    public static float[][] waveSamplesFrequencyMod(Waveform waveform, float amp, float durationSeconds, final float startFreq, final float endFreq) {
        float[] resultSamples = Util.createEmptySampleArray(SAMPLE_RATE, durationSeconds);
        float[] frequencies = Util.createEmptySampleArray(SAMPLE_RATE, durationSeconds);
        for (int i = 0; i < resultSamples.length; i++) {
            float timeSeconds = ((float) i) / SAMPLE_RATE;

            double freq;
            if (startFreq < endFreq) {
                freq = Util.mapRange(0, resultSamples.length, startFreq, endFreq, i);
            } else {
                freq = Util.mapRange(0, resultSamples.length, endFreq, startFreq, i);
            }
            double periodSeconds = Util.freqToPeriod(freq);
            if (i % 50 == 0) {
                System.out.println("freq = " + freq);
                System.out.println("periodSeconds = " + periodSeconds);
            }
            frequencies[i] = (float) freq;
            resultSamples[i] = waveform.sample(timeSeconds, periodSeconds, amp, 0);
        }
        return new float[][] {resultSamples, frequencies};
    }
}
