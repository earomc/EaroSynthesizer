package net.earomc.synthesizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.earomc.synthesizer.debug.ByteArrayDumpCreator;
import net.earomc.synthesizer.debug.FloatSampleArrayDumpCreator;
import net.earomc.synthesizer.waveform.Waveform;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.earomc.synthesizer.SimpleAudioConversion.decode;
import static net.earomc.synthesizer.SimpleAudioConversion.encode;

public class EaroSynthesizer
        //extends Application
{


    public static final Logger LOGGER = Logger.getLogger("EaroSynthesizer");
    private ScheduledFuture<?> future;
    private ScheduledExecutorService executor;

    private static final int SAMPLE_RATE = 44100; // in Hertz/Hz | means 48000 samples per second
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
    private static final float VOLUME = 1f / 100; // has to be a value between -1 and 1


    public EaroSynthesizer() {
    }

    public static void main(String[] args) {
        //launch(args);
        System.out.println("lel");
    }

    //@Override
    public void start(Stage stage) throws Exception {
        LOGGER.setLevel(Level.ALL);
        EaroSynthesizer synthesizer = new EaroSynthesizer();
        //synthesizer.waitForKeyboardInput();
        //synthesizer.playTestSequence();
        Canvas canvas = new Canvas();
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        Scene scene = new Scene(new VBox(canvas), 1280, 720);

        // do shit with pixelWriter
        //https://stackoverflow.com/questions/28417623/the-fastest-filling-one-pixel-in-javafx
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }

    //TODO: Make oscillator constantly output samples like an OutputStream


    public void waitForKeyboardInput() throws IOException {
        boolean running = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String readLine = reader.readLine();
            if (readLine.equalsIgnoreCase("stop")) break;
            if (readLine.startsWith("playsound")) {
                float[] samples;
                if (readLine.endsWith("sine")) {
                    samples = waveSamples(Waveform.SINE, 100, VOLUME, 1, 0);
                } else if (readLine.endsWith("saw")) {
                    samples = waveSamples(Waveform.SAW, 100, VOLUME, 1, 0);
                } else if (readLine.endsWith("triangle")) {
                    samples = waveSamples(Waveform.TRIANGLE, 100, VOLUME, 1, 0);
                } else if (readLine.endsWith("noise")) {
                    samples = waveSamples(Waveform.NOISE, 100, VOLUME, 1, 0);
                } else if (readLine.endsWith("square")) {
                    samples = waveSamples(Waveform.SQUARE, 100, VOLUME, 1, 0);
                } else samples = new float[0];
                playSamples(samples, AUDIO_FORMAT);
            }
            System.out.println("Invalid command");
        }
    }


    private void playTestSequence() throws FileNotFoundException {
        //float[] sineWaves = sineWaveSamples(0, 3000);
        float[] samplesTaunt = decode(getAudioFileBytes("taunt.wav"), SAMPLE_SIZE, AUDIO_FORMAT);
        //float[] waveSamples = waveSamples(Waveform.SINE, 27.5f , VOLUME, 2);

        FloatArrayConcatenator concatenator = new FloatArrayConcatenator();
        Waveform[] waveforms = {Waveform.NOISE};
        for (Waveform waveForm : Waveform.WAVEFORMS) {
            concatenator.append(waveSamples(waveForm, 110, VOLUME, 1f, Util.phase01ToRadians(0.5f)));
        }

        float[] allWaveformSamples = concatenator.concat();
        float[] waveformSamples1 = waveSamples(Waveform.TRIANGLE, 25, 0.02f, 2, 0);
        float[] waveformSamples2 = waveSamples(Waveform.SAW, 80, 0.05f, 8, 0);
        float[] waveformSamples3 = waveSamples(Waveform.SAW, 160, 0.05f, 8, 0);

        //displayChart(waveformSamples1);
        playSamples(waveformSamples1, AUDIO_FORMAT, "waves");
        //displayChart(samples);
        //synthesizer.playAudioFile("yoyo.wav");
        //byte[] audioFileBytes = synthesizer.getAudioFileBytes("yoyo.wav");
        //float[] audioFileSamples = decode(audioFileBytes, SAMPLE_SIZE, AUDIO_FORMAT);
        //float[] mixedSamples = synthesizer.mix(audioFileSamples, sineWaves);
        //synthesizer.playSamples(mixedSamples, AUDIO_FORMAT);
    }

    private float[] mix(float[] samples1, float[] samples2) {
        int max = Math.max(samples1.length, samples2.length);
        float[] longerSamples;
        if (samples1.length == max) {
            longerSamples = samples1;
        } else {
            longerSamples = samples2;
        }
        float[] mixedSamples = new float[max];
        for (int i = 0; i < mixedSamples.length; i++) {
            if (i < samples1.length && i < samples2.length) {
                mixedSamples[i] = 0.5f * (samples1[i] + samples2[i]);
            } else {
                mixedSamples[i] = longerSamples[i];
            }
        }
        return mixedSamples;
    }

    public static float[] waveSamples(Waveform waveform, float freq, float amp, float durationSeconds, float phaseRadians) {
        float[] samples = Util.createEmptySampleArray(SAMPLE_RATE, durationSeconds);
        float periodSeconds = Util.freqToPeriod(freq);
        for (int i = 0; i < samples.length; i++) {
            float timeSeconds = ((float) i) / SAMPLE_RATE;
            samples[i] = waveform.sample(timeSeconds, periodSeconds, amp, phaseRadians);
        }
        return samples;
    }

    public static float[] sawWaveSamples(float freq, float amp, int durationSeconds) {
        return null;
    }

    private static float sinFunc(float amplitude, float freq, float timeSeconds) {
        return (float) (amplitude * Math.sin(2f * Math.PI * freq * timeSeconds));
    }

    public void playSamples(float[] samples, AudioFormat audioFormat, @Nullable String dumpFileName) {
        byte[] encodedSampleBytes = encode(samples, SAMPLE_SIZE, audioFormat);
        ByteArrayInputStream encSampleBytesStream = new ByteArrayInputStream(encodedSampleBytes);
        //InputStream encSampleBytesStream = new ByteArrayInputStream(encodedSampleBytes);
        try {
            FloatSampleArrayDumpCreator dumpCreator1 = new FloatSampleArrayDumpCreator(dumpFileName);
            dumpCreator1.createDump(samples, (int) audioFormat.getSampleRate());
            dumpCreator1.close();
            playFromInputStream(encSampleBytesStream, audioFormat, dumpFileName);
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void playSamples(float[] samples, AudioFormat audioFormat) {
        playSamples(samples, audioFormat, null);
    }

    public void playFromInputStream(InputStream inputStream, AudioFormat format) throws LineUnavailableException, IOException {
        playFromInputStream(inputStream, format, null);
    }

    private void playFromInputStream(InputStream inputStream, AudioFormat format, @Nullable String dumpFileName) throws IOException, LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

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
            //float[] samples = decode(buffer, BUFFER_SIZE, AUDIO_FORMAT);
            //if (dumpCreator1 != null) dumpCreator1.createDump(samples, SAMPLE_RATE);
            LOGGER.info("Wrote " + bytesRead + " bytes to sdl");
        }


        // 3 Seconds after playback, close SourceDataLine and inputStream to make the program be able to finish.
        System.out.println("Audio playback should be finished... Closing SDL in 3 seconds:");
        this.executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Closed SDL");
            if (future != null) future.cancel(true);
            if (executor != null) executor.shutdown();
        }, 3, TimeUnit.SECONDS);

        if (dumpCreator != null) dumpCreator.close();
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
        chart.addSeries("Samples", seconds, samples);

        /*
        float[] count = new float[samples.length];
        for (float i = 0; i < samples.length; i++) {
            count[(int) i] = i;
        }
        chart.addSeries("Samples", count, samples);
         */

        LOGGER.info("Init SwingWrapper " + chart);
        new SwingWrapper<>(chart).displayChart();
    }

    public void playAudioFile(String fileName) throws FileNotFoundException {
        InputStream resource = getClass().getResourceAsStream("/" + fileName);
        if (resource == null) throw new FileNotFoundException(fileName + " could not be found.");
        BufferedInputStream bis = new BufferedInputStream(resource, BUFFER_SIZE);
        try (AudioInputStream audioInputStream = convertToDefaultFormat(AudioSystem.getAudioInputStream(bis))) {
            LOGGER.info("Playing " + fileName);
            playFromInputStream(audioInputStream, audioInputStream.getFormat(), fileName);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getAudioFileBytes(String fileName) throws FileNotFoundException {
        InputStream resource = getClass().getResourceAsStream("/" + fileName);
        if (resource == null) throw new FileNotFoundException(fileName + " could not be found.");
        BufferedInputStream bis = new BufferedInputStream(resource, BUFFER_SIZE);
        try (AudioInputStream audioInputStream = convertToDefaultFormat(AudioSystem.getAudioInputStream(bis))) {
            return audioInputStream.readAllBytes();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AudioInputStream convertToDefaultFormat(AudioInputStream audioInputStream) {
        return AudioSystem.getAudioInputStream(AUDIO_FORMAT, audioInputStream);
    }


    public static int calcPCMFrameSize(int channels, int sampleSize) {
        return channels * sampleSize / Byte.SIZE;
    }
}