package decisionTree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import driver.Attribute;
import driver.Gain;
import driver.Pruning;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.SerializationUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Suavek on 07/11/2016.
 */
public class TreeConstructor {

    Tree root;

    public TreeConstructor(final List<CSVRecord> dataSet, final HashMap attributes, final Attribute targetAttribute) {
        this.root = constructDecisionTree(dataSet, attributes, targetAttribute);
    }

    /**
     * Iterative method that constructs a decision tree based on the given dataset.
     *
     * @param dataSet    - List of classified examples to train on
     * @param attributes - List of attributes
     * @param target     - The target attribute
     * @return
     */
    //http://blog.takipi.com/benchmark-how-java-8-lambdas-and-streams-can-make-your-code-5-times-slower/ lambda performance
    private static Tree constructDecisionTree(final List<CSVRecord> dataSet, final HashMap<String, Attribute> attributes, final Attribute target) {

        // Calculate Target Entropy
        final double targetEntropy = calculateEntropy(dataSet, target);
        // if entropy = 0 create leaf
        if (!(targetEntropy > 0)) {
            // extract label
            final CSVRecord instance = Iterables.get(dataSet, 0);

            System.out.println("\n------------------" + instance.get(target.getName()) + "\n\n");
            // create and return new node of target type
            return new Tree(instance.get(target.getName()), target);
        }

        // holds information on the attribute that gives the maximum gain
        final Gain maximumGain = getMaximumGain(dataSet, attributes, target, targetEntropy);
        dataSet.removeAll(dataSet); // remove processed data

        if (Pruning.getSplitCriterion(maximumGain))
            System.out.println("\nFeature: " + maximumGain.getAttributeName() + "\nGAIN: " + maximumGain.getGain() + "\nTHRESHOLD: " + maximumGain.getValue() + "\nSPLIT :" + Pruning.getSplitCriterion(maximumGain) + "\n");

        // Create new decision node using attribute that gives the biggest gain
        final Tree node = new Tree(maximumGain);

        // Add children
        // If Attribute is discrete
        if (!maximumGain.getAttribute().isContinuous()) {
            //Exclude attribute from the next iteration
            HashMap<String, Attribute> reducedAttributesList = (HashMap<String, Attribute>) SerializationUtils.clone(attributes);
            reducedAttributesList.remove(maximumGain.getAttribute().getName());
            // reate children nodes given names of the discrete Attribute values
            maximumGain.getSubsetsDiscrete().forEach((value, subset) ->
                    node.addChild(
                            maximumGain.getAttribute(),
                            value.toString(),
                            constructDecisionTree((List<CSVRecord>) subset, reducedAttributesList, target))
            );
        } else {
            // grow only if growth improves impurity measure
            // check if data set should be split further (pre-pruning by use of mdl principle)
            if (Pruning.getSplitCriterion(maximumGain)) {
                // if can be split, create child nodes recursively for each subset of dataSet 
                maximumGain.getSubsets().forEach(subset ->
                        node.addChild(
                                constructDecisionTree(
                                        (List<CSVRecord>) subset,
                                        attributes,
                                        target)
                        )
                );
            } else {
                // if can't be split, create leaf node taking most occurring label 
                return new Tree(maximumGain.getMostOccurringLabel(), target);

            }
        }
        return node;
    }

    /**
     * Method iterates through all attributes searching for and returning the biggest gain
     *
     * @param dataSet
     * @param attributes
     * @param target
     * @param targetEntropy
     * @return
     */
    private static Gain getMaximumGain(List<CSVRecord> dataSet, HashMap<String, Attribute> attributes, Attribute target, double targetEntropy) {
        final Gain[] maxGain = {null};
        // for each attribute excluding target
        attributes.forEach((attributeName, attribute) -> {
            if (attribute.isTarget())
                return;
            // Calculate gain
            final Gain gainOfAnAttribute = getGain(dataSet, attribute, target, targetEntropy);
            // if greater or equal to previous, set as biggest
            if (maxGain[0] == null) {
                maxGain[0] = gainOfAnAttribute;
            } else if (gainOfAnAttribute.getGain() >= maxGain[0].getGain()) {
                maxGain[0] = gainOfAnAttribute;
            }
        });
        return maxGain[0];
    }


