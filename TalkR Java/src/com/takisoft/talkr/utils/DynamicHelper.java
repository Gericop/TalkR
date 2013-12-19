package com.takisoft.talkr.utils;

import com.takisoft.talkr.ai.Expression;
import com.takisoft.talkr.ai.Group;
import com.takisoft.talkr.analyzer.Analyzer;
import com.takisoft.talkr.analyzer.HumanLife;
import com.takisoft.talkr.analyzer.RobotLife;
import com.takisoft.talkr.data.Antonym;
import com.takisoft.talkr.data.Synonym;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.helper.NodeResolver;
import java.util.ArrayList;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

/**
 *
 * @author Gericop
 */
public class DynamicHelper {

    private final Analyzer analyzer;
    private final NodeResolver resolver;

    public DynamicHelper(Analyzer analyzer, NodeResolver resolver) {
        this.analyzer = analyzer;
        this.resolver = resolver;
    }

    public Expression findExpression(String exp) {
        IndexHits<Node> hits = resolver.getExpressionsWithFuzzy(exp);

        if (hits != null && hits.hasNext()) {
            Expression e = new Expression(hits.next());
            hits.close();
            return e;
        }

        return null;
    }

    public Group findGroup(String groupId) {
        Node node = resolver.findGroup(groupId);

        if (node != null) {
            return new Group(node);
        }

        return null;
    }

    public Group findGroup(int groupIndex) {
        Node node = resolver.findGroup(groupIndex);

        if (node != null) {
            return new Group(node);
        }

        return null;
    }

    public Word findName(String name) {
        IndexHits<Node> hits = resolver.getWordsWithFuzzy(name);

        if (hits != null) {
            for (Node node : hits) {
                Word word = new Word(node);
                if (word.getType() == Word.WordType.PROPERTY) {
                    return word;
                }
            }
            hits.close();
        }

        return null;
    }

    public Word findPlace(String name) {
        IndexHits<Node> hits = resolver.getWordsWithFuzzy(name);

        if (hits != null) {
            for (Node node : hits) {
                Word word = new Word(node);
                if (word.getType() == Word.WordType.GEO) {
                    return word;
                }
            }
            hits.close();
        }

        return null;
    }

    public ArrayList<Synonym> findSynonyms(String word) {
        IndexHits<Node> hits = resolver.getWordsWithFuzzy(word);

        if (hits != null && hits.hasNext()) {
            Word w = new Word(hits.next());
            hits.close();
            return w.getSynonyms();
        }

        return null;
    }

    public ArrayList<Antonym> findAntonyms(String word) {
        IndexHits<Node> hits = resolver.getWordsWithFuzzy(word);

        if (hits != null && hits.hasNext()) {
            Word w = new Word(hits.next());
            hits.close();
            return w.getAntonyms();
        }

        return null;
    }

    public RobotLife getRobot() {
        return analyzer.robot;
    }

    public HumanLife getHuman() {
        return analyzer.human;
    }
}
