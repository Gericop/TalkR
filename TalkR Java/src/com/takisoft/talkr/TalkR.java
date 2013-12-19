package com.takisoft.talkr;

import com.google.gson.Gson;
import com.takisoft.talkr.ai.Group;
import com.takisoft.talkr.analyzer.Analyzer;
import com.takisoft.talkr.data.Coverb;
import com.takisoft.talkr.data.DetailConstants;
import com.takisoft.talkr.data.PageData;
import com.takisoft.talkr.data.PageDataParser;
import com.takisoft.talkr.data.Synonym;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.data.XMLParser;
import com.takisoft.talkr.data.XMLParser.XMLParserListener;
import com.takisoft.talkr.helper.NodeResolver;
import com.takisoft.talkr.ui.Message;
import com.takisoft.talkr.ui.Message.Who;
import com.takisoft.talkr.ui.MessageBoard;
import com.takisoft.talkr.utils.DynamicCompiler;
import com.takisoft.talkr.utils.DynamicHelper;
import com.takisoft.talkr.utils.Utils;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.IndexHits;

/**
 * The Hungarian chatter bot.
 *
 * @author Gericop
 */
public class TalkR extends JFrame implements XMLParserListener {

    public static final String DB_PATH = "test_db";
    boolean isWordDbReady = false;
    private NodeResolver resolver;
    private GraphDatabaseService graphDb;
    private Analyzer analyzer;
    private final MessageBoard board = new MessageBoard();
    private final JTextArea userInput = new JTextArea(1, 80);
    private final JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final KeyAdapter keyListener;

