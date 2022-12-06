package net.earomc.synthesizer.debug;

import java.io.IOException;
import java.util.Arrays;

public class FloatSampleArrayDumpCreator extends DumpCreator {
    public FloatSampleArrayDumpCreator(String fileName) throws IOException {
        super(fileName);
    }
    public void createDump(float[] samples, double fullScale, int freq, int sampleRate, int durationMillis) throws IOException {
        writer.append(Arrays.toString(samples));
        writer.append("fullScale(SAMPLE_SIZE) = ").append(String.valueOf(fullScale));
        writer.append("samples.length = ").append(String.valueOf(samples.length));
        writer.append("freq = ").append(String.valueOf(freq)).append(", sampleRate = ").append(String.valueOf(sampleRate)).append(", durationMillis = ").append(String.valueOf(durationMillis));
    }
}
