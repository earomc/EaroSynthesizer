package net.earomc.synthesizer.debug;

import java.io.*;
import java.util.Arrays;

public class ByteArrayDumpCreator extends DumpCreator {

    public ByteArrayDumpCreator(String fileName) throws IOException {
        super(fileName + "_bytearray_dump");
    }

    public void addByteArray(byte[] bytes, int bytesRead) throws IOException {
        writer
                .append("buffer length: ")
                .append(String.valueOf(bytes.length)).append(" | bytes read: ")
                .append(String.valueOf(bytesRead))
                .append(" ").append(Arrays.toString(bytes));
        writer.newLine();
        writer.append("--- end of buffer dump ---");
        writer.newLine();
    }
}
