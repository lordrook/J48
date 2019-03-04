package userInterface;

import classifier.C45;
import classifier.Results;
import driver.GUIDriver;
import driver.PreprocessedData;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;


/**
 * Created by jamesfallon on 21/11/2016.
 *
 * This class is the entry point for the application. It defines and opens a GUI for the application.
 */
public class GUI {

    JFrame gui;

    public static void main(final String... args) throws Exception {
        GUI gui = new GUI();
    }

    /**
     * @throws Exception
     * @author jamesfallon
     */
    public GUI() throws Exception {


        /**
         * Create a GUIDriver object. This will be used as a go-between for the GUI and the algorithm.
         * Initialise the training percentage as 66%.
         */

        GUIDriver driver = new GUIDriver(0.66, new C45());

        /**
         * Create the frame.
         */

        JFrame frame = new JFrame("C4.5 Classifier");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        /**
         * Create a panel for storing options
         */

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());

        /**
         * Create and initialize components
         */


        //Button which opens a file chooser window.
        JButton fileChooserButton = new JButton("Open dataset...");

        //Label which displays what file has been selected
        JLabel selectedFile = new JLabel();
        selectedFile.setText("             No file has been selected");
        selectedFile.setLabelFor(fileChooserButton);

        //Text field which is used to input the training split percentage
        JTextField trainingSplitPercentageTextField = new JTextField();
        trainingSplitPercentageTextField.setText(Double.toString(driver.getTrainingSplitPercentage()));

        //Label for the training split percentage text field
        JLabel trainingSplitPercentageLabel = new JLabel("  Percentage of dataset to train on:");
        trainingSplitPercentageLabel.setLabelFor(trainingSplitPercentageTextField);

        //Text field for specifying the number of decision trees to create
        JTextField numberOfTreesToCreateTextField = new JTextField();
        numberOfTreesToCreateTextField.setText("10");

        //Label for the 'number of trees to create' text field.
        JLabel numberOfTimesToRunLabel = new JLabel("  Number of trees to create:");
        numberOfTimesToRunLabel.setLabelFor(numberOfTreesToCreateTextField);

        //Text area which displays the results of the algorithm
        JTextArea textDisplay = new JTextArea(40, 40);
        textDisplay.setEditable(false);
        JScrollPane display = new JScrollPane(textDisplay);

        //Button that runs the algorithm
        JButton runButton = new JButton("Run");

        //Button that saves the results to a file. Only visible once the algorithm has successfully run
        JButton saveResultsButton = new JButton("Save Results");
        saveResultsButton.setVisible(false);


        /**
         * Add action listeners
         */

        fileChooserButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            //Program currently only supports CSV files
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("csv datasets (*.csv)", "csv");
            fileChooser.setFileFilter(csvFilter);

            int returnValue = fileChooser.showOpenDialog(new JFrame());
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    selectedFile.setText(file.getAbsolutePath());
                    driver.setPpd(new PreprocessedData(file.getAbsolutePath()));

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Not a valid csv file.");
                    return;
                }


            }
        });

        saveResultsButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();

            //Program currently only supports CSV files

            int returnValue = fileChooser.showSaveDialog(new JFrame());
            if (returnValue == JFileChooser.APPROVE_OPTION) {

                File file = fileChooser.getSelectedFile();

                try {
                    driver.getResults().printResultsToFile(file.getAbsolutePath());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        runButton.addActionListener(e -> {

            //Check that a file has been selected
            if (driver.getPpd() == null) {
                JOptionPane.showMessageDialog(null, "Please specify a file to use.");
                return;
            }

            //Check that the number entered for the training split percentage is a double
            try {
                double split = Double.parseDouble(trainingSplitPercentageTextField.getText());

                if (!(split < 1.0 && split > 0.0)) {
                    throw new NumberFormatException();
                }
            } catch (final NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please enter a value between 0 and 1 for the training split percentage.");
                return;
            }

            //Check if the 'number of times to run' is valid
            try {

                int treesToCreate = Integer.parseInt(numberOfTreesToCreateTextField.getText());

                if (treesToCreate <= 0 || treesToCreate > 50) {
                    throw new NumberFormatException();
                }


            } catch (final NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number of trees to create (1-50).");
                return;
            }

            //Both numbers are OK so store them
            double trainingSplitPercentage = Double.parseDouble(trainingSplitPercentageTextField.getText());
            int numberOfTreesToCreate = Integer.parseInt(numberOfTreesToCreateTextField.getText());


            /**
             * Run the C45 algorithm for the number of times specified
             */


            //Hold aggregate values
            String resultText = "";
            double[] accuracies = new double[numberOfTreesToCreate];


            for (int i = 0; i < numberOfTreesToCreate; i++) {

                //Create a new PreprocessedData instance
                try {
                    driver.setPpd(new PreprocessedData(selectedFile.getText()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                driver.setTrainingSplitPercentage(trainingSplitPercentage);
                driver.train();
                driver.test();
                Results results = driver.getResults();

                //Specify what tree these results belong to
                resultText = resultText.concat("Tree " + (i + 1) + ":\n\n");

                double classificationAccuracy = results.getAccuracy();
                accuracies[i] = classificationAccuracy;

                //Print the accuracy and confusion matrix
                resultText = resultText.concat("Classification Accuracy: " + results.getAccuracy() + "\n\n");
                resultText = resultText.concat(results.getConfusionMatrix());

                resultText = resultText.concat("\n");
                resultText = resultText.concat("__________________________________________________\n\n");
            }

            //Print results to the display
            textDisplay.setText(resultText);
            saveResultsButton.setVisible(true);

            //Calculate and print the mean classification accuracy
            double sum = 0.0;

            for (double d : accuracies) sum += d;

            double meanAccuracy = sum / (double) accuracies.length;

            textDisplay.append("Mean Classification Accuracy: " + meanAccuracy);

        });


        /**
         * Display the components.
         *
         */

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 2;
        gbc.weighty = 4;

        /**
         * Column 1:
         */

        //Row 1

        gbc.gridx = 0;
        gbc.gridy = 0;

        optionsPanel.add(fileChooserButton, gbc);

        //Row 2

        gbc.gridx = 0;
        gbc.gridy = 1;

        optionsPanel.add(trainingSplitPercentageLabel, gbc);

        //Row 3

        gbc.gridx = 0;
        gbc.gridy = 2;

        optionsPanel.add(numberOfTimesToRunLabel, gbc);


        //Row 4

        gbc.gridx = 0;
        gbc.gridy = 3;

        optionsPanel.add(runButton, gbc);

        /**
         * Column 2:
         **/

        gbc.anchor = GridBagConstraints.LINE_END;

        //Row 1

        gbc.gridx = 1;
        gbc.gridy = 0;

        optionsPanel.add(selectedFile, gbc);

        //Row 2

        gbc.gridx = 1;
        gbc.gridy = 1;

        optionsPanel.add(trainingSplitPercentageTextField, gbc);

        //Row 3

        gbc.gridx = 1;
        gbc.gridy = 2;

        optionsPanel.add(numberOfTreesToCreateTextField, gbc);

        //Row 4

        gbc.gridx = 1;
        gbc.gridy = 3;

        optionsPanel.add(saveResultsButton, gbc);


        /**
         * Add the options panel to the frame
         */

        frame.add(optionsPanel);

        /**
         * Add the display to the frame
         */

        frame.add(display);

        //Size the frame.
        frame.setSize(500, 800);


        //Show it.
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        this.gui = frame;
    }


}
