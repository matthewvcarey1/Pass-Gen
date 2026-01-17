package uk.co.puce4.util.password.passgen;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PassGen {
    static List<String> cachedWords = null;
    private static final SecureRandom RAND = new SecureRandom();
    private static int currentWordCount = 3;
    private static boolean includeNumber = false; // Checkbox state

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            loadDictionary();
            showEnhancedGenerator();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void showEnhancedGenerator() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        refreshList(listModel);

        JList<String> passwordList = new JList<>(listModel);
        passwordList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Slider for word count
        JSlider wordSlider = new JSlider(2, 6, 3);
        wordSlider.setMajorTickSpacing(1);
        wordSlider.setPaintTicks(true);
        wordSlider.setPaintLabels(true);
        wordSlider.addChangeListener(_ -> {
            currentWordCount = wordSlider.getValue();
            refreshList(listModel);
        });

        // Checkbox for numbers
        JCheckBox numBox = new JCheckBox("Append random number (0-9)");
        numBox.addActionListener(_ -> {
            includeNumber = numBox.isSelected();
            refreshList(listModel);
        });

        // Build the Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(new JLabel("Number of Words:"));
        controlPanel.add(wordSlider);
        controlPanel.add(numBox);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(passwordList), BorderLayout.CENTER);

        Object[] options = {"Copy Selected", "Refresh", "Exit"};

        while (true) {
            int result = JOptionPane.showOptionDialog(null, mainPanel, "Password Generator",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (result == JOptionPane.YES_OPTION) {
                if (passwordList.getSelectedValue() != null) {
                    copyToClipboard(passwordList.getSelectedValue());
                    break;
                }
            } else if (result == JOptionPane.NO_OPTION) {
                refreshList(listModel);
            } else {
                break;
            }
        }
    }

    private static void refreshList(DefaultListModel<String> model) {
        model.clear();
        for (int i = 0; i < 7; i++) {
            model.addElement(generatePassword(currentWordCount));
        }
    }

    public static String generatePassword(int count) {
        String base = IntStream.range(0, count)
                .mapToObj(_ -> cachedWords.get(RAND.nextInt(cachedWords.size())))
                .collect(Collectors.joining("-")); //.toLowerCase();

        return includeNumber ? base + "-" + (RAND.nextInt(10)) : base;
    }

    // (Dictionary loading and Clipboard methods remain the same as previous)
    private static void loadDictionary() throws Exception {
        // 1. Get the resource reference first
        InputStream is = PassGen.class.getResourceAsStream("/words.txt");

        // 2. Check it immediately
        if (is == null) {
            throw new IOException("Dictionary file 'words.txt' not found in classpath resources!");
        }

        // 3. Now safely use it in try-with-resources
        try (is; // Valid in Java 9+, otherwise use: try (InputStream autoCloseIs = is;
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            cachedWords = reader.lines()
                    .map(String::trim)
                    .filter(w -> !w.isEmpty())
                    .filter(w -> w.length() >= 4 && w.length() <= 6)
                    .filter(w -> w.matches("^[a-z|A-Z]+$"))
                    .collect(Collectors.toList());
        }

        if (cachedWords.isEmpty()) {
            throw new IllegalStateException("Dictionary loaded but no words matched the 4-6 character filter.");
        }
    }

    private static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }
}