package net.earomc.synthesizer;

import net.earomc.synthesizer.debug.ByteArrayDumpCreator;
import net.earomc.synthesizer.audioplayer.AudioPlayer;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;

import javax.sound.sampled.*;
import java.io.*;

import static net.earomc.synthesizer.SimpleAudioConversion.*;

public class EaroSynthesizer {

    private static final int SAMPLE_RATE = 48000; // in Hertz/Hz | means 48000 samples per second
    private static final int FRAME_RATE = SAMPLE_RATE;
    private static final int SAMPLE_SIZE = 16; // in bits / bits per sample | 16 bits = 2 bytes | like a short
    private static final int CHANNELS = 2; // STEREO
    private static final int BUFFER_SIZE = 0x1000; // 4096 - in bytes (1 byte = 8 bits)
    private static final AudioFormat AUDIO_FORMAT
            = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            calcPCMFrameSize(CHANNELS, SAMPLE_SIZE),
            FRAME_RATE,
            false);

    public EaroSynthesizer() {
    }

    //TODO: Find way to constantly stream floats. DataInputStream?
    //TODO: Fix audio crackle

    public static void main(String[] args) throws FileNotFoundException {
        EaroSynthesizer synthesizer = new EaroSynthesizer();
        float[] sineWaves = sineWaveSamples(0, 8000);
        synthesizer.playSamples(sineWaves, AUDIO_FORMAT);
        //synthesizer.playAudioFile("ImperialMarch60.wav");
        //displayChart(sineWaves);
    }

    public static float[] sineWaveSamples(int freq, int durationMillis) {
        float[] samples = new float[(int) Math.ceil(SAMPLE_RATE * (durationMillis / 1000f))];
        double fullScale = fullScale(SAMPLE_SIZE) - 1;
        float volume = 0.02f; // has to be value between -1 and 1
        for (int i = 0; i < samples.length; i++) {
            double timeSeconds = 1d * i / SAMPLE_RATE; //
            samples[i] = sinFunc(volume, freq + ((i * 20f) / SAMPLE_RATE), timeSeconds);
        }

        /*try (FloatSampleArrayDumpCreator dumpCreator = new FloatSampleArrayDumpCreator("sine_wave_samples")) {
            dumpCreator.createDump(samples, fullScale, freq, SAMPLE_RATE, durationMillis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        return samples;
    }

    private static float sinFunc(float amplitude, float freq, double timeSeconds) {
        return (float) (amplitude * Math.sin(2f * Math.PI * freq * timeSeconds));
    }

    public void playSamples(float[] samples, AudioFormat audioFormat) {
        byte[] encodedSampleBytes = new byte[samples.length * bytesPerSample(SAMPLE_SIZE)];
        encode(samples, encodedSampleBytes, samples.length, audioFormat);
        InputStream encSampleBytesStream = new BufferedInputStream(new ByteArrayInputStream(encodedSampleBytes), BUFFER_SIZE);
        //InputStream encSampleBytesStream = new ByteArrayInputStream(encodedSampleBytes);
        try {
            playFromInputStream(encSampleBytesStream, audioFormat);
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void playFromInputStream(InputStream inputStream, AudioFormat format) throws LineUnavailableException, IOException {
        playFromInputStream(inputStream, format, null);
    }

    private void playFromInputStream(InputStream inputStream, AudioFormat format, @Nullable String dumpFileName) throws IOException, LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        // try with resources auto-closes sourcedataline before audio playback is finished.
        //try () {
        var sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open();
        sourceDataLine.start();

        ByteArrayDumpCreator dumpCreator = null;
        if (dumpFileName != null) {
            dumpCreator = new ByteArrayDumpCreator(dumpFileName);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            sourceDataLine.write(buffer, 0, bytesRead);
            if (dumpCreator != null) dumpCreator.addByteArray(buffer, bytesRead);
        }
        if (dumpCreator != null) dumpCreator.close();

        //}

    }

    private static void displayChart(float[] samples) {
        XYChart chart = new XYChartBuilder()
                .width(1280)
                .height(720)
                .title("Sine experiment")
                .xAxisTitle("t in seconds")
                .yAxisTitle("amplitude")
                .build();
        XYStyler styl = chart.getStyler();
        styl.setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        styl.setChartTitleVisible(false);
        styl.setLegendPosition(Styler.LegendPosition.InsideSW);
        styl.setMarkerSize(2);


        float[] seconds = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            seconds[i] = (i * 1f) / SAMPLE_RATE;
        }

        float[] count = new float[samples.length];
        for (float i = 0; i < samples.length; i++) {
            count[(int) i] = i;
        }
        chart.addSeries("sin", count, samples);

        new SwingWrapper<>(chart).displayChart();
    }


    public void playAudioFile(String fileName) throws FileNotFoundException {
        InputStream resource = getClass().getResourceAsStream("/" + fileName);
        if (resource == null) throw new FileNotFoundException(fileName + " could not be found.");
        BufferedInputStream bis = new BufferedInputStream(resource, BUFFER_SIZE);
        try (AudioInputStream audioInputStream = AudioPlayer.convertToPCM(AudioSystem.getAudioInputStream(bis))){
            System.out.println("Playing " + fileName);
            playFromInputStream(audioInputStream, audioInputStream.getFormat(), fileName);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    private float[] squareWave(int amplitude, int phase, int frequency) {
        int seconds = 1;
        float[] samples = new float[SAMPLE_RATE * seconds];
        for (int i = 0; i < samples.length; i++) {

        }
        return samples;
    }

    public static int calcPCMFrameSize(int channels, int sampleSize) {
        return channels * sampleSize / Byte.SIZE;
    }
}