package net.earomc.synthesizer;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import static java.lang.Math.*;

/**
 * <p>Performs simple audio format conversion.</p>
 *
 * <p>Encodes samples (represented as floats) into bytes that represent the audio in the given audio format</p>
 * <p>
 * An audio sample is represented as a float with the range -1 to 1.
 * 1 Meaning the speaker membrane is fully extended,
 * -1 meaning the speaker membrane is fully retracted, 0 meaning a neutral position.
 * </p>
 * The java "float" is a 32-bit signed floating point number,
 * meaning the maximum sample size for this class is 32-bit, which is more than enough for the vast majority of audio applications.
 * (For example: Most audio is in 16-bit and some very high quality audio has a 24-bit sample size.)
 * The sample size also known as "(audio) bit depth" or "bits per sample" defines how many bits are there to represent
 * each sample. The higher the sample size, the more possible values are there to represent one sample. the lower the quantization error and therefore the higher the audio quality/accuracy.
 * <p>More info on wikipedia: <a href="https://en.wikipedia.org/wiki/Audio_bit_depth">...</a></p>
 * <p></p>
 *
 * <p>This class turns arrays of samples (of floats) into arrays of bytes. One byte is only 8 bit, while most audio
 * is more than that, so each sample
 * </p>
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code  AudioInputStream ais = ... ;
 * SourceDataLine  line = ... ;
 * AudioFormat      fmt = ... ;
 *
 * // do setup
 *
 * for (int blen = 0; (blen = ais.read(bytes)) > -1;) {
 *     int slen;
 *     slen = SimpleAudioConversion.decode(bytes, samples, blen, fmt);
 *
 *     // do something with samples
 *
 *     blen = SimpleAudioConversion.encode(samples, bytes, slen, fmt);
 *     line.write(bytes, 0, blen);
 * }}</pre>
 *
 * @author Radiodef
 * @see <a href="http://stackoverflow.com/a/26824664/2891664">Overview on Stack Overflow</a>
 */
public final class SimpleAudioConversion {
    private SimpleAudioConversion() {
    }

    /**
     * Converts from a byte array to an audio sample float array.
     *
     * @param bytes    the byte array, filled by the AudioInputStream
     * @param samples  an array to fill up with audio samples
     * @param bytesLen the return value of AudioInputStream.read / amount of read bytes from an InputStream
     * @param fmt      the source AudioFormat
     * @return the number of valid audio samples converted
     * @throws NullPointerException           if bytes, samples or fmt is null
     * @throws ArrayIndexOutOfBoundsException if bytes.length is less than bytesLen or
     *                                        if samples.length is less than bytesLen / bytesPerSample(fmt.getSampleSizeInBits())
     */
    public static int decode(byte @NotNull [] bytes,
                             float @NotNull [] samples,
                             int bytesLen,
                             @NotNull AudioFormat fmt) {
        int bitsPerSample = fmt.getSampleSizeInBits();
        int bytesPerSample = bytesPerSample(bitsPerSample);
        boolean isBigEndian = fmt.isBigEndian();
        Encoding encoding = fmt.getEncoding();
        double fullScale = fullScale(bitsPerSample);

        int i = 0;
        int s = 0;
        while (i < bytesLen) {
            long bits = unpackBits(bytes, i, isBigEndian, bytesPerSample);
            float sample = 0f;

            if (encoding == Encoding.PCM_SIGNED) {
                bits = extendSign(bits, bitsPerSample);
                sample = (float) (bits / fullScale);

            } else if (encoding == Encoding.PCM_UNSIGNED) {
                bits = unsignedToSigned(bits, bitsPerSample);
                sample = (float) (bits / fullScale);

            } else if (encoding == Encoding.PCM_FLOAT) {
                if (bitsPerSample == 32) {
                    sample = Float.intBitsToFloat((int) bits);
                } else if (bitsPerSample == 64) {
                    sample = (float) Double.longBitsToDouble(bits);
                }
            } else if (encoding == Encoding.ULAW) {
                sample = bitsToMuLaw(bits);

            } else if (encoding == Encoding.ALAW) {
                sample = bitsToALaw(bits);
            }

            samples[s] = sample;
            // i = bytes decoded
            i += bytesPerSample;
            // s = samples decoded
            s++;
        }

        return s;
    }

