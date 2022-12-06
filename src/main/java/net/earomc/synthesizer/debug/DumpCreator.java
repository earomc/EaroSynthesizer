package net.earomc.synthesizer.debug;

import java.io.*;

public abstract class DumpCreator implements Closeable {
    protected final BufferedWriter writer;

    public DumpCreator(String fileName) throws IOException {
        File file = new File(fileName + ".txt");
        if (file.exists()) file.delete();
        writer = new BufferedWriter(new FileWriter(file, true));
        writer.append(fileName);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
