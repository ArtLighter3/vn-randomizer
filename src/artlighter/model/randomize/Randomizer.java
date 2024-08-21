package artlighter.model.randomize;

import artlighter.model.repack.Entry;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public abstract class Randomizer {

    public abstract int randomize(Entry[] entries);

    protected Random getRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException ex) {
            return new SecureRandom();
        }
    }

}
