package net.earomc.synthesizer.waveform;

public interface Waveform {
    float[] getSamples(int freq, int sampleRate, int durationMillis);
}
