package net.earomc.synthesizer;

import java.util.ArrayList;

public class FloatArrayConcatenator {
    ArrayList<float[]> arrays = new ArrayList<>();
    public float[] concat() {
        return Util.concatFloatArrays(arrays.toArray(float[][]::new));
    }

    public FloatArrayConcatenator append(float[] floats) {
        arrays.add(floats);
        return this;
    }

    public FloatArrayConcatenator append(float[] ... floatArrays) {
        for (float[] floatArray : floatArrays) {
            arrays.add(floatArray);
        }
        return this;
    }

    public void clear() {
        arrays.clear();
    }
}