    public TalkR() {
        this.keyListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    sendDataToAnalyzer();
                }
            }
        };
    }

    public void initWindow() {
        setTitle("TalkR v0.3");
        setSize(640, 480);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        scrollPane.setViewportView(board);

        add(scrollPane, BorderLayout.CENTER);

        //userInput.setLineWrap(true);
        //userInput.setWrapStyleWord(true);
        userInput.addKeyListener(keyListener);

        add(userInput, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void initData() {
        startDatabase();

        if (!isWordDbReady) {
            resolver.beginTransaction();
            initDatabaseFromXML();
            resolver.endTransaction();

            resolver.beginTransaction();
            initDatabaseFromCustomFile();
            resolver.endTransaction();
        }
        
        //TODO remove this
        //initDatabaseFromCustomFile();
        analyzer = new Analyzer(board, resolver);

        /*Node topicsNode = resolver.findCategory("Témák");
        
         ArrayList<Category> topics = resolver.findCategoriesByRelationship(topicsNode, DetailConstants.RelTypes.LINKED);
         for (Category c : topics) {
         System.out.println("# " + c.getTitle().toLowerCase());
         }*/
        //testFindWords();
        //wordTester();
    }

    private void sendDataToAnalyzer() {
        String input = userInput.getText();
        userInput.setText("");
        if (input.trim().isEmpty()) {
            return;
        }

        board.add(new Message(Who.HUMAN, input));
        //analyzer.analyzeSentence(input);
        analyzer.analyzeInput(input);
    }

    private void testFindWords() {
        long start, end;
        String[] myWords = new String[]{"klenódium", "felhőszakadás", "víz", "aludni", "lapátol", "műjég", "hogyha", "fog", "majd", "felfog", "összefog", "mond"};
        for (String myWord : myWords) {
            start = System.currentTimeMillis();
            ArrayList<Word> words = resolver.findWords(myWord);
            end = System.currentTimeMillis();
            if (words.isEmpty()) {
                System.err.println("NOT FOUND: " + myWord);
            } else {
                System.out.println("FOUND IN " + (end - start) + " ms:");
                for (Word word : words) {
                    System.out.println('\t' + word.getWord() + " - " + word.getType());
                }
            }
        }
        start = System.currentTimeMillis();
        Node wordNode = resolver.findWord("nem", Word.WordType.ADVERB);
        end = System.currentTimeMillis();
        if (wordNode != null) {
            System.out.println("'nem' ID: " + wordNode.getId() + " | time: " + (end - start));
        } else {
            System.err.println("NOT FOUND: nem");
        }
        start = System.currentTimeMillis();
        wordNode = resolver.findWord("szép", Word.WordType.ADJECTIVE);
        end = System.currentTimeMillis();
        if (wordNode != null) {
            System.out.println("'szép' ID: " + wordNode.getId() + " | time: " + (end - start));
        } else {
            System.err.println("NOT FOUND: szép");
        }

        //resolver.findWordsOrderByScore("+fogad*");
        //resolver.getAllNodes();
        // teszt a szó lebontására
        String word = "kanalával";
        StringBuilder sb = new StringBuilder(word);
        IndexHits<Node> hits = null;
        while ((hits = resolver.getWordsStartingWith(sb.toString())) == null) {
            sb.deleteCharAt(sb.length() - 1);
            if (sb.length() == 0) {
                System.err.println("'" + word + "' cannot be found.");
                break;
            }
        }

        if (hits != null) {
            hits.close();
            hits = resolver.getWordsWithFuzzy(sb.toString());
            System.out.println("--- FOUND FOR '" + sb.toString() + "' ---");
            for (Node hit : hits) {
                Word w = new Word(hit);
                System.out.println(w.getWord() + " | " + w.getType() + " | " + hits.currentScore());
            }
            hits.close();
        }
        System.out.println("NO ACCENT: " + Utils.removeAccentMarks(";ÁőÖ21üŰú-"));
        System.out.println("ABC ONLY: " + Utils.removePunctuation(";ÁőÖ21üŰú-"));
    }

    private void wordTester() {
        String data = null;
        File file = new File("word_fog.txt");
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] bytes = new byte[(int) file.length()];

            for (int j = 0; j < file.length(); j++) {
                bytes[j] = raf.readByte();
            }
            data = new String(bytes, "UTF-8");
            //System.out.println(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (data != null) {
            PageData page = new PageData();
            page.setNamespace("0");
            page.setTitle("fog");
            page.setText(data);

            PageDataParser parser = new PageDataParser(page);
            switch (parser.getType()) {
                case WORD:
                    System.out.println("IT'S A WORD!\nParsing...\n");
                    List<Word> words = parser.parseWord();
                    for (Word word : words) {
                        //resolver.addWord(word);

                        System.out.println(word.getWord() + " - " + word.getType().name());
                        System.out.println("- SYN");
                        ArrayList<Synonym> synonyms = word.getSynonyms();
                        for (Synonym synonym : synonyms) {
                            System.out.println("\t" + synonym.getWord());
                        }

                        ArrayList<Coverb> coverbs = word.getCoverbs();
                        if (coverbs != null) {
                            System.out.println("- CO");
                            for (Coverb coverb : coverbs) {
                                System.out.println("\t" + coverb.getWord() + word.getWord());
                            }
                        }
                    }

                    break;
            }
        }
    }

    private void startDatabase() {
        isWordDbReady = new File(DB_PATH).exists();

        GraphDatabaseBuilder graphDbBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(DB_PATH);

        //Map<String, String> dbConfig = new HashMap<>();
//        String nodeIndices = DetailConstants.PROP_KEY_OBJECT_ID + ",";
//        nodeIndices += DetailConstants.PROP_KEY_TYPE + ",";
//        nodeIndices += DetailConstants.PROP_KEY_WORD_TYPE + ",";
        String nodeIndices = "";
        nodeIndices += DetailConstants.PROP_KEY_G_ID + ",";
        nodeIndices += DetailConstants.PROP_KEY_G_INDEX + ",";

        nodeIndices += DetailConstants.PROP_KEY_E_VALUE + ",";
        nodeIndices += DetailConstants.PROP_KEY_E_NEUTRAL;

        //String relIndices = DetailConstants.PROP_;
        graphDb = graphDbBuilder.setConfig(GraphDatabaseSettings.node_keys_indexable, nodeIndices).
                setConfig(GraphDatabaseSettings.relationship_keys_indexable, "chance").
                setConfig(GraphDatabaseSettings.node_auto_indexing, "true").
                setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").
                newGraphDatabase();

        //graphDbBuilder.setConfig(dbConfig);
        registerShutdownHook(graphDb);

        //graphDb.index().getNodeAutoIndexer().getAutoIndex();
        resolver = new NodeResolver(graphDb);
    }

    private void initDatabaseFromXML() {
        File file = new File("huwiktionary-20131019-pages-articles.xml");
        if (file.exists()) {
            XMLParser parser = new XMLParser(this);
            parser.parse(file);
        } else {
            System.out.println("Could not find the XML in " + System.getProperty("user.dir"));
            JOptionPane.showMessageDialog(rootPane, "Could not find the XML in " + System.getProperty("user.dir"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    int i = 0;

    @Override
    public void onPageDataAvailable(PageData page) {
        i++;
        if (i % 40 == 0) {
            System.out.println("Processed words: " + i + " | current: " + page.getTitle());
        }

        PageDataParser parser = new PageDataParser(page);
        switch (parser.getType()) {
            case WORD:
                //System.out.println("IT'S A WORD!\nParsing...\n");
                List<Word> words = parser.parseWord();
                for (Word word : words) {
                    resolver.addWord(word);

//                    System.out.println(word.getWord() + " - " + word.getType().name());
//                    ArrayList<Synonym> synonyms = word.getSynonyms();
//                    for (Synonym synonym : synonyms) {
//                        System.out.println("\t" + synonym.getWord());
//                    }
                }
                break;
            case CATEGORY:
//                Category cat = parser.parseCategory();
//                System.out.println(cat.getTitle());
//                ArrayList<Category> cats = cat.getLinkedCategories();
//                if(cats != null){
//                    for(Category c : cats){
//                        System.out.println("\t" + c.getTitle());
//                    }
//                }
                resolver.addCategory(parser.parseCategory());
                break;
        }

        if (i % 40 == 0) {
            resolver.endTransaction();
            resolver.beginTransaction();
        }

//        if (i % 1000 == 0) {
//            System.out.println("Processed words: " + i + " | current: " + page.getTitle());
//            if (i == 27000) {
//                System.out.println(page.getText());
//            }
//        }
//
//        if (page.getTitle().equalsIgnoreCase("fog") || page.getTitle().equalsIgnoreCase("bubifrizura")) {
//            System.out.println("- TITLE: " + page.getTitle());
//            System.out.println("- TEXT: " + page.getText());
//            System.out.println("-------");
//        }
    }

    @Override
    public void onXMLParsingFinished() {
        System.out.println("### END OF XML PROCESSING ###");
        System.out.println("Total words processed: " + i);
    }

    private void initDatabaseFromCustomFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("custom_expressions.js"))) {
            Gson gson = new Gson();
            Group[] groups = gson.fromJson(br, Group[].class);

            for (Group group : groups) {
                System.out.println(group.getId());
                resolver.addGroup(group);
//                if(group.getExpressions() != null){
//                    for(Expression exp : group.getExpressions()){
//                        System.out.println("-- " + exp.getValue() + " | " + exp.getNeutral());
//                    }
//                }
            }
            
            File dir = new File(DynamicCompiler.COMPILATION_PATH);
            if(!dir.exists()){
                dir.mkdir();
            }
            DynamicCompiler.compile(groups);

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String data = "abba- agyon- alá- alább- által- alul- át- be- bele- benn- egybe- el- ellen- elő- előre- fel- föl- félbe- félre- felül- fölül- fenn- fönn- hátra- haza- helyre- hozzá- ide- jóvá- keresztül- ketté- ki- kölcsön- körbe- körül- közbe- közre- külön- le- létre- meg- mellé- neki- oda- össze- rá- rajta- széjjel- szembe- szerte- szét- tele- tova- tovább- tönkre- túl- újjá- újra- utána- végbe- végig- vissza-";
//        String[] datas = data.split(" ");
//        System.out.println("String[] coverbs = new String[]{");
//        for(int i = 0; i < datas.length; i++){
//            datas[i] = datas[i].substring(0, datas[i].length()-1);
//            System.out.print("\""+datas[i]+"\"");
//            if(i < datas.length-1){
//                System.out.println(',');
//            }else{
//                System.out.println();
//            }
//        }
//        System.out.println("};");
//        
        TalkR talkr = new TalkR();
        talkr.initWindow();
        talkr.initData();
    }
}
