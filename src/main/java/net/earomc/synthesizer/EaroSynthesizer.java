package net.earomc.synthesizer;

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
import java.awt.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static net.earomc.synthesizer.SampleArrays.getFrequencyModSamples;
import static net.earomc.synthesizer.SampleArrays.waveSamples;
import static net.earomc.synthesizer.SimpleAudioConversion.encode;

public class EaroSynthesizer {

    public static final Logger LOGGER = Logger.getLogger("EaroSynthesizer");
    private ScheduledFuture<?> future;
    private ScheduledExecutorService executor;

    public static final int SAMPLE_RATE = 4000; // in Hertz/Hz | means 48000 samples per second
    public static final int FRAME_RATE = SAMPLE_RATE;
    public static final int SAMPLE_SIZE = 16; // in bits / bits per sample | 16 bits = 2 bytes | like a short
    public static final int CHANNELS = 2; // STEREO
    public static final int BUFFER_SIZE = 0x1000; // 4096 - in bytes (1 byte = 8 bits)
    public static final AudioFormat AUDIO_FORMAT
            = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            calcPCMFrameSize(CHANNELS, SAMPLE_SIZE),
            FRAME_RATE,
            false);
    public static final float VOLUME = 1f / 50; // has to be a value between -1 and 1


    public EaroSynthesizer() {
    }

    public static void main(String[] args) throws IOException {
        new EaroSynthesizer().waitForKeyboardInput();
    }


    //TODO: Make oscillator constantly output samples like an OutputStream


    public void waitForKeyboardInput() throws IOException {
        boolean running = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Ready for commands: (stop, playsound <sine|saw|triangle|noise|square>)");
            String readLine;
            //readLine = reader.readLine();
            readLine = "playsound test";

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
                } else if (readLine.endsWith("test")) {
                    float[][] frequencyModSamples = getFrequencyModSamples();
                    samples = frequencyModSamples[0];
                    displayFrequencyChart(samples, frequencyModSamples[1]);
                } else samples = new float[0];
                playSamples(samples, AUDIO_FORMAT);
                displayChart(samples);
                break;
            }
            System.out.println("Invalid command");

        }
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
        int bytesReadTotal = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            sourceDataLine.write(buffer, 0, bytesRead);
            if (dumpCreator != null) dumpCreator.addByteArray(buffer, bytesRead);
            //float[] samples = decode(buffer, BUFFER_SIZE, AUDIO_FORMAT);
            //if (dumpCreator1 != null) dumpCreator1.createDump(samples, SAMPLE_RATE);
            bytesReadTotal += bytesRead;
        }
        LOGGER.info("Wrote " + bytesReadTotal + " bytes to sdl");


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
                .title("Chart")
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

    private static void displayFrequencyChart(float[] samples, float[] frequencies) {
        XYChart chart = new XYChartBuilder()
                .width(1280)
                .height(720)
                .theme(Styler.ChartTheme.XChart)
                .title("Frequency Chart")
                .xAxisTitle("Frequency of wave in Hz")
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
        XYSeries frequenciesSeries = chart.addSeries("Frequencies", frequencies, samples);
        frequenciesSeries.setLineColor(Color.BLUE);

        //XYSeries samplesSeries = chart.addSeries("Samples", seconds, samples);
        //samplesSeries.setLineColor(Color.ORANGE);


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

    public static byte[] getAudioFileBytes(String fileName) throws FileNotFoundException {
        InputStream resource = EaroSynthesizer.class.getResourceAsStream("/" + fileName);
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