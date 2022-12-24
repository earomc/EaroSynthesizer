package net.earomc.synthesizer;

import java.util.ArrayList;

public class FloatArrayConcatenator {
    ArrayList<float[]> floatArrays = new ArrayList<>();
    public float[] concat() {
        return Util.concatFloatArrays(floatArrays.toArray(float[][]::new));
    }

    public void append(float[] floats) {
        floatArrays.add(floats);
    }
}
