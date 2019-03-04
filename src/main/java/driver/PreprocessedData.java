package driver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dataReader.CSVReader;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Suavek on 08/11/2016.
 */
public class PreprocessedData {

    private final CSVReader data;
    private ArrayList<String> attributeNames = Lists.newArrayList();
    private final String targetName;
    final HashMap<String, Attribute> attributes = Maps.newHashMap();

    List trainingSet = Lists.newArrayList();
    List testingSet = Lists.newArrayList();

    public PreprocessedData(final String filePath) throws Exception {
        // currently supporting csv format only
        if (filePath.toLowerCase().endsWith(".csv")) {
            this.data = new CSVReader(filePath);
        } else {
            throw new Exception("Data Format not supported");
        }

        // get attribute names
        this.attributeNames = this.data.getAttributeNames();
        // for each create an Attribute object and place in the map
        this.attributeNames.forEach(attributeName ->
                attributes.put(
                        attributeName,
                        createAttribute(this.data.getDataSet(), attributeName)
                )
        );
        // get target name, assuming position in last column
        this.targetName = attributeNames.get(attributeNames.size() - 1);
        // set the target
        this.attributes.get(targetName).setAsTarget();
    }

    //http://www.saedsayad.com/decision_tree.htm

    /**
     * Method creates and returns an Attribute object with assigned type :
     * All records of an attribute are tested towards converting to a Double
     * If exception occurs then it indicates that Attribute consists of discrete values
     * therefore is not continuous
     *
     * @param dataRecords
     * @param attributeName
     * @return
     */
    public static Attribute createAttribute(final Iterable<CSVRecord> dataRecords, final String attributeName) {
        // Check if attribute is continuous
        boolean isContinuous = true;
        for (final CSVRecord record : dataRecords) {
            try {
                final String value = record.get(attributeName);
                if (!value.trim().equals("")) {
                    Double.parseDouble(record.get(attributeName));
                }
            } catch (final NumberFormatException e) {
                isContinuous = false;
                return new Attribute(attributeName, isContinuous);
            }
        }
        return new Attribute(attributeName, isContinuous);
    }

    /**
     * Method splits the data set into 2 subsets of random values by percentage given as parameter
     *
     * @param splitPercent
     */
    public void splitTrainingTestPercentage(double splitPercent) {
        int numberOfTrainingSamples = (int) Math.ceil(getDataSet().size() * splitPercent);
        int numberOfTestingSamples = getDataSet().size() - numberOfTrainingSamples;

        List instancesLocalSet = Lists.newArrayList(getDataSet());

        for (int i = 0; i < numberOfTestingSamples; i++) {
            CSVRecord tmpSample = (CSVRecord) getDataSet().get(ThreadLocalRandom.current().nextInt(0, instancesLocalSet.size()));
            this.testingSet.add(tmpSample);
            instancesLocalSet.remove(tmpSample);
        }
        this.trainingSet = instancesLocalSet;
    }

    public List getDataSet() {
        return this.data.getDataSet();
    }

    public List getTrainingDataSet() {
        return this.trainingSet.size() > 0 ?
                this.trainingSet :
                this.data.getDataSet();

    }

    public List getTestingDataSet() {
        return this.testingSet;
    }

    public HashMap<String, Attribute> getAttributes() {
        return this.attributes;
    }

    public ArrayList<String> getAttributeNames() {
        return this.attributeNames;
    }

    public String getTargetName() {
        return this.targetName;
    }

}