    /**
     * Method calculates and returns gain of a given Attribute
     *
     * @param dataSet
     * @param attribute
     * @param target
     * @param targetEntropy
     * @return
     */
    private static Gain getGain(final List<CSVRecord> dataSet, final Attribute attribute, final Attribute target, final double targetEntropy) {
        final Gain[] finalGain = {new Gain()};
        // Create thresholds for either Discrete or Continuous data
        final ArrayList<String> thresholds = createThresholdsForAttribute(dataSet, attribute, target);
        // if Attribute is Continuous then find gains for each from given thresholds and return biggest 
        if (attribute.isContinuous()) {
            thresholds.forEach(threshold -> {
                // Calculate gain of Continuous Attribute based on a given threshold
                final Gain tmp = getContinuousAttributeGain(dataSet, attribute, target, targetEntropy, threshold);
                // if greater than previous then set as new biggest
                if (tmp.getGain() >= finalGain[0].getGain()) {
                    finalGain[0] = tmp;
                }
            });
            return finalGain[0];
        } else {
            // Otherwise, calculate gain for the Discrete Attribute
            return getDiscreteAttributeGain(dataSet, attribute, target, targetEntropy, thresholds);
        }
    }


    /**
     * This method calculates and returns the information gain of a discrete atrribute.
     *
     * @param dataSet
     * @param attribute
     * @param target
     * @param targetEntropy
     * @param uniqueAttributeValues
     * @author jamesfallon
     * @return
     */
    public static Gain getDiscreteAttributeGain(final List<CSVRecord> dataSet, final Attribute attribute, final Attribute target, final double targetEntropy, final ArrayList<String> uniqueAttributeValues) {

        //Map to hold subsets for each unique attribute value
        Map<String, List<CSVRecord>> subsets = Maps.newLinkedHashMap();

        //Map to hold the count of each target value for each unique attribute value
        Map<String, Map> occurrencesOfLabelsInSubsets = Maps.newLinkedHashMap();

        uniqueAttributeValues.forEach(value -> {
            //Holds examples that have the given attribute value
            final List<CSVRecord> subset = Lists.newArrayList();
            //Loop through all examples and if that example contains the attribute value add it to the subset
            dataSet.forEach(example -> {
                if (value.equals(example.get(attribute.getName()))) {
                    subset.add(example);
                }
            });
            //Add subset to the list of subsets
            subsets.put(value, subset);
            //Add the target values count for this unique attribute value
            occurrencesOfLabelsInSubsets.put(value.toString(), countDecisionClassLabels(subset, target));
        });

        //Map to hold the entropy of each subset
        Map subsetsEntropies = Maps.newLinkedHashMap();
        final double[] gain = {targetEntropy};
        //Iterate through the list of subsets, extract individual information gain and calculate gain of the entire dataSet
        subsets.forEach((value, subset) -> {
            double probabilityOfSubset = (double) subset.size() / (double) dataSet.size();
            double entropyOfSubset = calculateEntropy(subset, target);
            gain[0] -= (probabilityOfSubset * entropyOfSubset);
            subsetsEntropies.put(value, entropyOfSubset);
        });
        return new Gain(attribute, gain, subsets, occurrencesOfLabelsInSubsets, subsetsEntropies);
    }

