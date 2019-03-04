package driver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Suavek on 11/11/2016.
 */
public class Gain {

    private Attribute attribute;
    private double entropyA;
    private double entropyB;
    private double entropyAB;
    private String value;
    private double informationGain;
    HashMap<String, Double> occurrenceA = Maps.newHashMap();
    HashMap<String, Double> occurrenceB = Maps.newHashMap();
    List<CSVRecord> recordsBelowEqualThreshold;
    List<CSVRecord> recordsAboveThreshold;

    List<List<CSVRecord>> subsets = Lists.newArrayList();
    Map<String, List<CSVRecord>> subsetsDiscrete;
    Map<String, Map> occurrencesOfLabelsInSubsets;
    Map subsetsEntropy;


    public Map getSubsetsDiscrete() {
        return this.subsetsDiscrete;
    }

    public Gain(final Attribute attribute, final double entropyA, final double entropyB, final String threshold, final double gain, final HashMap a, final HashMap b, final List indexListA, final List indexListB, double targetEntr) {
        this.attribute = attribute;
        this.entropyA = entropyA;
        this.entropyB = entropyB;
        this.value = threshold;
        this.informationGain = gain;
        this.occurrenceA = a;
        this.occurrenceB = b;
        this.recordsBelowEqualThreshold = indexListA;
        this.recordsAboveThreshold = indexListB;
        this.subsets.add(indexListA);
        this.subsets.add(indexListB);
        this.entropyAB = targetEntr;
    }

    public Gain() {
    }

    public Gain(Attribute attribute, double[] gain, Map<String, List<CSVRecord>> subsets, Map<String, Map> occurrencesOfLabelsInSubsets, Map subsetsEntropy) {
        this.attribute = attribute;
        this.informationGain = gain[0];
        this.subsetsDiscrete = subsets;
        this.occurrencesOfLabelsInSubsets = occurrencesOfLabelsInSubsets;
        this.subsetsEntropy = subsetsEntropy;
    }

    /**
     * Method returns most occurring label
     * Used in pre-pruning when decision is made on not spiting further
     * The Gain object holds information on labels occurrences in two subsets
     * and this method extract most occurring value in both both
     *
     * @return
     */
    public String getMostOccurringLabel() {
        // concatenate two maps by summing values of duplicate keys
        final Map<String, Double> mergedMaps = Stream.concat(occurrenceA.entrySet().stream(), occurrenceB.entrySet().stream())
                .collect(Collectors.toMap(
                        entry -> entry.getKey(), // key
                        entry -> entry.getValue(), // value
                        (occurrenceA, occurrenceB) -> occurrenceA + occurrenceB) // merge
                );
        // return key (label) of the biggest value in the map
        return Collections.max(mergedMaps.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public double getGain() {
        return this.informationGain;
    }

    public double getEntropyA() {
        return entropyA;
    }

    public double getEntropyB() {
        return entropyB;
    }

    public double getEntropyAB() {
        return entropyAB;
    }

    public String getValue() {
        return value;
    }

    public String getAttributeName() {
        return attribute.getName();
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public List getLeftSubset() {
        return recordsBelowEqualThreshold;
    }

    public List getRightSubset() {
        return recordsAboveThreshold;
    }

    public List getSubsets() {
        return subsets;
    }


}
