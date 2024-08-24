package artlighter;

import artlighter.model.RandomizerService;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    private View view;
    public final static String BACKUP_FILE_SUFFIX = "_backup";

    public Controller(View view) {
        this.view = view;
        view.init();
    }

    public void randomize(RandomizerService.NovelType type, boolean[] options, boolean lowMemoryMode, Path gamePath) {
        RandomizerService service = new RandomizerService(lowMemoryMode);
        String[] paths = getFullPaths(gamePath, type);
        int randomized = 0;
        boolean[] checks = new boolean[6];
        for (int i = 0; i < checks.length; i++) {
            if (i < options.length) checks[i] = options[i];
            else checks[i] = false;
        }
        System.out.println("------CREATING BACKUPS------");
        String[] sourcePaths = saveBackups(type, gamePath, checks);
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
    private String[] getFullPaths(Path gamePath, RandomizerService.NovelType type) {
        String[] paths = type.getFilenames();
        String[] result = new String[paths.length];
        for (int i = 0; i < paths.length; i++) result[i] = gamePath.resolve(Paths.get(paths[i])).toString();
        return result;
    }
    public String[] saveBackups(RandomizerService.NovelType type, Path gamePath, boolean[] checks) {
        String[] filenames = getFullPaths(gamePath, type);
        String[] copies = new String[filenames.length];
        boolean allCreated = true;
        for (int i = 0; i < filenames.length; i++) {
            String path = filenames[i];
            int pointIndex = path.lastIndexOf(".");
            String extension = path.substring(pointIndex);
            copies[i] = path.substring(0, pointIndex) + BACKUP_FILE_SUFFIX + extension;
            if (checks[i] && !Files.exists(Paths.get(copies[i]))) {
                allCreated = false;
                try {
                    Files.copy(Paths.get(path), Paths.get(copies[i]));
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
    public void actionPerformed(Action action) {
        RandomizerService.NovelType type = view.getNovelType();
        Path gamePath = view.getGamePath();
        if (action == Action.RANDOMIZE) {
            boolean[] options = view.getOptions();
            boolean lowMemoryMode = view.isLowMemoryMode();
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                view.enableButtons(false);
                randomize(type, options, lowMemoryMode, gamePath);
                view.enableButtons(true);
            });
        } else if (action == Action.RESTORE) {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                view.enableButtons(false);
                restoreBackups(gamePath, type);
                view.enableButtons(true);
            });
        }
    }
    public void restoreBackups(Path gamePath, RandomizerService.NovelType type) {
        System.out.println("------RESTORING BACKUPS------");
        String[] filenames = getFullPaths(gamePath, type);
        String[] copies = new String[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            String path = filenames[i];
            int pointIndex = path.lastIndexOf(".");
            String extension = path.substring(pointIndex);
            copies[i] = path.substring(0, pointIndex) + BACKUP_FILE_SUFFIX + extension;
            try {
                Files.copy(Paths.get(copies[i]), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.out.println("Seems like " + path + " is already in its original state");
            }
        }
        for (String path : copies) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException ignored) {}
        }
        System.out.println("------RESTORED------");
    }

    public static void main(String[] args) {
        //Set<String> flags = new HashSet<>(Arrays.asList(args));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        View view = new View();
        Controller controller = new Controller(view);
        view.setController(controller);

        /*if (flags.contains("--help")) {
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
        }*/

        //controller.randomize(type, options, lowMemoryMode);

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
