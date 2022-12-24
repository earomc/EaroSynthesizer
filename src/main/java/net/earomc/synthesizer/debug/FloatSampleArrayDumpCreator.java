package net.earomc.synthesizer.debug;

import java.io.IOException;
import java.util.Arrays;

public class FloatSampleArrayDumpCreator extends DumpCreator {
    public FloatSampleArrayDumpCreator(String fileName) throws IOException {
        super(fileName + "_sample_dump");
    }
    public void createDump(float[] samples, int sampleRate) throws IOException {
        writer.append(Arrays.toString(samples));
        writer.append("samples.length = ").append(String.valueOf(samples.length));
        writer.append(", sampleRate = ").append(String.valueOf(sampleRate));
    }
}
