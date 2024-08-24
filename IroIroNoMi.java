import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import java.io.InputStreamReader;

public class IroIroNoMi extends JFrame {
    private JButton loadImageButton;
    private JButton generateColorsButton;
    private JLabel imageLabel;
    private JFileChooser fileChooser;
    private JPanel colorPanel;
    private JTextArea outputArea;
    private File selectedImageFile;
    private int nColors = 5;  // Number of colors to extract

    public IroIroNoMi() {
        // Set up the frame
        setTitle("Iro Iro no Mi - Color Palette Generator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(Color.BLACK);

        // Create components
        loadImageButton = new JButton("Load Image");
        generateColorsButton = new JButton("Generate Colors");
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER); // Center the image
        fileChooser = new JFileChooser();
        colorPanel = new JPanel(); // Panel to display color palette
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.WHITE);

        // Set up panel for buttons
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.add(loadImageButton);
        panel.add(generateColorsButton); // Add generate button
        add(panel, BorderLayout.NORTH);

        // Add components to the frame
        JPanel imagePanel = new JPanel();
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Frame for image
        imagePanel.setBackground(Color.BLACK);
        imagePanel.add(imageLabel);
        add(imagePanel, BorderLayout.CENTER);

        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS)); // Vertical strips
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Frame for colors
        colorPanel.setBackground(Color.BLACK);
        add(colorPanel, BorderLayout.EAST);

        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        // Add action listener for the load image button
        loadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    outputArea.setText("Selected file: " + selectedImageFile.getAbsolutePath());
                    displayImage(selectedImageFile);
                    outputArea.setText("Image loaded. Click 'Generate Colors' to extract colors.");
                }
            }
        });

        // Add action listener for the generate colors button
        generateColorsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedImageFile != null) {
                    String outputPath = "colors.json";  // Path to save the color JSON file
                    runPythonScript(selectedImageFile.getAbsolutePath(), nColors, outputPath);
                } else {
                    outputArea.setText("Please load an image first.");
                }
            }
        });
    }

    private void displayImage(File imageFile) {
        try {
            // Load image and set it to the JLabel
            Image image = ImageIO.read(imageFile);
            ImageIcon imageIcon = new ImageIcon(image.getScaledInstance(600, -1, Image.SCALE_SMOOTH));
            imageLabel.setIcon(imageIcon);
        } catch (IOException e) {
            e.printStackTrace();
            outputArea.setText("Error loading image.");
        }
    }

    private void runPythonScript(String imagePath, int nColors, String outputPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python", "color-palette-gen.py", imagePath, String.valueOf(nColors), outputPath
            );
            Process process = processBuilder.start();
            process.waitFor();  // Wait for the process to complete
    
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorBuilder = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorBuilder.append(errorLine).append("\n");
            }
            errorReader.close();
            System.out.println("Python script error output:\n" + errorBuilder.toString());  // Debug: Print error output
    
            int exitCode = process.exitValue();
            System.out.println("Python script exit code: " + exitCode);
    
            File colorFile = new File(outputPath);
            if (colorFile.exists()) {
                updateColorPanel(colorFile);
            } else {
                outputArea.setText("Error generating color palette. File not found: " + outputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.setText("Error running Python script.");
        }
    }

    private void updateColorPanel(File colorFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(colorFile));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();
            String json = jsonBuilder.toString();
    
            JSONArray colorArray = new JSONArray(json);
            colorPanel.removeAll();
            for (int i = 0; i < colorArray.length(); i++) {
                JSONArray color = colorArray.getJSONArray(i);
                int r = color.getInt(0);
                int g = color.getInt(1);
                int b = color.getInt(2);
                Color c = new Color(r, g, b);
                String hexCode = String.format("#%02x%02x%02x", r, g, b);  // Convert to HEX code
    
                JPanel colorSwatch = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(c);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                colorSwatch.setPreferredSize(new Dimension(100, 50));
                
                // Add mouse listener to copy color code
                colorSwatch.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        copyToClipboard(hexCode);  // Copy HEX code to clipboard
                        outputArea.setText("Copied color code: " + hexCode);
                    }
                });
                
                colorPanel.add(colorSwatch);
            }
            colorPanel.revalidate();
            colorPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.setText("Error reading color palette.");
        }
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IroIroNoMi().setVisible(true);
        });
    }
}
