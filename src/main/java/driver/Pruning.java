package driver;

import java.util.TreeSet;

/**
 * Created by Suavek on 17/11/2016.
 */
public class Pruning {

    /**
     * Method calculates split criterion based on mdl principle
     * <p>
     * gain >= (1/N) x log2(N-1) + (1/N) x [ log2 ((3^|AuB|)-2) - ( |AuB| x Entropy(A+B) &ndash; |A| x Entropy(A) &ndash; |B| x Entropy(B) ]
     * where:
     * N - number of samples in the set
     * A - subset of values < threshold
     * B -  subset of values > threshold
     * |AuB| - number of possible class labels in entire set
     * |A| - in subset A
     * |B| - in subset B
     *
     * @param gain
     * @return boolean
     */
    public static boolean getSplitCriterion(final Gain gain) {

        final double gainValue = gain.getGain();
        final double entropyA = gain.getEntropyA();
        final double entropyB = gain.getEntropyB();
        final double entropyAB = gain.getEntropyAB();
        final double sizeA = gain.recordsBelowEqualThreshold != null ? gain.recordsBelowEqualThreshold.size() : 0;
        final double sizeB = gain.recordsAboveThreshold != null ? gain.recordsAboveThreshold.size() : 0;
        final double N = sizeA + sizeB;
        final double A = gain.occurrenceA.size();
        final double B = gain.occurrenceB.size();
        // |AuB| - number of possible labels in entire set
        final TreeSet labels = new TreeSet();
        labels.addAll(gain.occurrenceA.keySet());
        labels.addAll(gain.occurrenceB.keySet());
        final double AuB = labels.size();

        double leftSideOfFormula = ((1 / N) * (Math.log(N - 1) / Math.log(2))) +
                (1 / N) * ((Math.log(Math.pow(3, AuB) - 2) / Math.log(2)) - (AuB * entropyAB) - (A * entropyA) - (B * entropyB));
        if (leftSideOfFormula < 0) leftSideOfFormula *= 1;
        return gainValue > 0 && gainValue >= leftSideOfFormula;
    }


}
