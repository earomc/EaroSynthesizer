package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.waveform.Waveform;

public class Sine implements Waveform {
    @Override
    public float function(float timeSeconds, float periodSeconds, float amp, float phaseRadians) {
        return (float) (amp * Math.sin((2f * Math.PI * timeSeconds - phaseRadians) / periodSeconds));
    }
}
