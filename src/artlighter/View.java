package artlighter;

import artlighter.model.RandomizerService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class View implements ActionListener, ItemListener {
    private final static int WIDTH = 500;
    private final static int HEIGHT = 600;
    private JFrame frame;
    private JButton randomizeButton, restoreButton, openDirectory;
    private boolean[] options = new boolean[6];
    private JCheckBox[] checkBoxes = new JCheckBox[options.length];
    private boolean lowMemoryMode = false;
    private JCheckBox lowMemoryModeCheckBox;
    private JFileChooser fileChooser;
    private JTextField pathField;
    private JTextArea logArea;
    private Controller controller;
    private Path gamePath;
    private Path savedOptionsPath = Paths.get("saved.properties");
    private Properties properties = new Properties();


    void init() {
        loadProperties();

        randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(this);
        randomizeButton.setBounds(new Rectangle(new Point(WIDTH/2 - randomizeButton.getPreferredSize().width/2, 457),
                randomizeButton.getPreferredSize()));
        restoreButton = new JButton("Restore backups");
        restoreButton.setBounds(new Rectangle(new Point(WIDTH/2 - restoreButton.getPreferredSize().width/2, 509),
                restoreButton.getPreferredSize()));
        restoreButton.addActionListener(this);
        openDirectory = new JButton("Open VN directory");
        openDirectory.addActionListener(this);

        checkBoxes[0] = new JCheckBox("Randomize characters");
        checkBoxes[1] = new JCheckBox("Randomize soundtrack");
        checkBoxes[2] = new JCheckBox("Randomize backgrounds");
        checkBoxes[3] = new JCheckBox("Randomize CGs");
        checkBoxes[4] = new JCheckBox("Randomize sound effects");
        checkBoxes[5] = new JCheckBox("Randomize voice lines");
        for (int i = 0; i < options.length; i++) {
            String defaultValue = "false";
            if (i < 3) defaultValue = "true";
            options[i] = Boolean.parseBoolean(properties.getProperty("options" + i, defaultValue));
            checkBoxes[i].setSelected(options[i]);
        }

        lowMemoryModeCheckBox = new JCheckBox("Enable low memory mode (check this if you have less than 4GB RAM)");
        lowMemoryMode = Boolean.parseBoolean(properties.getProperty("lowmemory", "false"));
        lowMemoryModeCheckBox.setSelected(lowMemoryMode);

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String savedPath = properties.getProperty("path", "");
        if (Files.exists(Paths.get(savedPath)) && Files.isDirectory(Paths.get(savedPath)))
            fileChooser.setSelectedFile(new File(savedPath));
        gamePath = fileChooser.getSelectedFile().toPath();
        pathField = new JTextField();
        pathField.setText(gamePath.toString());
        pathField.addActionListener(this);

        JPanel fileManager = new JPanel();
        fileManager.setBorder(new CompoundBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0)),
                fileManager.getBorder()));
        fileManager.setLayout(new BoxLayout(fileManager, BoxLayout.X_AXIS));
        //openDirectory.setSize(100, 40);
        pathField.setPreferredSize(new Dimension(WIDTH/2, 22));

        fileManager.add(pathField);
        fileManager.add(openDirectory);

        fileManager.setBounds(new Rectangle(new Point(WIDTH/2 - fileManager.getPreferredSize().width/2, 22),
                fileManager.getPreferredSize()));

        JScrollPane scrollPane = new JScrollPane();
        logArea = new JTextArea();
        logArea.setEditable(false);
        scrollPane.setPreferredSize(new Dimension(fileManager.getWidth(), 22));
        scrollPane.setViewportView(logArea);

        System.setOut(new PrintStream(new JTextAreaOutputStream()));

        frame = new JFrame("VN Randomizer");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(null);
       // frame.add(fileChooser);
       // frame.add(openDirectory);
        frame.add(fileManager);
        //frame.add(Box.createRigidArea(new Dimension(WIDTH, HEIGHT/2)));
        frame.add(scrollPane);
        scrollPane.setBounds(WIDTH/2 - scrollPane.getPreferredSize().width/2, 62,
                scrollPane.getPreferredSize().width, 165);

        int YPosition = 257;
        int XPosition = WIDTH/2 - checkBoxes[0].getPreferredSize().width/2;
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.addItemListener(this);
            checkBox.setBounds(new Rectangle(new Point(XPosition, YPosition), checkBox.getPreferredSize()));
            YPosition += 25;
            frame.add(checkBox);
        }
        lowMemoryModeCheckBox.setBounds(new Rectangle(new Point(WIDTH/2 - lowMemoryModeCheckBox.getPreferredSize().width/2,
                YPosition + 25), lowMemoryModeCheckBox.getPreferredSize()));

        frame.add(lowMemoryModeCheckBox);
        frame.add(randomizeButton);
        frame.add(restoreButton);

        frame.setVisible(true);
    }
    private void loadProperties() {
        if (!Files.exists(savedOptionsPath)) return;
        try (InputStream fis = Files.newInputStream(savedOptionsPath)) {
            properties.load(fis);
        } catch (IOException ex) {
            System.out.println("Failed to load saved options");
        }
    }
    private void saveProperties() {
        try (OutputStream fos = Files.newOutputStream(savedOptionsPath)) {
            properties.store(fos, "");
        } catch (IOException ex) {
            System.out.println("Failed to save properties. Your options won't be remembered");
        }
    }
    public void log(String message) {
        logArea.append(message);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
    public boolean[] getOptions() {
        return options;
    }

    public Path getGamePath() {
        return gamePath;
    }
    //Temporary
    public RandomizerService.NovelType getNovelType() {
        //System.out.println("Getting type " + gamePath.resolve(Paths.get(RandomizerService.NovelType.CHAOSCHILD.getFilenames()[2])));
        if (Files.exists(gamePath.resolve(Paths.get(RandomizerService.NovelType.CHAOSCHILD.getFilenames()[2]))))
            return RandomizerService.NovelType.CHAOSCHILD;
        return RandomizerService.NovelType.STEINSGATE;
    }
    public void enableButtons(boolean state) {
        openDirectory.setEnabled(state);
        randomizeButton.setEnabled(state);
        restoreButton.setEnabled(state);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Path newPath = Paths.get(pathField.getText());
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            gamePath = Paths.get(pathField.getText());
            properties.setProperty("path", gamePath.toString());
        }
        else pathField.setText(gamePath.toString());

        if (e.getSource() == randomizeButton) {
            saveProperties();
            controller.actionPerformed(Action.RANDOMIZE);
        }
        else if (e.getSource() == restoreButton) controller.actionPerformed(Action.RESTORE);
        else if (e.getSource() == openDirectory) {
            int result = fileChooser.showOpenDialog(frame.getContentPane());
            if (result == JFileChooser.APPROVE_OPTION) {
                gamePath = fileChooser.getSelectedFile().toPath();
                //System.out.println(gamePath.toString());
                pathField.setText(gamePath.toString());
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getItemSelectable() == lowMemoryModeCheckBox) {
            lowMemoryMode = e.getStateChange() == ItemEvent.SELECTED ? true : false;
            properties.setProperty("lowmemory", Boolean.toString(lowMemoryMode));
            return;
        }
        for (int i = 0; i < checkBoxes.length; i++) {
            if (e.getItemSelectable() == checkBoxes[i]) {
                options[i] = e.getStateChange() == ItemEvent.SELECTED ? true : false;
                properties.setProperty("options" + i, Boolean.toString(options[i]));
            }
        }
    }

    public boolean isLowMemoryMode() {
        return lowMemoryMode;
    }

    private class JTextAreaOutputStream extends OutputStream {

        @Override
        public void write(byte[] buffer, int offset, int length) throws IOException {
            String text = new String(buffer, offset, length);
            SwingUtilities.invokeLater(() -> View.this.log(text));
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }
    }

}
