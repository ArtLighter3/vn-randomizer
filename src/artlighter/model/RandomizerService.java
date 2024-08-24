package artlighter.model;

import artlighter.model.randomize.Randomizer;
import artlighter.model.randomize.RandomizerFactory;
import artlighter.model.randomize.mages.MagesRandomizerFactory;
import artlighter.model.repack.Entry;
import artlighter.model.repack.MpkRepacker;
import artlighter.model.repack.Repacker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RandomizerService {
    private final Map<String, Repacker> repackers = new HashMap<>();
    private final Map<NovelType, RandomizerFactory> factories = new HashMap<>();
    private boolean lowMemoryMode = false;

    public RandomizerService() {
        repackers.put(new String(MpkRepacker.MPK_HEADER, StandardCharsets.UTF_8), new MpkRepacker());

        factories.put(NovelType.STEINSGATE, new MagesRandomizerFactory());
        factories.put(NovelType.CHAOSCHILD, new MagesRandomizerFactory());
    }
    public RandomizerService(boolean lowMemoryMode) {
        this();
        this.lowMemoryMode = lowMemoryMode;
    }

    public int randomizeCharacters(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING CHARACTERS------");
        return randomizeAndWrite(source, dest, factories.get(type).getCharacterRandomizer());
    }
    public int randomizeSoundtrack(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING SOUNDTRACK------");
        return randomizeAndWrite(source, dest, factories.get(type).getSoundtrackRandomizer());
    }
    public int randomizeSoundEffects(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING SOUND EFFECTS------");
        return randomizeAndWrite(source, dest, factories.get(type).getSoundRandomizer());
    }
    public int randomizeCGs(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING CGs------");
        return randomizeAndWrite(source, dest, factories.get(type).getCGRandomizer());
    }
    public int randomizeBackgrounds(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING BACKGROUNDS------");
        return randomizeAndWrite(source, dest, factories.get(type).getBackgroundRandomizer());
    }
    public int randomizeVoiceLines(String source, String dest, NovelType type) {
        System.out.println("------RANDOMIZING VOICE LINES------");
        return randomizeAndWrite(source, dest, factories.get(type).getVoiceRandomizer());
    }

    public int randomizeAndWrite(String source, String dest, Randomizer randomizer) {
        System.out.println("Initializing repacker for " + source);
        Repacker repacker = getRepacker(source);
        if (repacker == null) return 0;
        repacker.setLowMemoryMode(lowMemoryMode);

        System.out.println("Reading " + source);
        Entry[] entries = getFileEntries(source, repacker);
        if (entries == null) return 0;

        System.out.println("Randomizing entries");
        int randomized = randomizer.randomize(entries);

        System.out.println("Writing " + dest);
        int written = write(repacker, source, dest, entries);
        if (written == 0) return 0;

        return randomized;
    }

    public int write(Repacker repacker, String source, String dest, Entry[] entries) {
        try {
            repacker.writeMappedFiles(source, dest, entries);
            return entries.length;
        } catch (IOException ex) {
            System.out.println("ERROR: Failed to write randomized files to " + dest + "\n" + ex.getMessage());
        }
        return 0;
    }

    public Entry[] getFileEntries(String filename, Repacker repacker) {
        try {
            return repacker.getFileData(filename);
        } catch (IOException ex) {
            System.out.println("ERROR: Failed to read " + filename + " with " +
                                repacker.getClass().getSimpleName() + "\n" + ex.getMessage());
        }
        return null;
    }

    public Repacker getRepacker(String filename) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
            byte[] signature = new byte[8];
            bis.read(signature);
            String signatureString = new String(signature, StandardCharsets.UTF_8);
            if (repackers.containsKey(signatureString)) return repackers.get(signatureString);
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: File " + filename + " not found");
        } catch (IOException ex) {
            System.out.println("ERROR: Failed to identify archive format of " + filename + "\n" + ex.getMessage());
        }
        return null;
    }

    public enum NovelType {
        STEINSGATE("USRDIR/chara.mpk", "USRDIR/bgm.mpk",
                "USRDIR/bg.mpk", "USRDIR/bg.mpk",
                "USRDIR/se.mpk", "USRDIR/voice.mpk"),
        CHAOSCHILD("USRDIR/chara.mpk", "USRDIR/bgm.mpk",
                "USRDIR/bg1.mpk", "USRDIR/bg2.mpk",
                "USRDIR/se.mpk", "USRDIR/voice.mpk");
        //String[0] - Character file
        //String[1] - Soundtrack file
        //String[2] - Background file
        //String[3] - CGs file
        //String[4] - SFX file
        //String[5] - Voicelines file
        private String[] filenames;
        NovelType(String... filenames) {
            this.filenames = filenames;
        }
        public String[] getFilenames() {
            return filenames;
        }
    }

}
