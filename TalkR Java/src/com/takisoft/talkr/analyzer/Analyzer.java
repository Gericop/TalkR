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
import com.takisoft.talkr.utils.DynamicCompiler;
import com.takisoft.talkr.utils.DynamicHelper;
import com.takisoft.talkr.utils.Utils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final Class dynClass;
    private Object dynProcessor;

    private String lastId;
    private Who lastWho;
    private boolean expectingAnswer = false;

    public RobotLife robot;
    public HumanLife human;

    SecureRandom sr = new SecureRandom();

    Timer timer = new Timer(true);
    AskAQuestionTask task;

    public class AskAQuestionTask extends TimerTask {

        @Override
        public void run() {
            if (human.getName() == null) {
                Expression exp = getRandomExpressionForGroup("what_is_name");
                ask("what_is_name", exp.getValue());
            } else if (human.getAge() == -1) {
                Expression exp = getRandomExpressionForGroup("what_is_age");
                ask("what_is_age", exp.getValue());
            } else if (human.getLocation() == null) {
                Expression exp = getRandomExpressionForGroup("what_is_location");
                ask("what_is_location", exp.getValue());
            }

            rescheduleQuestions();
        }

    }

    public class NoProgramException extends RuntimeException {

    }

    public Analyzer(MessageBoard board, NodeResolver resolver) {
        this.resolver = resolver;
        this.board = board;

        this.robot = new RobotLife();
        this.human = new HumanLife();

        dynClass = DynamicCompiler.getDynamicProcessor();
        try {
            dynProcessor = dynClass.getConstructor(DynamicHelper.class).newInstance(new DynamicHelper(Analyzer.this, resolver));
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.err.println(ex);
        }

        rescheduleQuestions();
    }

    public final void rescheduleQuestions() {
        if (task != null) {
            task.cancel();
        }
        timer.purge();
        task = new AskAQuestionTask();
        timer.schedule(task, sr.nextInt(7000) + 5000);
    }

    public String processUsersQuestion(Group group, String question) throws NoProgramException {
        try {
            Method method = dynClass.getDeclaredMethod(DynamicCompiler.PREFIX_ANSWER + group.getId(), String.class);
            return (String) method.invoke(dynProcessor, question);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.err.println(ex);
            throw new NoProgramException();
        }
    }

    public String processUsersAnswer(String answer) throws NoProgramException {
        try {
            Method method = dynClass.getDeclaredMethod(DynamicCompiler.PREFIX_ASK + lastId, String.class);
            return (String) method.invoke(dynProcessor, answer);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.err.println(ex);
            throw new NoProgramException();
        }
    }

    public void analyzeInput(String sentence) {
        //sentence = Utils.escapeString(sentence);
        rescheduleQuestions();
        System.out.println("input: " + sentence + " | expectingAnswer: " + expectingAnswer + " | lastId: " + lastId);

        if (expectingAnswer && lastId != null) {
            expectingAnswer = false;
            try {
                String result = processUsersAnswer(sentence);
                lastId = null;

                if (result != null) {
                    say(result);
                    return;
                } else {
                    Expression exp = getRandomExpressionForGroup("why_no_answer");
                    say(exp.getValue());
                    return;
                }
            } catch (NoProgramException e) {
            }
        }
        IndexHits<Node> hits = resolver.getExpressionsWithFuzzy(sentence);

        if (hits != null) {
            Expression mostProbable = null;

            hits:
            for (Node n : hits) {
                Expression e = new Expression(n);

                System.out.println(e.getValue() + " | " + e.getNeutral() + " | " + hits.currentScore());

                if (mostProbable == null) {
                    mostProbable = e;
                }

                Iterable<Relationship> iter = n.getRelationships(DetailConstants.RelTypes.GROUPED);

                rels:
                for (Relationship rel : iter) {
                    Group group = new Group(rel.getOtherNode(n));
                    lastId = group.getId();
                    lastWho = Who.HUMAN;

                    try {
                        String result = processUsersQuestion(group, sentence);
                        if (result != null) {
                            say(result);
                            break hits;
                        }
                    } catch (NoProgramException ex) {
                    }

                    Integer response;
                    if ((response = group.getResponse()) != null) {
                        group = new Group(resolver.findGroup(response));
                        //lastId = group.getId();
                    }

                    List<Expression> exps = group.getExpressions();
                    Expression exp = exps.get(sr.nextInt(exps.size()));
                    if (response != null || (group.getPrograms() != null && group.getPrograms().getAsk() != null)) {
                        ask(group.getId(), exp.getValue());
                    } else {
                        say(exp.getValue());
                    }
                    break hits;
                }
            }

            if (lastId != null && lastId.equals("hi") && human.getName() == null) {
                Expression exp = getRandomExpressionForGroup("what_is_name");
                ask("what_is_name", exp.getValue());
            }

            //board.add(new Message(Who.ROBOT, mostProbable.getValue()));
        } else {
            Expression exp = getRandomExpressionForGroup("general_no_idea");
            say(exp.getValue());
        }
    }

    private void say(String answer) {
        board.add(new Message(Who.ROBOT, answer));
    }

    private void ask(String id, String question) {
        board.add(new Message(Who.ROBOT, question));
        lastId = id;
        lastWho = Who.ROBOT;
        expectingAnswer = true;
    }

    private Expression getRandomExpressionForGroup(String groupId) {
        Group noIdeaGroup = new Group(resolver.findGroup(groupId));
        List<Expression> exps = noIdeaGroup.getExpressions();

        Expression exp = exps.get(sr.nextInt(exps.size()));
        return exp;
    }

    @Deprecated
    private void analyzeSentence(String sentence) {
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