    public static float[] decode(byte[] bytes, int sampleSize, AudioFormat audioFormat) {
        float[] samples = new float[(int) ceil((float) bytes.length / bytesPerSample(sampleSize))];
        decode(bytes, samples, bytes.length, audioFormat);
        return samples;
    }

    /**
     * Converts from an audio sample float array to a byte array which is made of bytes that resemble the given audio format.
     * samples (floats) -> bytes for given audio format.
     *
     * @param samples    an array of audio samples to encode
     * @param bytes      an array to fill up with encoded audio bytes
     * @param samplesLen the return value of the decode method / number of samples that are taken from the given samples array starting at index 0.
     * @param fmt        the destination AudioFormat
     * @return the number of valid bytes converted
     * @throws NullPointerException           if samples, bytes or fmt is null
     * @throws ArrayIndexOutOfBoundsException if samples.length is less than samplesLen or
     *                                        if bytes.length is less than samplesLen * bytesPerSample(fmt.getSampleSizeInBits())
     */
    public static int encode(float[] samples, // float can store 4 bytes = 32 bit
                             byte[] bytes,
                             int samplesLen,
                             AudioFormat fmt) {
        int bitsPerSample = fmt.getSampleSizeInBits();
        int bytesPerSample = bytesPerSample(bitsPerSample);
        boolean isBigEndian = fmt.isBigEndian();
        Encoding encoding = fmt.getEncoding();
        double fullScale = fullScale(bitsPerSample);

        int i = 0; // position in the given byte array where the packed (into bytes) bits represented as a long are written to.
        int s = 0; // sample counter increased with every loop iteration
        while (s < samplesLen) {
            float sample = samples[s];
            long bits = 0L;

            if (encoding == Encoding.PCM_SIGNED) {
                bits = (long) (sample * fullScale);

            } else if (encoding == Encoding.PCM_UNSIGNED) {
                bits = (long) (sample * fullScale);
                bits = signedToUnsigned(bits, bitsPerSample);
            } else if (encoding == Encoding.PCM_FLOAT) {
                if (bitsPerSample == 32) {
                    bits = Float.floatToRawIntBits(sample);
                } else if (bitsPerSample == 64) {
                    bits = Double.doubleToRawLongBits(sample);
                }
            } else if (encoding == Encoding.ULAW) {
                bits = muLawToBits(sample);

            } else if (encoding == Encoding.ALAW) {
                bits = aLawToBits(sample);
            }

            packBits(bytes, i, bits, isBigEndian, bytesPerSample);

            i += bytesPerSample;
            s++;
        }

        return i;
    }

    public static byte[] encode(float[] samples, int sampleSizeBits, AudioFormat audioFormat) {
        byte[] encodedSampleBytes = new byte[samples.length * bytesPerSample(sampleSizeBits)];
        encode(samples, encodedSampleBytes, samples.length, audioFormat);
        return encodedSampleBytes;
    }

    /**
     * Computes the block-aligned bytes per sample of the audio format,
     * using Math.ceil(bitsPerSample / 8.0).
     * <p>
     * Round towards the ceiling because formats that allow bit depths
     * in non-integral multiples of 8 typically pad up to the nearest
     * integral multiple of 8. So for example, a 31-bit AIFF file will
     * actually store 32-bit blocks.
     *
     * @param bitsPerSample the return value of AudioFormat.getSampleSizeInBits
     * @return The block-aligned bytes per sample of the audio format.
     */
    public static int bytesPerSample(int bitsPerSample) {
        return ((bitsPerSample + 7) >>> 3); // optimization switched in from original comment. Original code: (int) ceil(bitsPerSample / 8.0);
    }

