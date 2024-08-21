package artlighter.model.randomize;

public interface RandomizerFactory {

    Randomizer getCharacterRandomizer();
    Randomizer getSoundtrackRandomizer();
    Randomizer getSoundRandomizer();
    Randomizer getBackgroundRandomizer();
    Randomizer getCGRandomizer();
    Randomizer getVoiceRandomizer();

}
