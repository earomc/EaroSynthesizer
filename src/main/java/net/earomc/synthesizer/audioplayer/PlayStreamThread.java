package net.earomc.synthesizer.audioplayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;

class PlayStreamThread extends PlayThread {
    private byte[] tempBuffer = new byte[10000];

    private AudioInputStream audioInputStream;
    private SourceDataLine sourceDataLine;

    private boolean loop;
    private boolean playing;

    public PlayStreamThread(AudioInputStream audioInputStream, SourceDataLine sourceDataLine) {
        this.audioInputStream = audioInputStream;
        this.sourceDataLine = sourceDataLine;
        playing = false;
    }

    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    public void start() {
        playSound();
        super.start();
    }

    public void playSound() {
        if (playing) {
            return;
        }

        try {
            audioInputStream.reset();
            sourceDataLine.start();
            playing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopSound() {
        sourceDataLine.flush();
        sourceDataLine.stop();
        playing = false;
    }

    public void run() {
        try {
            int cnt;
            while (true) {
                int avail = sourceDataLine.available();
                while (playing && (cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }
                    Thread.sleep(1);
                }

                // using this loop instead of sourceDataLine.drain() in case
                // the stopSound() method is called -- drain() is a blocking method
                while (playing && (sourceDataLine.available() < avail)) {
//                     System.out.println(sourceDataLine.available());
                }

                if (loop && playing) {
                    playing = false;
                    playSound();
                } else {
                    stopSound();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
