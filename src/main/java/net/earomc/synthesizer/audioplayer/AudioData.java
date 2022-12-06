package net.earomc.synthesizer.audioplayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;

public class AudioData {
    public AudioInputStream audioInputStream = null;
    public DataLine dataLine = null;
    public PlayThread thread = null;
}
