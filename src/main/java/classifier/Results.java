package classifier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dataReader.CSVReader;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jamesfallon on 23/11/2016.
 */
public class Results {

    List<CSVRecord> records;
    private final Set<String> labels = Sets.newHashSet();
    private final File tempFile;

    /**
     * Constructor which initialises the instance variables.
     *
     * @param tempFile - tempFile created by the classifier.
     *                 Has a list of results with the columns 'predicted label, actual label, error'
     * @throws Exception
     */
    public Results(File tempFile) throws Exception {
        this.tempFile = tempFile;
        records = new CSVReader(tempFile.getAbsolutePath()).getDataSet();

        for (CSVRecord record : records) {
            labels.add(record.get("predicted"));
            labels.add(record.get("actual"));
        }

    }

    /**
     * This method writes the results to a csv file.
     * This takes the data from the temp file and prints it to a user specified file.
     *
     * @param filePath - path of where to write the new file.
     * @throws Exception
     */
    public void printResultsToFile(String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(tempFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        FileWriter fstream = new FileWriter(filePath, true);
        BufferedWriter out = new BufferedWriter(fstream);

        String line = null;
        while ((line = in.readLine()) != null) {
            out.write(line);
            out.newLine();
        }

        in.close();
        out.close();
    }

    /**
     * This method returns the classification accuracy of this classifier.
     *
     * @return
     */
    public double getAccuracy() {
        double numberOfErrors = 0.0;
        for (CSVRecord record : records) {
            if (record.get("error").equals("+")) {
                numberOfErrors++;
            }
        }
        System.out.println(records.size());
        return 1.0 - (numberOfErrors / (double) records.size());
    }


    /**
     * This method creates a consfusion matrix for the labels in this dataset.
     *
     * @return - A string which contains a formatted confusion matrix ready for printing
     */
    public String getConfusionMatrix() {
        Map<String, Integer> labelCombinationAndCount = Maps.newHashMap();
        String confusionMatrix = "";


        for (CSVRecord record : records) {
            String combination = record.get("predicted") + "|" + record.get("actual");

            if (labelCombinationAndCount.containsKey(combination)) {
                labelCombinationAndCount.put(combination, labelCombinationAndCount.get(combination) + 1);
            } else {
                labelCombinationAndCount.put(combination, 1);
            }
        }


        //Print header

        confusionMatrix = confusionMatrix.concat("Predicted:");

        for (String predicted : labels) {
            confusionMatrix = confusionMatrix.concat("\t" + predicted);
        }

        confusionMatrix = confusionMatrix.concat("\n");

        for (String actual : labels) {
            confusionMatrix = confusionMatrix.concat(actual + "\t");

            for (String predicted : labels) {

                if (labelCombinationAndCount.containsKey(actual + "|" + predicted)) {
                    confusionMatrix = confusionMatrix.concat(labelCombinationAndCount.get(actual + "|" + predicted) + "\t");
                } else {
                    confusionMatrix = confusionMatrix.concat(0 + "\t");
                }
            }

            confusionMatrix = confusionMatrix.concat("\n");

        }

        return confusionMatrix;

    }

}
