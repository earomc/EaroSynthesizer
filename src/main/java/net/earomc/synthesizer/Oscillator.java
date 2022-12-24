package net.earomc.synthesizer;

import net.earomc.synthesizer.waveform.Waveform;

public class Oscillator {

    // frequency of the wave in Hertz (Hz)
    private float freq;
    private float amp;
    private Waveform waveform;

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public void setAmp(float amp) {
        if (amp < 1 || amp > 1)
            throw new IllegalArgumentException("Illegal value for amp: " + amp + "! Has to be a value between -1 and 1");
        this.amp = amp;
    }

    public float getFreq() {
        return freq;
    }

    public float getAmp() {
        return amp;
    }

    public Waveform getWaveform() {
        return waveform;
    }

    public void setWaveform(Waveform waveform) {
        this.waveform = waveform;
    }
}
