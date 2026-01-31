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
    private static boolean includeNumber = false;
    private static final int WORDS_IN_FRAME = 7;
    private static final int TWO_SECONDS = 2000;
    private static final int MIN_WORDS = 2;
    private static final int MAX_WORDS = 6;
    private static final int DEFAULT_WORDS = 3;
    private static final Color DARK_GREEN = new Color(0, 128, 0);
    private static final int MIN_WORD_LENGTH = 4;
    private static final int MAX_WORD_LENGTH = 6;
    private static final int PASSWORD_LIST_FONT_SIZE = 14;
    private static final int DOUBLE_DIGIT_RANDOM_LIMIT = 100;
    private static final int STATUS_LABEL_HORIZONTAL_BORDER = 5;
    private static final int STATUS_LABEL_VERTICAL_BORDER = 10;
    private static final int CONTROL_PANEL_BORDER = 10;
    private static int currentWordCount = DEFAULT_WORDS;

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
        passwordList.setFont(new Font("Monospaced", Font.PLAIN, PASSWORD_LIST_FONT_SIZE));
        passwordList.setVisibleRowCount(WORDS_IN_FRAME);

        JScrollPane listScrollPane = new JScrollPane(passwordList);
        listScrollPane.setPreferredSize(new Dimension(520, 240));

        // Initialise the Slider (The Control Components)
        JSlider wordSlider = new JSlider(MIN_WORDS, MAX_WORDS, DEFAULT_WORDS);
        wordSlider.setMajorTickSpacing(1);
        wordSlider.setPaintTicks(true);
        wordSlider.setPaintLabels(true);
        wordSlider.addChangeListener(e -> {
            currentWordCount = wordSlider.getValue();
            refreshList(listModel);
        });

        // Initialise the Checkbox
        JCheckBox numBox = new JCheckBox("Append random number (0-99)");
        numBox.addActionListener(e -> {
            includeNumber = numBox.isSelected();
            refreshList(listModel);
        });

        // Build a nicer "form-like" control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(
                        CONTROL_PANEL_BORDER,
                        CONTROL_PANEL_BORDER,
                        CONTROL_PANEL_BORDER,
                        CONTROL_PANEL_BORDER
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(4, 0, 4, 0);

        controlPanel.add(new JLabel("Number of words:"), gbc);

        gbc.gridy++;
        controlPanel.add(wordSlider, gbc);

        gbc.gridy++;
        controlPanel.add(numBox, gbc);

        // Buttons Panel (right-aligned looks more "native")
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton copyBtn = new JButton("Copy Selected");
        JButton refreshBtn = new JButton("Refresh");
        buttonPanel.add(refreshBtn);
        buttonPanel.add(copyBtn);

        // Status label (left side, like a status bar)
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(
                STATUS_LABEL_VERTICAL_BORDER,
                STATUS_LABEL_HORIZONTAL_BORDER,
                STATUS_LABEL_VERTICAL_BORDER,
                STATUS_LABEL_HORIZONTAL_BORDER
        ));
        statusLabel.setForeground(DARK_GREEN);

        Timer clearTimer = new Timer(TWO_SECONDS, e -> statusLabel.setText(" "));
        clearTimer.setRepeats(false);

        copyBtn.addActionListener(e -> {
            String selected = passwordList.getSelectedValue();
            if (selected != null) {
                copyToClipboard(selected);
                statusLabel.setText("✓ Password copied to clipboard!");
                clearTimer.restart();
            }
        });
        refreshBtn.addActionListener(e -> refreshList(listModel));

        // Bottom "status bar": status on the left, buttons on the right
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        bottomBar.add(statusLabel, BorderLayout.CENTER);
        bottomBar.add(buttonPanel, BorderLayout.EAST);

        // Final Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(listScrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
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

        return includeNumber ? base + "-" + (RAND.nextInt(DOUBLE_DIGIT_RANDOM_LIMIT)) : base;
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
                    .filter(w -> w.length() >= MIN_WORD_LENGTH && w.length() <= MAX_WORD_LENGTH)
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