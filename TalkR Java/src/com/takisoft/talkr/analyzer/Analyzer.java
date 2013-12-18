package com.takisoft.talkr.analyzer;

import com.takisoft.talkr.ai.Expression;
import com.takisoft.talkr.ai.Group;
import com.takisoft.talkr.analyzer.AnalyzerConstants.VowelHarmony;
import com.takisoft.talkr.data.DetailConstants;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.helper.NodeResolver;
import com.takisoft.talkr.ui.Message;
import com.takisoft.talkr.ui.Message.Who;
import com.takisoft.talkr.ui.MessageBoard;
import com.takisoft.talkr.utils.Utils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

/**
 *
 * @author Gericop
 */
public class Analyzer {

    private final NodeResolver resolver;
    private final MessageBoard board;

    public Analyzer(MessageBoard board, NodeResolver resolver) {
        this.resolver = resolver;
        this.board = board;
    }

    public void analyzeInput(String sentence) {
        //sentence = Utils.escapeString(sentence);

        System.out.println("Analyze input: " + sentence);
        SecureRandom sr = new SecureRandom();
        IndexHits<Node> hits = resolver.getExpressionsWithFuzzy(sentence);

        if (hits != null) {
            Expression mostProbable = null;

            hits:
            for (Node n : hits) {
                Expression e = new Expression(n);

                if (mostProbable == null) {
                    mostProbable = e;
                }

                Iterable<Relationship> iter = n.getRelationships(DetailConstants.RelTypes.GROUPED);

                rels:
                for (Relationship rel : iter) {
                    Group group = new Group(rel.getOtherNode(n));

                    Integer response = null;
                    if ((response = group.getResponse()) != null) {
                        group = new Group(resolver.findGroup(response));
                    }

                    List<Expression> exps = group.getExpressions();
                    Expression exp = exps.get(sr.nextInt(exps.size()));
                    board.add(new Message(Who.ROBOT, exp.getValue()));
                    break hits;
                }

                //System.out.println(e.getValue() + " | " + e.getNeutral() + " | " + hits.currentScore());
            }

            //board.add(new Message(Who.ROBOT, mostProbable.getValue()));

        } else {
            Group noIdeaGroup = new Group(resolver.findGroup("general_no_idea"));
            List<Expression> exps = noIdeaGroup.getExpressions();

            Expression exp = exps.get(sr.nextInt(exps.size()));
            board.add(new Message(Who.ROBOT, exp.getValue()));
        }
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

    public String getSuffixForObject(String word) {
        word = word.trim();
        String w = word.trim();

        ArrayList<VowelHarmony> harmony = new ArrayList<>();

        char[] chars = w.toCharArray();
        final char[] chFront = VowelHarmony.FRONT.getChars();
        final char[] chBack = VowelHarmony.BACK.getChars();

        Arrays.sort(chFront);
        Arrays.sort(chBack);

        for (char ch : chars) {
            if (Arrays.binarySearch(chFront, ch) >= 0) {
                harmony.add(VowelHarmony.FRONT);
            } else if (Arrays.binarySearch(chBack, ch) >= 0) {
                harmony.add(VowelHarmony.BACK);
            }
        }

        char lastChar = w.charAt(w.length() - 1);
        boolean lastCharVowel = (Arrays.binarySearch(chFront, lastChar) >= 0 || Arrays.binarySearch(chBack, lastChar) >= 0);

        StringBuilder suffixed = new StringBuilder(word);

        if (lastCharVowel) {
            switch (lastChar) {
                case 'a':
                    suffixed.deleteCharAt(suffixed.length() - 1);
                    suffixed.append('á');
                    suffixed.append('t');
                    break;
                case 'e':
                    suffixed.deleteCharAt(suffixed.length() - 1);
                    suffixed.append('é');
                    suffixed.append('t');
                    break;
                default:
                    suffixed.append('t');
                    break;
            }
        } else {
            // pff
        }

        return suffixed.toString();
    }

    private ArrayList<Clause> getClauses(String sentence) {

        return null;
    }
}
