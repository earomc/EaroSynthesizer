package net.earomc.synthesizer.waveform.waveforms;

import net.earomc.synthesizer.waveform.Waveform;

import java.util.concurrent.ThreadLocalRandom;

public class Noise implements Waveform {
    @Override
    public float sample(float timeSeconds, float periodSeconds, float amp, float phaseRadians) {
        return amp * ThreadLocalRandom.current().nextFloat(-1f, 1f);
    }
}
