package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.waveform.Waveform;

public class Square implements Waveform {
    @Override
    public float function(float timeSeconds, float periodSeconds, float amp, float phaseRadians) {
        return (float) (amp * Math.signum(Math.sin((2 * Math.PI * timeSeconds - phaseRadians) / periodSeconds)));
    }
}
