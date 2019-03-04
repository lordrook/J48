package decisionTree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import driver.Attribute;
import driver.Gain;
import org.apache.commons.csv.CSVRecord;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Suavek on 15/11/2016.
 */
public class Tree implements Serializable {

    private Attribute attribute;
    private String nodeName;
    private String value;
    private final HashMap<String, Tree> childrenDiscrete = Maps.newLinkedHashMap();
    private final List<Tree> childrenContinuous = Lists.newLinkedList();

    /**
     * Constructs a leaf node
     *
     * @param label
     * @param targetAttribute
     */
    public Tree(final String label, Attribute targetAttribute) {
        this.nodeName = label;
        this.value = nodeName;
        this.attribute = targetAttribute;
    }

    /**
     * Constructs a decision node
     *
     * @param gain
     */
    public Tree(final Gain gain) {
        this.nodeName = gain.getAttributeName();
        this.value = gain.getValue();
        this.attribute = gain.getAttribute();
    }


    /**
     * Appends child nodes of Continuous Attributes
     *
     * @param child
     */
    public void addChild(final Tree child) {
        final ArrayList children = new ArrayList<>(this.childrenContinuous);
        //if the same labels on both sides of the decision node then make node a label
        if (child.attribute.isTarget() && children.size() > 0) {
            if (child.nodeName.equals(children.get(0))) {
                this.nodeName = child.nodeName;
                this.attribute = child.attribute;
                return;
            }
        }
        this.childrenContinuous.add(child);
    }

    /**
     * Appends child nodes of discrete attributes
     *
     * @author jamesfallon
     * @param attribute
     * @param child
     */
    public void addChild(Attribute attribute, final String childName, final Tree child) {
        this.attribute = attribute;
        this.childrenDiscrete.put(childName, child);
    }

    /**
     * Search for a label node
     *
     * @param node
     * @param instance
     * @return
     */
    public String search(final Tree node, final CSVRecord instance) {

        if (node.attribute.isTarget()) {
            return node.nodeName;
        }

        final String value = instance.get(node.nodeName);
        if (node.attribute.isContinuous()) {
            // check if value smaller or greater than threshold and continue search recursively
            int direction = new BigDecimal(value).compareTo(new BigDecimal(node.value)) <= 0 ?
                    0 : // left
                    1; // right
            return search(node.childrenContinuous.get(direction), instance);
        }
        try {
            return search(node.childrenDiscrete.get(value), instance);
        } catch (Exception e) {
            // Classifier does not know this value
            return " ";
        }
    }


}

