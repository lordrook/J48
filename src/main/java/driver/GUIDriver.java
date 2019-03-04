package driver;

import classifier.Classifier;
import classifier.Results;


/**
 * This class is used to interact with the C4.5 algorithm from the GUI.
 * It provides methods that allow the GUI to specify the training split percentage,
 * the dataset to use and retrieve the results of the algorithm.
 *
 * Created by jamesfallon on 21/11/2016.
 */
public class GUIDriver {

    private PreprocessedData ppd;
    private double trainingSplitPercentage;
    private Classifier classifier;

    public GUIDriver(double trainingSplitPercentage, Classifier classifier) {
        this.trainingSplitPercentage = trainingSplitPercentage;
        this.classifier = classifier;
    }

    public void test() {
        classifier.test(ppd);
    }

    public void train() {
        ppd.splitTrainingTestPercentage(trainingSplitPercentage);
        classifier.train(ppd);
    }

    public PreprocessedData getPpd() {
        return ppd;
    }

    public void setPpd(PreprocessedData ppd) {
        this.ppd = ppd;
    }

    public double getTrainingSplitPercentage() {
        return trainingSplitPercentage;
    }

    public void setTrainingSplitPercentage(double trainingSplitPercentage) {
        this.trainingSplitPercentage = trainingSplitPercentage;
    }

    public Results getResults() {
        return classifier.getResults();
    }

}
