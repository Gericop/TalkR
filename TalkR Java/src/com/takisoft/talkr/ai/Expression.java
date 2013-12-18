package com.takisoft.talkr.ai;

import com.takisoft.talkr.data.DetailConstants;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Gericop
 */
public class Expression {

    private String value;
    private String neutral;

    public Expression(Node node) {
        value = (String) node.getProperty(DetailConstants.PROP_KEY_E_VALUE);
        neutral = (String) node.getProperty(DetailConstants.PROP_KEY_E_NEUTRAL);
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the neutral
     */
    public String getNeutral() {
        return neutral;
    }

    /**
     * @param neutral the neutral to set
     */
    public void setNeutral(String neutral) {
        this.neutral = neutral;
    }
}
