package net.earomc.synthesizer.debug;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import java.util.Arrays;

public class Debug {
    public static void sendSourceLineInfo(Mixer.Info mixerInfo) {
        Line.Info[] sourceLineInfos = AudioSystem.getMixer(mixerInfo).getSourceLineInfo();
        if (sourceLineInfos.length > 0) {
            System.out.println("Source lines:");
            Arrays.stream(sourceLineInfos)
                    .forEach(srcLInfo -> System.out.println("--- " + srcLInfo.toString()));
        } else System.out.println("No source lines.");
    }

    public static void sendTargetLineInfo(Mixer.Info mixerInfo) {
        Line.Info[] targetLineInfos = AudioSystem.getMixer(mixerInfo).getTargetLineInfo();
        if (targetLineInfos.length > 0) {
            System.out.println("Target lines:");
            Arrays.stream(targetLineInfos)
                    .forEach(trgtLInfo -> System.out.println("--- " + trgtLInfo.toString()));
        } else System.out.println("No target lines.");
    }

    public static void sendSingleMixerInfo(Mixer.Info info) {
        System.out.println(info);
        sendSourceLineInfo(info);
        sendTargetLineInfo(info);
        System.out.println("\n");
    }

    public static void sendAllMixerInfo() {
        Arrays.stream(AudioSystem.getMixerInfo()).forEach(Debug::sendSingleMixerInfo);
    }
}
