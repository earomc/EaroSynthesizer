package net.earomc.synthesizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

public class Util {
    /**
     * Checks if a mixer supports all the lines with the given info.
     *
     * @param mixer     the mixer you want to check.
     * @param lineInfos varargs array of line infos.
     * @return
     */
    public static boolean supportsLines(Mixer mixer, Line.Info... lineInfos) {
        for (var lineInfo : lineInfos) {
            if (!mixer.isLineSupported(lineInfo)) return false;
        }
        return true;
    }

    public static AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        final int SAMPLE_SIZE = 16;
        int frameSize = ch * SAMPLE_SIZE / Byte.SIZE; // = ch * 2
        //frame size indicates the size in bytes of a frame. meaning per
        return new AudioFormat(PCM_SIGNED, rate, SAMPLE_SIZE, ch, frameSize, rate, false);
    }

    public static int twoToPow(int x) {
        return 1 << x;
    }

    public static float[] createEmptySampleArray(int sampleRate, float durationSeconds) {
        return new float[(int) Math.ceil(sampleRate * durationSeconds)];
    }

    /**
     * Given two ranges
     * [a1, a2] and [b1, b2]
     * then a value s in range [a1, a2]
     * is linearly mapped to a value t in range [b1, b2]
     */
    public static double mapRange(double a1, double a2, double b1, double b2, double s) {
        return b1 + ((s - a1) * (b2 - b1)) / (a2 - a1);
    }

    /**
     * Maps a phase represented as a value from 0 to 1 to radians representation
     * <p><a href="https://en.wikipedia.org/wiki/Phase_(waves)">See "Phase" on Wikipedia</a></p>
     *
     * @param phase01 The phase as a value from 0 to 1.
     * @return Phase in radians.
     */
    public static float phase01ToRadians(float phase01) {
        if (phase01 < 0 || phase01 > 1)
            throw new IllegalArgumentException("Phase input cannot be smaller than 0 or bigger than 1");

        // 0 -> 0, 1 -> 2 * PI
        return (float) mapRange(0, 1, 0, 2 * Math.PI, phase01);
    }
    /**
     * Method represents the phase function.
     * The phase value is basically a point in the wave period.
     * <p>
     * See
     * <a href="https://en.wikipedia.org/wiki/Phase_(waves)#Mathematical_definition">Phase "Mathematical Definition" on Wikipedia</a>
     * </p>
     *
     * @param periodSeconds The period/wavelength in seconds.
     * @param timeSeconds   The current time (t) of the function in seconds
     * @param timeSeconds0  The "origin" time (t0), beginning of the cycle in seconds.
     * @return Returns the phase in radians.
     */
    public static float calcPhase(float periodSeconds, float timeSeconds, float timeSeconds0) {
        return (float) (2 * Math.PI * discardIntPart((timeSeconds - timeSeconds0) / periodSeconds));
    }

    /**
     * Removes the integer part of a fractional number. 7.2 -> 0.2, 1.5 -> 0.5.
     * So it is always below 0.
     * <p>
     * See
     * <a href="https://en.wikipedia.org/wiki/Phase_(waves)#Mathematical_definition">Phase "Mathematical Definition" on Wikipedia</a>
     * </p>
     */
    private static float discardIntPart(float in) {
        return in - (float) Math.floor(in);
    }

    static float freqToPeriod(float freqHz) {
        return 1f / freqHz;
    }

    public static float pitchUpOctave(int octaves, float freq) {
        if (octaves < 0) throw new IllegalArgumentException("Cannot pitch up negative octaves!");
        if (octaves == 0) return freq;
        if (octaves == 1) return freq * 2;
        return freq * (float) Math.pow(2, octaves);
    }

    public static float pitchDownOctave(int octaves, float freq) {
        if (octaves < 0) throw new IllegalArgumentException("Cannot pitch down negative octaves!");
        if (octaves == 0) return freq;
        if (octaves == 1) return freq / 2;
        return freq / (float) Math.pow(2, octaves);
    }

    public static float[] concatFloatArrays(float[] ... floatArrays) {
        int totalLength = 0;
        for (float[] array : floatArrays) {
            totalLength += array.length;
        }
        float[] result = new float[totalLength];
        int offset = 0;
        for (float[] array : floatArrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
