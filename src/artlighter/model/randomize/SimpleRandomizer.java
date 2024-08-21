package artlighter.model.randomize;

import artlighter.model.repack.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleRandomizer extends Randomizer {
    //protected Map<Entry, >
    @Override
    public int randomize(Entry[] entries) {
        int randomized = 0;
        List<Entry> list = new ArrayList<>(Arrays.asList(entries));
        Collections.shuffle(list, getRandom());
        for (int i = 0; i < entries.length; i++) {
            entries[i].setMappedFile(list.get(i));
            randomized++;
        }
        return randomized;
    }

}
