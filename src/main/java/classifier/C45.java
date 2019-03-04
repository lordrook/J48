package classifier;

import decisionTree.Tree;
import decisionTree.TreeConstructor;
import driver.Attribute;
import driver.PreprocessedData;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Suavek on 19/11/2016.
 */
public class C45 implements Classifier {

    public Tree decisionTree;
    private Results results;

    /**
     * This method trains a decision tree using the training set specified in the PreprocessedData argument.
     *
     * @param ppd - the dataset to train
     */
    @Override
    public void train(final PreprocessedData ppd) {
        final HashMap attributes = ppd.getAttributes();
        // choose target - assuming that target is a last column
        final Attribute targetAttribute = (Attribute) attributes.get(ppd.getTargetName());
        final List<CSVRecord> dataSet = ppd.getTrainingDataSet();
        TreeConstructor treeConstructor = new TreeConstructor(dataSet, attributes, targetAttribute);
        this.decisionTree = treeConstructor.getDecisionTree();

    }

    /**
     * This method tests the classifier. It uses the testing data set specified in PreprocessedData.
     * It creates a list of results and saves them to a temporary file. The results are in the form
     * 'predicted', 'actual', error with the error column containing a + if the predicted label is not equal to the actual label.
     * The file is then used to create a Results object.
     *
     * @author jamesfallon
     * @param ppd - The dataset to test
     */
    @Override
    public void test(final PreprocessedData ppd) {

        //Get the testing subset of examples
        final List<CSVRecord> dataSet = ppd.getTestingDataSet();

        try {

            //Create a tempfile to store records, if it exists already delete it
            String tempFilePath = System.currentTimeMillis() + "_Results.csv";

            if (new File(tempFilePath).isFile()) {
                new File(tempFilePath).delete();
            }
            File file = File.createTempFile(System.currentTimeMillis() + "_Results", ".csv");


            //Print the header
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, false));
            printWriter.println("actual,predicted,error");

            //For each record, store the actual target value.
            for (CSVRecord instance : dataSet) {

                //For each record, store the actual target value.
                String actual = instance.get(ppd.getTargetName());

                //Classify the instance using the classifier and store the predicted target value
                String predicted = classify(instance);

                //If the two values don't match, mark the error column
                String error = actual.equals(predicted) ? "" : "+";

                printWriter.println(actual + "," + predicted + "," + error);
            }

            printWriter.close();

            try {
                //Create a new results instance using this tempfile
                results = new Results(file);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Classifies an example.
     *
     * @param instance - example to classify
     * @return - String containing the classified label
     */
    @Override
    public String classify(final CSVRecord instance) {
        return decisionTree.search(decisionTree, instance);
    }

    @Override
    public Results getResults() {
        return results;
    }
}
