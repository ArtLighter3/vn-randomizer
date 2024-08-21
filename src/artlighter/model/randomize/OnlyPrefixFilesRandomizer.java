package artlighter.model.randomize;

import artlighter.model.repack.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnlyPrefixFilesRandomizer extends Randomizer {
    private final String[] prefixes;
    public OnlyPrefixFilesRandomizer(String... prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public int randomize(Entry[] entries) {
        int randomized = 0;
        List<Entry> list = new ArrayList<>();
        for (Entry entry : entries) {
            if (startsWith(entry.getFileName(), prefixes))
                list.add(entry);
        }
        Collections.shuffle(list, getRandom());
        for (int i = 0, j = 0; i < list.size() && j < entries.length; j++) {
            if (startsWith(entries[j].getFileName(), prefixes)) {
                //swapRawData(entries[j], list.get(i));
                entries[j].setMappedFile(list.get(i));
                i++;
                randomized++;
            }
        }
        return randomized;
    }

    private boolean startsWith(String src, String[] arr) {
        for (String s : arr) {
            if (src.startsWith(s)) return true;
        }
        return false;
    }

}