    /**
     * Method calculates and returns gain of the Continuous Attribute
     *
     * @param dataSet
     * @param attribute
     * @param target
     * @param targetEntropy
     * @param threshold
     * @return
     */
    public static Gain getContinuousAttributeGain(final List<CSVRecord> dataSet, final Attribute attribute, final Attribute target, final double targetEntropy, final String threshold) {
        // holds subsets of data, below and above the threshold
        final List instancesBelowThreshold = Lists.newArrayList();
        final List instancesAboveThreshold = Lists.newArrayList();
        // for each instance in the dataSet
        dataSet.forEach(instance -> {
            // Get Attribute's value from the current instance
            final String value = instance.get(attribute.getName());
            // if value smaller equal than threshold, associate instance with the left subset, otherwise right
            if (new BigDecimal(value).compareTo(new BigDecimal(threshold)) <= 0) {
                instancesBelowThreshold.add(instance);
            } else {
                instancesAboveThreshold.add(instance);
            }
        });

        // Calculate information gain
        // Probabilities of subsets
        final double numInstancesBelowThreshold = instancesBelowThreshold.size();
        final double numInstancesAboveThreshold = instancesAboveThreshold.size();
        final double numInstances = numInstancesBelowThreshold + numInstancesAboveThreshold;
        final double probA = numInstancesBelowThreshold / numInstances;
        final double probB = numInstancesAboveThreshold / numInstances;

        // Calculate subset's entropy
        final double entropyA = calculateEntropy(instancesBelowThreshold, target);
        final double entropyB = calculateEntropy(instancesAboveThreshold, target);

        //Calculate gain for the given threshold
        final double gain = targetEntropy - (probA * entropyA) - (probB * entropyB);

        // Create labels occurrences maps for each subset
        final HashMap decisionClassesLeft = countDecisionClassLabels(instancesBelowThreshold, target);
        final HashMap decisionClassesRight = countDecisionClassLabels(instancesAboveThreshold, target);

        return new Gain(attribute, entropyA, entropyB, threshold, gain, decisionClassesLeft, decisionClassesRight, instancesBelowThreshold, instancesAboveThreshold, targetEntropy);
    }

    /**
     * Method generates thresholds for the Continuous and Discrete Attributes
     *
     * @param dataSet
     * @param attribute
     * @param target
     * @return
     */
    public static ArrayList createThresholdsForAttribute(final List<CSVRecord> dataSet, final Attribute attribute, final Attribute target) {
        // Holds unique values of thresholds i ascending order
        final TreeSet thresholds = new TreeSet<>();
        // if discrete data then return all unique values
        if (!attribute.isContinuous()) {
            // get unique values of an attribute
            dataSet.forEach(instance -> thresholds.add(instance.get(attribute.getName())));
            return new ArrayList(thresholds);
        }

        // if data is continuous
        // sort records in ascending order
        Collections.sort(dataSet, (r1, r2) -> r1.get(attribute.getName()).compareTo(r2.get(attribute.getName())));
        // extract thresholds by checking at what points the decision class changes
        final CSVRecord[] previousInstance = {dataSet.get(0)};
        // for every instance in the dataSet
        dataSet.forEach(instance -> {
            // compare current and previous labels
            final String previousLabel = previousInstance[0].get(target.getName());
            final String currentLabel = instance.get(target.getName());
            // if different then add the Attribute value of the previous instance
            if (!currentLabel.equals(previousLabel)) {
                final String threshold = previousInstance[0].get(attribute.getName()).toString();
                thresholds.add(threshold);
            }
            previousInstance[0] = instance;

        });
        // return sorted list of unique thresholds
        return new ArrayList(thresholds);

    }

    /**
     * Method calculates the entropy for a given set of data
     *
     * @param dataSet
     * @param target
     * @return
     */
    public static Double calculateEntropy(final List<CSVRecord> dataSet, final Attribute target) {
        final double numberOfSamples = dataSet.size();
        // count occurrences of each decision class
        // returns HashMap where key = label and value = occurrence
        final HashMap countMap = countDecisionClassLabels(dataSet, target);
        final double[] entropy = {0};
        // for each label in the countMap get its corresponding occurrences
        // and use to calculate probabilities for entropy calculation
        countMap.keySet().forEach(key -> {
            final double probabilityOfValue = (
                    Double.parseDouble(
                            String.valueOf(countMap.get(key)))
                            /
                            numberOfSamples);
            entropy[0] -= (probabilityOfValue * (Math.log(probabilityOfValue) / Math.log(2)));

        });
        return entropy[0];
    }

    /**
     * This method gets the number of occurrences of each target value.
     *
     * @param dataSet
     * @param target
     * @return
     */
    public static HashMap countDecisionClassLabels(final List<CSVRecord> dataSet, final Attribute target) {
        final HashMap countMap = Maps.newHashMap();
        // For each instance in the dataSet add that target value to the map or increment the count if it exists
        dataSet.forEach(instance -> {

            final String label = instance.get(target.getName()).toString();
            if (!countMap.containsKey(label)) {
                countMap.put(label, (double) 1);
            } else {
                Double count = (Double) countMap.get(label);
                count = count + 1;
                countMap.put(label, count);
            }
        });
        return countMap;
    }

    public Tree getDecisionTree() {
        return root;
    }

}