    /**
     * Computes the largest magnitude representable by the audio format,
     * using Math.pow(2.0, bitsPerSample - 1). Note that for two's complement
     * audio, the largest positive value is one less than the return value of
     * this method.
     * <p>
     * The result is returned as a double because in the case that
     * bitsPerSample is 64, a long would overflow.
     *
     * @param bitsPerSample the return value of AudioFormat.getBitsPerSample
     * @return the largest magnitude representable by the audio format
     */
    public static double fullScale(int bitsPerSample) {
        return (1L << (bitsPerSample - 1)); // optimization switched in from original comment. Original code: pow(2.0, bitsPerSample - 1)
    }

    /**
     * Additional javadocs by earomc
     * <p>
     * Extracts one sample in the given byte array into one long representing the sample as a fugging number.
     * Takes care of byte order n shit.
     *
     * @param bytes          The bytes that are turned into a long
     * @param i              The starting point / offset of in the byte array. (Where the sample starts)
     * @param isBigEndian    Whether the byte order of the sample / audio format is big endian or little endian
     * @param bytesPerSample How many bytes there are in a sample in the audio format.
     * @return Returns the sample as a long in
     */
    private static long unpackBits(byte[] bytes,
                                   int i,
                                   boolean isBigEndian,
                                   int bytesPerSample) {
        switch (bytesPerSample) {
            case 1:
                return unpack8Bit(bytes, i);
            case 2:
                return unpack16Bit(bytes, i, isBigEndian);
            case 3:
                return unpack24Bit(bytes, i, isBigEndian);
            default:
                return unpackAnyBit(bytes, i, isBigEndian, bytesPerSample);
        }
    }

    private static long unpack8Bit(byte[] bytes, int i) {
        return bytes[i] & 0xffL;
    }

    private static long unpack16Bit(byte[] bytes,
                                    int i,
                                    boolean isBigEndian) {
        if (isBigEndian) {
            return (
                    ((bytes[i] & 0xffL) << 8)
                            | (bytes[i + 1] & 0xffL)
            );
        } else {
            return (
                    (bytes[i] & 0xffL)
                            | ((bytes[i + 1] & 0xffL) << 8)
            );
        }
    }

    private static long unpack24Bit(byte[] bytes,
                                    int i,
                                    boolean isBigEndian) {
        if (isBigEndian) {
            return (
                    ((bytes[i] & 0xffL) << 16)
                            | ((bytes[i + 1] & 0xffL) << 8)
                            | (bytes[i + 2] & 0xffL)
            );
        } else {
            return (
                    (bytes[i] & 0xffL)
                            | ((bytes[i + 1] & 0xffL) << 8)
                            | ((bytes[i + 2] & 0xffL) << 16)
            );
        }
    }

    private static long unpackAnyBit(byte[] bytes,
                                     int i,
                                     boolean isBigEndian,
                                     int bytesPerSample) {
        long temp = 0;

        if (isBigEndian) {
            for (int b = 0; b < bytesPerSample; b++) {
                temp |= (bytes[i + b] & 0xffL) << (
                        8 * (bytesPerSample - b - 1)
                );
            }
        } else {
            for (int b = 0; b < bytesPerSample; b++) {
                temp |= (bytes[i + b] & 0xffL) << (8 * b);
            }
        }

        return temp;
    }

    /**
     * Additional javadocs by earomc
     * <p>
     * Basically packs bits represented as long to bytes represented as byte array.
     * Turns a sample long (temp) into a byte array (with the lengths of bytesPerSample) and writes that as a section to the given byte array at position i.
     *
     * @param bytes          The byte array you want to write t
     * @param i              The position in the given byte array where the bytes are written to.
     * @param temp           this long represents a sample
     * @param isBigEndian    if true = big endian, if false = little endian.
     * @param bytesPerSample How many bytes are in the sample you want to turn into bytes.
     */

