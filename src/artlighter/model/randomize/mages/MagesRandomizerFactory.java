package artlighter.model.randomize.mages;

import artlighter.model.randomize.OnlyPrefixFilesRandomizer;
import artlighter.model.randomize.Randomizer;
import artlighter.model.randomize.RandomizerFactory;
import artlighter.model.randomize.SimpleRandomizer;

public class MagesRandomizerFactory implements RandomizerFactory {
    private OnlyPrefixFilesRandomizer bgr;
    private SimpleRandomizer sr;
    private MagesCharacterRandomizer cr;
    private OnlyPrefixFilesRandomizer cgr;

    @Override
    public Randomizer getCharacterRandomizer() {
        if (cr == null) cr = new MagesCharacterRandomizer();
        return cr;
    }

    @Override
    public Randomizer getSoundtrackRandomizer() {
        if (sr == null) sr = new SimpleRandomizer();
        return sr;
    }

    @Override
    public Randomizer getSoundRandomizer() {
        if (sr == null) sr = new SimpleRandomizer();
        return sr;
    }

    @Override
    public Randomizer getBackgroundRandomizer() {
        if (bgr == null) bgr = new OnlyPrefixFilesRandomizer("BG");
        return bgr;
    }

    @Override
    public Randomizer getCGRandomizer() {
        if (cgr == null) cgr = new OnlyPrefixFilesRandomizer("EV", "SG0_EV");
        return cgr;
    }

    @Override
    public Randomizer getVoiceRandomizer() {
        if (sr == null) sr = new SimpleRandomizer();
        return sr;
    }
}
