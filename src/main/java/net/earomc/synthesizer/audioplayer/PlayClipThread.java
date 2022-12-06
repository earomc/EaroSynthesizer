package net.earomc.synthesizer.audioplayer;

import javax.sound.sampled.Clip;

class PlayClipThread extends PlayThread {
    private Clip clip;
    private boolean loop = false;

    public PlayClipThread(Clip clip) {
        this.clip = clip;
    }

    public void run() {
        if (clip != null) {
            stopSound();

            // give the thread that is playing the clip a chance to
            // stop the clip before we restart it
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // don't do anything if the thread was interrupted
            }

            playSound();
        }
    }

    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    public void stopSound() {
        clip.stop();
    }

    public void playSound() {
        clip.setFramePosition(0);
        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
    }
}
