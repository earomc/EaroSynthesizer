package net.earomc.synthesizer.ref.audioplayer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * This class contains utility methods for loading and playing audio
 * clips and streams.
 *
 * <p>General usage is as follows:
 * <p>First, preload sounds from files, and give those sounds a name:
 * <p><blockquote><pre>
 *    AudioPlayer.loadClip("moo", "cow.wav");
 *    AudioPlayer.loadClip("chirp", "bird.wav");</pre></blockquote>
 *
 * <p>The various loading methods return <code>true</code> if the requested
 * audio was loaded successfully, and <code>false</code> otherwise. This
 * value can be used, for example, to display an error message:
 *
 * <p><blockquote><pre>
 *    if (!AudioPlayer.loadClip("arf", "dog.wav"))
 *        System.out.println("Could not load dog sound.");</pre></blockquote>
 *
 * <p>Then, play the sounds by specifying the names they were given, and
 * whether or not the sound should loop (<code>true</code> or <code>false</code>):
 * <p><blockquote><pre>
 *    AudioPlayer.play("moo", true);
 *    AudioPlayer.play("chirp", false);</pre></blockquote>
 *
 * <p>A playing sound can be stopped as follows:
 * <p><blockquote><pre>
 *    AudioPlayer.stop("moo");
 *    AudioPlayer.stop("chirp");</pre></blockquote>
 *
 * <p>Sounds only need to be loaded once. Generally, if the sounds you want to
 * load are small and short, it is recommended that you use one of the
 * {@link #loadClip loadClip} methods. For longer sounds that may not fit in
 * memory, such as full songs, you should use one of the
 * {@link #loadStream loadStream} methods.
 *
 * <p>When done playing sounds (typically when exiting your program), use:
 *
 * <p><blockquote><pre>
 *    AudioPlayer.shutdown();</pre></blockquote>
 *
 * <p>This will stop all playing sounds.
 */
public class AudioPlayer {
    public static HashMap<String, AudioData> soundMap = new HashMap<>();

    /**
     * Loads a sound clip from a file and gives it the specified name.
     * This name can be used when calling the <code>{@link #play play}</code>
     * and <code>{@link #stop stop}</code> methods.
     * The sound clip is completely loaded into memory, so it is recommended that
     * this method be used for loading small or short sounds. For longer sounds,
     * consider using the <code>{@link #loadStream(String, String) loadStream}</code> method.
     *
     * @param soundName the name to give this audio stream
     * @param filename  the name of the file to load the audio stream from
     * @return <code>true</code> if the clip loaded successfully,
     * <code>false</code> otherwise
     */
    public static boolean loadClip(String soundName, String filename) {
        try {
            return loadClip(soundName, AudioSystem.getAudioInputStream(new File(filename)));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a sound clip from a <code>{@link java.net.URL}</code>
     * and gives it the specified name. This name can be used when calling
     * the <code>{@link #play play}</code> and <code>{@link #stop stop}</code> methods.
     * The sound clip is completely loaded into memory, so it is recommended that
     * this method be used for loading small or short sounds. For longer sounds,
     * consider using the <code>{@link #loadStream(String, URL) loadStream}</code> method.
     *
     * @param soundName the name to give this audio stream
     * @param url       the name of the file to load the audio stream from
     * @return <code>true</code> if the clip loaded successfully,
     * <code>false</code> otherwise
     */
    public static boolean loadClip(String soundName, URL url) {
        try {
            return loadClip(soundName, AudioSystem.getAudioInputStream(url));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadClip(String soundName, AudioInputStream audioInputStream) {
        boolean retVal = true;

        try {
            // convert the AudioInputStream to PCM format -- needed for loading
            // mp3 files and files in other formats
            audioInputStream = convertToPCM(audioInputStream);

            // get a line for the Clip and load the audio from the input stream
            DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);

            AudioData ad = new AudioData();
            ad.audioInputStream = audioInputStream;
            ad.dataLine = clip;

            soundMap.put(soundName, ad);
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
            retVal = false;
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     * Converts an AudioInputStream to PCM_SIGNED format if it is not already
     * either PCM_SIGNED or PCM_UNSIGNED.
     */
    public static AudioInputStream convertToPCM(AudioInputStream audioInputStream) {
        AudioFormat format = audioInputStream.getFormat();

        AudioFormat.Encoding encoding = format.getEncoding();
        //if ((encoding != AudioFormat.Encoding.PCM_SIGNED) && (encoding != AudioFormat.Encoding.PCM_UNSIGNED)) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(), format.isBigEndian()
            );
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        //}

        return audioInputStream;
    }

    /**
     * Loads an audio stream from a file and gives it the specified name.
     * This name can be used when calling the <code>{@link #play play}</code>
     * and <code>{@link #stop stop}</code> methods.
     *
     * @param soundName the name to give this audio stream
     * @param filename  the name of the file to load the audio stream from
     * @return <code>true</code> if the audio stream loaded successfully,
     * <code>false</code> otherwise
     */
    public static boolean loadStream(String soundName, String filename) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filename));
            return loadStream(soundName, audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads an audio stream from a <code>{@link java.net.URL}</code>
     * and gives it the specified name. This name can be used when calling
     * the <code>{@link #play play}</code> and <code>{@link #stop stop}</code> methods.
     *
     * @param soundName the name to give this audio stream
     * @param url       the name of the file to load the audio stream from
     * @return <code>true</code> if the audio stream loaded successfully,
     * <code>false</code> otherwise
     */
    public static boolean loadStream(String soundName, URL url) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            return loadStream(soundName, audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean loadStream(String soundName, AudioInputStream audioInputStream) {
        boolean retVal = true;

        try {
            // convert the audio input stream to a buffered input stream that supports
            // mark() and reset()
            BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
            audioInputStream = new AudioInputStream(bufferedInputStream, audioInputStream.getFormat(), audioInputStream.getFrameLength());

            try {
                // convert the AudioInputStream to PCM format -- needed for loading
                // mp3 files and files in other formats
                audioInputStream = convertToPCM(audioInputStream);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioInputStream.getFormat());
                SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);

                AudioData ad = new AudioData();
                ad.audioInputStream = audioInputStream;
                ad.dataLine = sourceDataLine;

                soundMap.put(soundName, ad);
                audioInputStream.mark(2000000000);
                sourceDataLine.open(audioInputStream.getFormat());
            } catch (Exception e) {
                e.printStackTrace();
                retVal = false;
            } finally {
                //                 ain.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            retVal = false;
        }

        return retVal;
    }

    /**
     * Plays a sound that has already been loaded by one of the
     * sound loading methods. The sound can be played once or
     * looped forever. If the specified sound name does not exist,
     * this method does nothing.
     *
     * @param soundName the name of the sound to play
     * @param loop      <code>true</code> if the sound should loop forever,
     *                  <code>false</code> if the sound should play once
     */
    public static void play(String soundName, boolean loop) {
        AudioData ad = soundMap.get(soundName);
        System.out.println(ad);
        if (ad != null) {
            if (ad.thread == null || !ad.thread.isAlive()) {
                if (ad.dataLine instanceof SourceDataLine) {
                    ad.thread = new PlayStreamThread(ad.audioInputStream, (SourceDataLine) ad.dataLine);
                } else if (ad.dataLine instanceof Clip) {
                    ad.thread = new PlayClipThread((Clip) ad.dataLine);
                } else {
                    return;
                }

                ad.thread.setLooping(loop);
                ad.thread.start();
            } else {
                ad.thread.stopSound();
                ad.thread.setLooping(loop);
                ad.thread.playSound();
            }
        } else throw new RuntimeException(soundName + " isn't registered");
    }

    /**
     * Stops playing the specified sound.
     *
     * @param soundName the name of the sound to stop
     */
    public static void stop(String soundName) {
        AudioData ad = soundMap.get(soundName);
        if (ad != null) {
            if (ad.thread != null) {
                ad.thread.stopSound();
            }
        }
    }

    /**
     * Stops all playing sounds and closes all lines and audio input streams.
     * Any previously loaded sounds will have to be re-loaded to be played again.
     */
    public static void shutdown() {
        for (AudioData ad : soundMap.values()) {
            if (ad != null) {
                if (ad.thread != null) {
                    ad.thread.stopSound();
                    ad.dataLine.close();
                    try {
                        ad.audioInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        soundMap.clear();
    }
}