    private static void packBits(byte[] bytes,
                                 int i,
                                 long temp,
                                 boolean isBigEndian,
                                 int bytesPerSample) {
        switch (bytesPerSample) {
            case 1:
                pack8Bit(bytes, i, temp);
                break;
            case 2:
                pack16Bit(bytes, i, temp, isBigEndian);
                break;
            case 3:
                pack24Bit(bytes, i, temp, isBigEndian);
                break;
            default:
                packAnyBit(bytes, i, temp, isBigEndian, bytesPerSample);
                break;
        }
    }

    private static void pack8Bit(byte[] bytes, int i, long temp) {
        bytes[i] = (byte) (temp & 0xffL);
    }

    private static void pack16Bit(byte[] bytes,
                                  int i,
                                  long temp,
                                  boolean isBigEndian) {
        if (isBigEndian) {
            bytes[i] = (byte) ((temp >>> 8) & 0xffL);
            bytes[i + 1] = (byte) (temp & 0xffL);
        } else {
            bytes[i] = (byte) (temp & 0xffL);
            bytes[i + 1] = (byte) ((temp >>> 8) & 0xffL);
        }
    }

    private static void pack24Bit(byte[] bytes,
                                  int i,
                                  long temp,
                                  boolean isBigEndian) {
        if (isBigEndian) {
            bytes[i] = (byte) ((temp >>> 16) & 0xffL);
            bytes[i + 1] = (byte) ((temp >>> 8) & 0xffL);
            bytes[i + 2] = (byte) (temp & 0xffL);
        } else {
            bytes[i] = (byte) (temp & 0xffL);
            bytes[i + 1] = (byte) ((temp >>> 8) & 0xffL);
            bytes[i + 2] = (byte) ((temp >>> 16) & 0xffL);
        }
    }

    private static void packAnyBit(byte[] bytes,
                                   int i,
                                   long temp,
                                   boolean isBigEndian,
                                   int bytesPerSample) {
        if (isBigEndian) {
            for (int b = 0; b < bytesPerSample; b++) {
                bytes[i + b] = (byte) (
                        (temp >>> (8 * (bytesPerSample - b - 1))) & 0xffL
                );
            }
        } else {
            for (int b = 0; b < bytesPerSample; b++) {
                bytes[i + b] = (byte) ((temp >>> (8 * b)) & 0xffL);
            }
        }
    }

    private static long extendSign(long temp, int bitsPerSample) {
        int bitsToExtend = Long.SIZE - bitsPerSample;
        return (temp << bitsToExtend) >> bitsToExtend;
    }

    private static long unsignedToSigned(long temp, int bitsPerSample) {
        return temp - (long) fullScale(bitsPerSample);
    }

    private static long signedToUnsigned(long temp, int bitsPerSample) {
        return temp + (long) fullScale(bitsPerSample);
    }

    // mu-law constant
    private static final double MU = 255.0;
    // A-law constant
    private static final double A = 87.7;
    // natural logarithm of A
    private static final double LN_A = log(A);

    private static float bitsToMuLaw(long temp) {
        temp ^= 0xffL;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        return (float) (
                signum(sample)
                        *
                        (1.0 / MU)
                        *
                        (pow(1.0 + MU, abs(sample)) - 1.0)
        );
    }

    private static long muLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        sample = (float) (
                sign * (log(1.0 + (MU * sample)) / log(1.0 + MU))
        );

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0xffL;
    }

    private static float bitsToALaw(long temp) {
        temp ^= 0x55L;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        float sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / (1.0 + LN_A))) {
            sample = (float) (sample * ((1.0 + LN_A) / A));
        } else {
            sample = (float) (exp((sample * (1.0 + LN_A)) - 1.0) / A);
        }

        return sign * sample;
    }

    private static long aLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / A)) {
            sample = (float) ((A * sample) / (1.0 + LN_A));
        } else {
            sample = (float) ((1.0 + log(A * sample)) / (1.0 + LN_A));
        }

        sample *= sign;

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0x55L;
    }
}
