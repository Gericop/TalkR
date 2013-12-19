package com.takisoft.talkr.ai;

import com.takisoft.talkr.data.DetailConstants;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author RedMax
 */
public class Group {

    private String id;
    private int index;
    private List<Expression> expressions;
    private Integer response;
    private Programs programs;

    public Group(Node node) {
        id = (String) node.getProperty(DetailConstants.PROP_KEY_G_ID);
        index = (int) node.getProperty(DetailConstants.PROP_KEY_G_INDEX);

        if (node.hasProperty(DetailConstants.PROP_KEY_G_RESPONSE)) {
            response = (Integer) node.getProperty(DetailConstants.PROP_KEY_G_RESPONSE);
        }

        String ask = null;
        String ans = null;

        if (node.hasProperty(DetailConstants.PROP_KEY_G_ASK)) {
            ask = (String) node.getProperty(DetailConstants.PROP_KEY_G_ASK);
        }

        if (node.hasProperty(DetailConstants.PROP_KEY_G_ANSWER)) {
            ans = (String) node.getProperty(DetailConstants.PROP_KEY_G_ANSWER);
        }

        if (ask != null || ans != null) {
            programs = new Programs(ask, ans);
        }

        Iterable<Relationship> iter = node.getRelationships(DetailConstants.RelTypes.GROUPED);

        for (Relationship rel : iter) {
            if (expressions == null) {
                expressions = new ArrayList<>();
            }

            expressions.add(new Expression(rel.getOtherNode(node)));
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the expressions
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * @param expressions the expressions to set
     */
    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    /**
     * @return the response
     */
    public Integer getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(Integer response) {
        this.response = response;
    }

    /**
     * @return the programs
     */
    public Programs getPrograms() {
        return programs;
    }
}
