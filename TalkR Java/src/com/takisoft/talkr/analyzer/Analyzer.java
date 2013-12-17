package com.takisoft.talkr.analyzer;

import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.helper.NodeResolver;
import com.takisoft.talkr.utils.Utils;
import java.util.ArrayList;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

/**
 *
 * @author Gericop
 */
public class Analyzer {

    private NodeResolver resolver;

    public Analyzer(NodeResolver resolver) {
        this.resolver = resolver;
    }

    public void analyzeSentence(String sentence) {
        ArrayList<Clause> clauses = getClauses(sentence);

        // CSAK EGY ELŐTESZT
        String[] words = sentence.split(" ");
        System.out.println("### WORDS ###");
        for (int i = 0; i < words.length; i++) {
            words[i] = Utils.removePunctuation(words[i]);

            System.out.println("-- " + words[i]);
            // CSAK TESZT
            StringBuilder sb = new StringBuilder(words[i]);
            IndexHits<Node> hits = null;
            while ((hits = resolver.getWordsStartingWith(sb.toString())) == null) {
                sb.deleteCharAt(sb.length() - 1);
                if (sb.length() == 0) {
                    System.err.println("'" + words[i] + "' cannot be found.");
                    break;
                }
            }

            if (hits != null) {
                for (Node n : hits) {
                    Word w = new Word(n);
                    System.out.println("---- " + w.getWord() + " | " + w.getType());
                }
                hits.close();
                hits = resolver.getWordsWithFuzzy(sb.toString());
                if (hits == null) {
                    System.err.println("--- '" + sb.toString() + "' cannot be found. ---");
                } else {
                    System.out.println("--- FOUND FOR '" + sb.toString() + "' ---");
                    for (Node hit : hits) {
                        Word w = new Word(hit);
                        System.out.println(w.getWord() + " | " + w.getType() + " | " + hits.currentScore());
                    }
                    hits.close();
                }
            }
            // TESZT VÉGE
        }
    }

    private ArrayList<Clause> getClauses(String sentence) {

        return null;
    }
}
