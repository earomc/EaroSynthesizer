package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.waveform.Waveform;

public class Saw implements Waveform {
    @Override
    public float sample(float timeSeconds, float periodSeconds, float amp, float phaseRadians) {
        return (float) ((2 * amp / Math.PI) * Math.atan(Math.tan((2 * Math.PI * timeSeconds - phaseRadians) / (2 * periodSeconds))));
    }
}
