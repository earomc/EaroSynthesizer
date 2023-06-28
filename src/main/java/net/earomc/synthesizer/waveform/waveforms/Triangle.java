package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.waveform.Waveform;

public class Triangle implements Waveform {
    @Override
    public float sample(float timeSeconds, double periodSeconds, float amp, float phaseRadians) {
        return (float) ((2 * amp / Math.PI) * Math.asin(Math.sin((2 * Math.PI * timeSeconds - phaseRadians) / periodSeconds)));
    }
}
