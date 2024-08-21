package artlighter;

import artlighter.model.RandomizerService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Controller {

    public final static String BACKUP_FILE_SUFFIX = "_backup";
    private final static String HELP = """
            Usage: vn-randomizer [flags...]
            Without flags (excluding --lowmemory) it randomizes characters, soundtrack and backgrounds
            Flags:
            -C, --char            Randomize characters
            -m, --music           Randomize soundtrack
            -b, --bg              Randomize backgrounds
            -c, --cg              Randomize CGs
            -s, --sound           Randomize sound effects
            -v, --voice           Randomize voice lines
            --lowmemory           Run on low memory mode (if you get OutOfMemoryError)
            --restore             Restore default resource files""";
    private static final Map<RandomizerService.NovelType, String[]> pathMap = new HashMap<>();
    //String[0] - Character file
    //String[1] - Soundtrack file
    //String[2] - Background file
    //String[3] - CGs file
    //String[4] - SFX file
    //String[5] - Voicelines file
    static {
        String[] magesPaths = new String[]{"USRDIR/chara.mpk", "USRDIR/bgm.mpk",
                                            "USRDIR/bg.mpk", "USRDIR/bg.mpk",
                                            "USRDIR/se.mpk", "USRDIR/voice.mpk"};
        pathMap.put(RandomizerService.NovelType.STEINSGATE, magesPaths);

        String[] magesPaths2 = new String[]{"USRDIR/chara.mpk", "USRDIR/bgm.mpk",
                "USRDIR/bg1.mpk", "USRDIR/bg2.mpk",
                "USRDIR/se.mpk", "USRDIR/voice.mpk"};
        pathMap.put(RandomizerService.NovelType.CHAOSCHILD, magesPaths2);
    }

    public void randomize(RandomizerService.NovelType type, boolean[] options, boolean lowMemoryMode) {
        RandomizerService service = new RandomizerService(lowMemoryMode);
        String[] paths = pathMap.get(type);
        int randomized = 0;
        boolean[] checks = new boolean[6];
        for (int i = 0; i < checks.length; i++) {
            if (i < options.length) checks[i] = options[i];
            else checks[i] = false;
        }
        System.out.println("------CREATING BACKUPS------");
        String[] sourcePaths = saveBackups(type, checks);
        for (int i = 0; i < sourcePaths.length; i++) {
            if (!Files.exists(Paths.get(sourcePaths[i]))) sourcePaths[i] = paths[i];
        }

        if (checks[0]) randomized += service.randomizeCharacters(sourcePaths[0], paths[0], type);
        if (checks[1]) randomized += service.randomizeSoundtrack(paths[1], paths[1], type);
        if (checks[2]) randomized += service.randomizeBackgrounds(paths[2], paths[2], type);
        if (checks[3]) randomized += service.randomizeCGs(paths[3], paths[3], type);
        if (checks[4]) randomized += service.randomizeSoundEffects(paths[4], paths[4], type);
        if (checks[5]) randomized += service.randomizeVoiceLines(paths[5], paths[5], type);

        System.out.println("------TOTAL: " + randomized + " RANDOMIZED FILES------");
    }

    public String[] saveBackups(RandomizerService.NovelType type, boolean[] checks) {
        String[] filenames = pathMap.get(type);
        String[] copies = new String[filenames.length];
        boolean allCreated = true;
        for (int i = 0; i < filenames.length; i++) {
            String path = filenames[i];
            int pointIndex = path.indexOf(".");
            String extension = path.substring(pointIndex);
            copies[i] = path.substring(0, pointIndex) + BACKUP_FILE_SUFFIX + extension;
            if (checks[i] && !Files.exists(Paths.get(copies[i]))) {
                allCreated = false;
                try {
                    Files.copy(Paths.get(path),
                            Paths.get(copies[i]));
                    System.out.println("Backup for " + path + " has been created");
                } catch (FileNotFoundException ex) {
                    System.out.println("ERROR: File " + path + " not found");
                } catch (IOException ex) {
                    System.out.println("ERROR: Couldn't create backup for " + path + "\n" + ex.getMessage());
                }
            }
        }
        if (allCreated) System.out.println("All backups have been already created");
        return copies;
    }

    public void restoreBackups(RandomizerService.NovelType type) {
        String[] filenames = pathMap.get(type);
        String[] copies = new String[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            String path = filenames[i];
            int pointIndex = path.indexOf(".");
            String extension = path.substring(pointIndex);
            copies[i] = path.substring(0, pointIndex) + BACKUP_FILE_SUFFIX + extension;
            try {
                Files.copy(Paths.get(copies[i]), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("Couldn't restore " + path + " (If you didn't randomize it then ignore this)");
            }
        }
        for (String path : copies) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) {
        Set<String> flags = new HashSet<>(Arrays.asList(args));
        Controller controller = new Controller();

        if (flags.contains("--help")) {
            System.out.println(HELP);
            return;
        }

        RandomizerService.NovelType type;
        if (Files.exists(Paths.get(pathMap.get(RandomizerService.NovelType.CHAOSCHILD)[2])))
            type = RandomizerService.NovelType.CHAOSCHILD;
        else type = RandomizerService.NovelType.STEINSGATE;
        if (flags.contains("--restore")) {
            System.out.println("------RESTORING BACKUPS------");
            controller.restoreBackups(type);
            return;
        }

        boolean[] options = new boolean[6];
        boolean lowMemoryMode = flags.contains("--lowmemory");
        if (args.length == 0 || (args.length == 1 && lowMemoryMode)) {
            options[0] = true;
            options[1] = true;
            options[2] = true;
        } else {
            if (flags.contains("-C") || flags.contains("--char")) options[0] = true;
            if (flags.contains("-m") || flags.contains("--music")) options[1] = true;
            if (flags.contains("-b") || flags.contains("--bg")) options[2] = true;
            if (flags.contains("-c") || flags.contains("--cg")) options[3] = true;
            if (flags.contains("-s") || flags.contains("--sound")) options[4] = true;
            if (flags.contains("-v") || flags.contains("--voice")) options[5] = true;
        }

        controller.randomize(type, options, lowMemoryMode);
        /*long start = System.currentTimeMillis();

        MpkRepacker repacker = new MpkRepacker();
        repacker.setLowMemoryMode(false);


        MagesRandomizerFactory factory = new MagesRandomizerFactory();

        Entry[] entries = repacker.getFileData("USRDIR/SG0/chara.mpk");
        factory.getCharacterRandomizer().randomize(entries);
        repacker.writeMappedFiles("USRDIR/SG0/chara_r.mpk", entries);*/

        /*Entry[] entries = repacker.getFileData("USRDIR/SG0/bgm.mpk");
        factory.getSoundtrackRandomizer().randomize(entries);
        repacker.writeMappedFiles("USRDIR/SG0/bgm.mpk", entries);*/

       /* Entry[] entries = repacker.getFileData("USRDIR/SG0/bg.mpk");
        factory.getBackgroundRandomizer().randomize(entries);
        repacker.writeMappedFiles("USRDIR/SG0/bg.mpk", entries);*/


       // System.out.println("ELAPSED:" + (System.currentTimeMillis() - start) + " ms");


        /* for (Entry entry : entries) {
            if (entry.isCompressed()) repacker.inflateLayFile(entry);
            // System.out.println(entry.getFileName());
            try (FileOutputStream fos = new FileOutputStream("USRDIR/CC/chara_r_unpacked/" + entry.getFileName())) {
                List<byte[]> list = entry.getRawData();
               // if (entry.getFileName().endsWith(".lay")) for (byte b : list.get(0)) System.out.print(b + " ");
               // System.out.println();
                for (int i = 0; i < list.size(); i++) {
                    fos.write(list.get(i));
                }
            }
        }*/
    }
}
