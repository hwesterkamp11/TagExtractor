import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TagExtractor extends JFrame {
    private JTextArea resultArea;
    private JButton openFileButton;
    private JButton openStopWordsButton;
    private JButton saveResultsButton;
    private JButton quitButton;
    private JLabel fileLabel;
    private JLabel stopWordsLabel;
    private File selectedFile;
    private File stopWordsFile;
    private Map<String, Integer> wordFrequencyMap;
    private Set<String> stopWords;

    public TagExtractor() {
        System.out.println("TagExtractor() started");
        resultArea = new JTextArea();
        openFileButton = new JButton("Select File to Filter");
        openStopWordsButton = new JButton("Select Stop Words");
        saveResultsButton = new JButton("Save Results");
        quitButton = new JButton("Quit");
        saveResultsButton.setEnabled(false);
        fileLabel = new JLabel("No file selected");
        stopWordsLabel = new JLabel("No stop word filter selected.");

        wordFrequencyMap = new HashMap<>();
        stopWords = new HashSet<>();

        setupGUI();
        setupEventHandlers();
        System.out.println("TagExtractor() finished!");
    }

    private void setupGUI() {
        System.out.println("SetupGUI() started");
        setTitle("Tag Extractor");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        System.out.println("End of first block");

        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        saveResultsButton.setEnabled(false);
        System.out.println("End of second block");

        JPanel controlPanel = new JPanel(new GridLayout(2, 2));
        System.out.println("Control panel created");
        controlPanel.add(openFileButton);
        System.out.println("Added openFileButton");
        controlPanel.add(fileLabel);
        System.out.println("Added fileLabel button");
        controlPanel.add(openStopWordsButton);
        System.out.println("Added openStopWordsButton");
        controlPanel.add(stopWordsLabel);
        System.out.println("End of third block");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveResultsButton);
        buttonPanel.add(quitButton);
        System.out.println("End of fourth block");

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        System.out.println("SetupGUI() completed!");
    }

    private void setupEventHandlers() {
        System.out.println("setupEventHandlers() started");
        openFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                fileLabel.setText("Selected: " + selectedFile.getName());
                processFiles();
            }
        });

        openStopWordsButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                stopWordsFile = fileChooser.getSelectedFile();
                stopWordsLabel.setText("Selected: " + stopWordsFile.getName());
                loadStopWords();
                processFiles();
            }
        });

        saveResultsButton.addActionListener(e -> {
            if(wordFrequencyMap.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No results to save!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Tag Results");
            int userSelection = fileChooser.showSaveDialog(null);
            if(userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try(PrintWriter printWriter = new PrintWriter(fileToSave)) {
                    printWriter.println("Tag Extraction Results");
                    resultArea.append("Source File: " + selectedFile.getName() + "\n");
                    resultArea.append("Stop Words File: " + (stopWordsFile != null ? stopWordsFile.getName() : "None") + "\n");
                    resultArea.append("----------------------------------------");

                    List<Map.Entry<String, Integer>> sortedEntries = wordFrequencyMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toList());

                    for(Map.Entry<String, Integer> entry : sortedEntries) {
                        printWriter.printf("%-20s %d%n", entry.getKey(), entry.getValue());
                    }
                    JOptionPane.showMessageDialog(null, "Results saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        quitButton.addActionListener(e -> {
            System.out.println("Quit button engaged");
            System.exit(0);
        });
        System.out.println("setupEventHandlers() finished!");
    }
    private void loadStopWords() {
        System.out.println("loadStopWords() started");
        if(stopWordsFile == null || !stopWordsFile.exists()) {
            return;
        }
        stopWords.clear();
        try(BufferedReader reader = new BufferedReader(new FileReader(stopWordsFile))) {
            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if(!line.isEmpty()) {
                    stopWords.add(line);
                }
            }
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading stop words file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("loadStopWords() completed");
    }

    private void processFiles() {
        System.out.println("processFiles() started");
        if(selectedFile == null || !selectedFile.exists()) {
            return;
        }
        wordFrequencyMap.clear();
        try(BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            while((line = reader.readLine()) != null) {
                processLine(line);
            }
            displayResults();
            saveResultsButton.setEnabled(true);
        }catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading text file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("processFiles() finished");
    }

    private void processLine(String line) {
        System.out.println("processLine(String line) started");
        String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        for(String word : words) {
            word = word.trim();
            if(word.isEmpty()) {
                continue;
            }
            if(!stopWords.contains(word)) {
                wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word,0) + 1);
            }
            System.out.println("processLine(String line) completed");
        }
    }

    private void displayResults() {
        System.out.println("displayResults() started");
        resultArea.setText("");
        resultArea.append("Tag Extraction Results\n");
        resultArea.append("Source File: " + selectedFile.getName() + "\n");
        resultArea.append("Stop Words File: " + (stopWordsFile != null ? stopWordsFile.getName() : "None") + "\n");
        resultArea.append("----------------------------------------\n\n");

        List<Map.Entry<String, Integer>> sortedEntries = wordFrequencyMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toList());
        for(Map.Entry<String, Integer> entry : sortedEntries) {
            resultArea.append(String.format("%-20s %d%n", entry.getKey(), entry.getValue()));
        }
        System.out.println("displayResults() completed");
    }

    public static void main(String[] args) {
        System.out.println("Program started");
        SwingUtilities.invokeLater(() -> {
            TagExtractor tagExtractor = new TagExtractor();
            tagExtractor.setLocationRelativeTo(null);
            tagExtractor.setVisible(true);
        });
        System.out.println("TagExtractor() should start running");
    }
}