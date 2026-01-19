package uk.co.puce4.util.password.passgen;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PassGen {
    static List<String> cachedWords = null;
    private static final SecureRandom RAND = new SecureRandom();
    private static int currentWordCount = 3;
    private static boolean includeNumber = false;
    private static final int WORDS_IN_FRAME = 7;

    public static void main(String[] args) {
        // Run UI on the Event Dispatch Thread (standard Swing practice)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                loadDictionary();
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Password Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the program icon for MS Windows
        URL iconURL = PassGen.class.getResource("/security-high.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            frame.setIconImage(icon.getImage());
        } else {
            System.err.println("Could not find the icon file!");
        }

        // Setup the List and Model
        DefaultListModel<String> listModel = new DefaultListModel<>();
        refreshList(listModel);
        JList<String> passwordList = new JList<>(listModel);
        passwordList.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Initialise the Slider (The Control Components)
        JSlider wordSlider = new JSlider(2, 6, 3);
        wordSlider.setMajorTickSpacing(1);
        wordSlider.setPaintTicks(true);
        wordSlider.setPaintLabels(true);
        wordSlider.addChangeListener(e -> {
            currentWordCount = wordSlider.getValue();
            refreshList(listModel);
        });

        // Initialise the Checkbox
        JCheckBox numBox = new JCheckBox("Append random number (0-9)");
        numBox.addActionListener(e -> {
            includeNumber = numBox.isSelected();
            refreshList(listModel);
        });

        //  Build the controlPanel (This was missing in the snippet!)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(new JLabel("Number of Words:"));
        controlPanel.add(wordSlider);
        controlPanel.add(numBox);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //  Buttons Panel (Standard GUI buttons)
        JPanel buttonPanel = new JPanel();
        JButton copyBtn = new JButton("Copy Selected");
        JButton refreshBtn = new JButton("Refresh");

        copyBtn.addActionListener(e -> {
            if (passwordList.getSelectedValue() != null) {
                copyToClipboard(passwordList.getSelectedValue());
            }
        });
        refreshBtn.addActionListener(e -> refreshList(listModel));

        buttonPanel.add(copyBtn);
        buttonPanel.add(refreshBtn);

        // Create the Status Label
        JLabel statusLabel = new JLabel(" "); // Space keeps the height consistent
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setForeground(new Color(0, 128, 0)); // Dark green for success

        // 7. Setup a Timer to clear the status after 2 seconds
        Timer clearTimer = new Timer(2000, e -> statusLabel.setText(" "));
        clearTimer.setRepeats(false);

        // Update the Copy Button Logic
        copyBtn.addActionListener(e -> {
            String selected = passwordList.getSelectedValue();
            if (selected != null) {
                copyToClipboard(selected);
                statusLabel.setText("✓ Password copied to clipboard!");
                clearTimer.restart(); // Reset the 2-second countdown
            }
        });

        // Group Buttons and Status in a bottom panel
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(buttonPanel, BorderLayout.CENTER);
        bottomContainer.add(statusLabel, BorderLayout.SOUTH);

        // Final Layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(passwordList), BorderLayout.CENTER);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private static void refreshList(DefaultListModel<String> model) {
        model.clear();
        for (int i = 0; i < WORDS_IN_FRAME; i++) {
            model.addElement(generatePassword(currentWordCount));
        }
    }

    public static String generatePassword(int count) {
        String base = IntStream.range(0, count)
                .mapToObj(i -> cachedWords.get(RAND.nextInt(cachedWords.size())))
                .collect(Collectors.joining("-")); //.toLowerCase();

        return includeNumber ? base + "-" + (RAND.nextInt(10)) : base;
    }

    // (Dictionary loading and Clipboard methods remain the same as previous)
    public static void loadDictionary() throws Exception {
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