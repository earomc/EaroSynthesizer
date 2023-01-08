package net.earomc.synthesizer.waveform;

import net.earomc.synthesizer.waveform.waveforms.*;

public interface Waveform {

    double PERIOD_LENGTH = 2 * Math.PI;

    Waveform SAW = new Saw();
    Waveform TRIANGLE = new Triangle();
    Waveform SINE = new Sine();
    Waveform SQUARE = new Square();
    Waveform NOISE = new Noise();
    Waveform[] WAVEFORMS = {SAW, TRIANGLE, SINE, SQUARE, NOISE};

    float sample(float timeSeconds, float periodSeconds, float amp, float phaseRadians);

}
