package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.Util;
import net.earomc.synthesizer.waveform.Waveform;

public class Sine implements Waveform {
    @Override
    public float sample(float timeSeconds, double periodSeconds, float amp, float phaseRadians) {
        float sample = (float) (amp * Math.sin((2f * Math.PI * timeSeconds - phaseRadians) / periodSeconds));

        return sample;
    }
}
