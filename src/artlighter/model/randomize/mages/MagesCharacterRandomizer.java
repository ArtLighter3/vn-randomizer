package artlighter.model.randomize.mages;

import artlighter.model.randomize.Randomizer;
import artlighter.model.repack.Entry;

import java.util.*;

public class MagesCharacterRandomizer extends Randomizer {
    private static final int CHARACTER_NAME_LENGTH = 3, SKIN_CODE_POSITION = 4,
            SIZE_CODE_POSITION = 5, POSE_CODE_POSITION = 6;
    @Override
    public int randomize(Entry[] entries) {
        int randomized = 0;
        Map<String, List<Entry>> characterEntries = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i++) {
            String characterId = entries[i].getFileName().substring(0, CHARACTER_NAME_LENGTH);
            if (characterEntries.containsKey(characterId)) characterEntries.get(characterId).add(entries[i]);
            else {
                characterEntries.put(characterId, new ArrayList<>());
                characterEntries.get(characterId).add(entries[i]);
            }
        }

        Map<List<Entry>, List<Entry>> mappedCharacters = getRandomizedMap(new ArrayList<>(characterEntries.values()));
        for (Map.Entry<List<Entry>, List<Entry>> mapEntry : mappedCharacters.entrySet()) {
            List<Entry> original = mapEntry.getKey();
            List<Entry> mapped = mapEntry.getValue();
            for (int i = 0; i < original.size() || i < mapped.size(); i++) {
                original.get(i).setMappedFile(mapped.get(i));
                randomized++;
            }
        }
        return randomized;
    }
    private Map<List<Entry>, List<Entry>> getRandomizedMap(List<? extends List<Entry>> characterEntries) {
        Map<List<Entry>, List<Entry>> shuffled = new LinkedHashMap<>();
        List<List<Entry>> shuffledList = new ArrayList<>();
        for (List<Entry> list : characterEntries) {
            shuffledList.add(new ArrayList<>(list));
        }
        Collections.shuffle(shuffledList, getRandom());
        for (int i = 0; i < characterEntries.size(); i++) {
            randomizeSkins(characterEntries.get(i), shuffledList.get(i), shuffled);
        }
        return shuffled;
    }
    private void randomizeSkins(List<Entry> original, List<Entry> mapped, Map<List<Entry>, List<Entry>> shuffled) {
        int originalSkins = getSkinsCount(original);
        int mappedSkins = getSkinsCount(mapped);
        List<Integer> shuffledSkins = new ArrayList<>();

        for (int i = 0; i < originalSkins; i++) {
            if (originalSkins >= mappedSkins && i < mappedSkins) shuffledSkins.add(i);
            else shuffledSkins.add(getRandom().nextInt(mappedSkins));
        }
        Collections.shuffle(shuffledSkins, getRandom());
        for (int i = 0; i < originalSkins; i++) {
            List<Entry> list1 = getEntriesBySkin(original, i);
            List<Entry> list2 = getEntriesBySkin(mapped, shuffledSkins.get(i));
            randomizePoses(list1, list2, shuffled);
        }
    }
    private void randomizePoses(List<Entry> original, List<Entry> mapped, Map<List<Entry>, List<Entry>> shuffled) {
        List<Integer> shuffledPoses = new ArrayList<>();
        int originalPoses = getPosesCount(original);
        int mappedPoses = getPosesCount(mapped);
        for (int j = 0; j < originalPoses; j++) {
            if (originalPoses >= mappedPoses && j < mappedPoses) shuffledPoses.add(j);
            else shuffledPoses.add(getRandom().nextInt(mappedPoses));
        }
       // System.out.println("originalPoses = " + originalPoses + "; mappedPoses = " + mappedPoses);
        Collections.shuffle(shuffledPoses, getRandom());

        for (int j = 0; j < originalPoses; j++) {
            List<Entry> sortedPoses1 = getEntriesByPose(original, j);
            List<Entry> sortedPoses2 = getEntriesByPose(mapped, shuffledPoses.get(j));
            Collections.sort(sortedPoses1);
            Collections.sort(sortedPoses2);

            int originalSizes = getSizesCount(sortedPoses1);
            int mappedSizes = getSizesCount(sortedPoses2);
            if (originalSizes != mappedSizes) {
                for (int k = 0; k < Math.abs(originalSizes - mappedSizes); k++) {
                    if (originalSizes < mappedSizes)
                        sortedPoses1.addAll(getEntriesBySize(getEntriesByPose(original, 0),
                                getRandom().nextInt(originalSizes)));
                    else sortedPoses2.addAll(getEntriesBySize(getEntriesByPose(mapped, 0),
                            getRandom().nextInt(mappedSizes)));
                }
            }

            shuffled.put(sortedPoses1, sortedPoses2);
        }
    }
    private int getSkinsCount(List<Entry> original) {
        return getCodeCount(original, SKIN_CODE_POSITION);
    }
    private int getPosesCount(List<Entry> original) {
        return getCodeCount(original, POSE_CODE_POSITION);
    }
    private int getSizesCount(List<Entry> original) {
        return getCodeCount(original, SIZE_CODE_POSITION);
    }
    private List<Entry> getEntriesBySkin(List<Entry> original, int skin) {
        return getEntriesByCode(original, skin, SKIN_CODE_POSITION);
    }
    private List<Entry> getEntriesBySize(List<Entry> original, int size) {
        return getEntriesByCode(original, size, SIZE_CODE_POSITION);
    }
    private List<Entry> getEntriesByPose(List<Entry> original, int pose) {
        return getEntriesByCode(original, pose, POSE_CODE_POSITION);
    }
    private List<Entry> getEntriesByCode(List<Entry> original, int code, int index) {
        List<Entry> result = new ArrayList<>();
        //Collections.sort(original);
        int i = 0;
        Map<Character, Integer> map = new HashMap<>();
        for (Entry entry : original) {
            if (!map.containsKey(entry.getFileName().charAt(index))) {
                map.put(entry.getFileName().charAt(index), i++);
            }
        }
        for (Entry entry : original) {
            if (code == map.get(entry.getFileName().charAt(index)))
                result.add(entry);
        }
        return result;
    }
    private int getCodeCount(List<Entry> original, int index) {
        Set<Character> set = new HashSet<>();
        for (Entry entry : original) {
            char skinCode = entry.getFileName().charAt(index);
            set.add(skinCode);
        }
        return set.size();
    }
}
