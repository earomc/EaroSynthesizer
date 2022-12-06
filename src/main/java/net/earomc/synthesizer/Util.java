package net.earomc.synthesizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class Util {
    /**
     * Checks if a mixer supports all the lines with the given info.
     * @param mixer the mixer you want to check.
     * @param lineInfos varargs array of line infos.
     * @return
     */
    public static boolean supportsLines(Mixer mixer, Line.Info ... lineInfos) {
        for (var lineInfo : lineInfos) {
            if (!mixer.isLineSupported(lineInfo)) return false;
        }
        return true;
    }

    public static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        final int SAMPLE_SIZE = 16;
        int frameSize =  ch * SAMPLE_SIZE / Byte.SIZE; // = ch * 2
        //frame size indicates the size in bytes of a frame. meaning per
        return new AudioFormat(PCM_SIGNED, rate, SAMPLE_SIZE, ch, frameSize, rate, false);
    }

    public static int twoToPow(int x) {
        return 1 << x;
    }

}
