package com.takisoft.talkr.helper;

import com.takisoft.talkr.data.Antonym;
import com.takisoft.talkr.data.Category;
import com.takisoft.talkr.data.Coverb;
import com.takisoft.talkr.data.DetailConstants;
import com.takisoft.talkr.data.DetailConstants.RelTypes;
import com.takisoft.talkr.data.Synonym;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.data.Word.WordType;
import com.takisoft.talkr.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.QueryContext;

/**
 *
 * @author Gericop
 */
public class NodeResolver {

    private final GraphDatabaseService graphDb;
    private Transaction tx;
    private Index<Node> indexWords;
    private Index<Node> indexWordsWoAccentMark;

    public NodeResolver(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        createFullTextIndex();
        createAccentMarklessIndex();
    }

    private void createFullTextIndex() {
        IndexManager index = graphDb.index();
        indexWords = index.forNodes("words-fulltext",
                MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext"));
    }

    private void createAccentMarklessIndex() {
        IndexManager index = graphDb.index();
        indexWordsWoAccentMark = index.forNodes("words-wo-accentmark",
                MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext"));
    }

    public void beginTransaction() {
        tx = graphDb.beginTx();
        //System.out.println("### Transaction START ###");
    }

    public void endTransaction() {
        tx.success();
        tx.finish();
        tx = null;
        //System.out.println("### Transaction END ###");
    }

    public void addWord(Word word) {
        if (tx == null) {
            throw new IllegalStateException("Must be in a transaction!");
        }

        Node node = findWord(word.getWord(), word.getType());
        if (node == null) {
            try {
                node = graphDb.createNode();

                node.setProperty(DetailConstants.PROP_KEY_OBJECT_ID, word.getWord());
                node.setProperty(DetailConstants.PROP_KEY_TYPE, DetailConstants.PROP_TYPE_WORD);
                node.setProperty(DetailConstants.PROP_KEY_WORD_TYPE, word.getType().name());

                addNodeToFullTextIndex(node, word.getWord());
                //tx.success();
            } catch (Exception e) {
                System.err.println(e);
                tx.failure();
            } finally {
                //tx.finish();
            }
        }
        if (node == null) {
            return;
        }

        ArrayList<Synonym> synonyms = word.getSynonyms();
        if (!synonyms.isEmpty()) {
            for (Synonym synonym : synonyms) {
                addSynonym(node, synonym, word.getType());
            }
        }

        ArrayList<Antonym> antonyms = word.getAntonyms();
        if (!antonyms.isEmpty()) {
            for (Antonym antonym : antonyms) {
                addAntonym(node, antonym, word.getType());
            }
        }

        ArrayList<Coverb> coverbs = word.getCoverbs();
        if (coverbs != null && !coverbs.isEmpty()) {
            for (Coverb coverb : coverbs) {
                addCoverb(node, coverb);
            }
        }

        ArrayList<Category> categories = word.getCategories();
        if (categories != null && !categories.isEmpty()) {
            for (Category category : categories) {
                Node catNode = addCategory(category);
                Relationship rel = node.createRelationshipTo(catNode, RelTypes.LINKED);
                // TODO relationship property kell?
            }
        }
    }

    public void addSynonym(Node parent, Synonym word, WordType type) {
        if (tx == null) {
            throw new IllegalStateException("Must be in a transaction!");
        }

        try {
            Node node = findWord(word.getWord(), type);

            if (node == null) {
                node = graphDb.createNode();

                node.setProperty(DetailConstants.PROP_KEY_OBJECT_ID, word.getWord());
                node.setProperty(DetailConstants.PROP_KEY_TYPE, DetailConstants.PROP_TYPE_WORD);
                node.setProperty(DetailConstants.PROP_KEY_WORD_TYPE, type.name());

                addNodeToFullTextIndex(node, word.getWord());
            }

            if (!existsRelationship(parent, node, RelTypes.SYNONYM)) {
                Relationship rel = parent.createRelationshipTo(node, RelTypes.SYNONYM);
                // TODO kell valami property a kapcsolathoz?
            }
            //tx.success();
        } catch (Exception e) {
            System.err.println(e);
            tx.failure();
        } finally {
            //tx.finish();
        }
    }

    public void addAntonym(Node parent, Antonym word, WordType type) {
        if (tx == null) {
            throw new IllegalStateException("Must be in a transaction!");
        }

        try {
            Node node = findWord(word.getWord(), type);

            if (node == null) {
                node = graphDb.createNode();

                node.setProperty(DetailConstants.PROP_KEY_OBJECT_ID, word.getWord());
                node.setProperty(DetailConstants.PROP_KEY_TYPE, DetailConstants.PROP_TYPE_WORD);
                node.setProperty(DetailConstants.PROP_KEY_WORD_TYPE, type.name());

                addNodeToFullTextIndex(node, word.getWord());
            }

            if (!existsRelationship(parent, node, RelTypes.ANTONYM)) {
                Relationship rel = parent.createRelationshipTo(node, RelTypes.ANTONYM);
                // TODO kell valami property a kapcsolathoz?
            }
            //tx.success();
        } catch (Exception e) {
            System.err.println(e);
            tx.failure();
        } finally {
            //tx.finish();
        }
    }

    public void addCoverb(Node parent, Coverb word) {
        if (tx == null) {
            throw new IllegalStateException("Must be in a transaction!");
        }

        try {
            Node node = findWord(word.getWord(), WordType.VERB);

            if (node == null) {
                node = graphDb.createNode();

                node.setProperty(DetailConstants.PROP_KEY_OBJECT_ID, word.getWord());
                node.setProperty(DetailConstants.PROP_KEY_TYPE, DetailConstants.PROP_TYPE_WORD);
                node.setProperty(DetailConstants.PROP_KEY_WORD_TYPE, WordType.VERB.name());

                addNodeToFullTextIndex(node, word.getWord());
            }

            if (!existsRelationship(parent, node, RelTypes.COVERB)) {
                Relationship rel = parent.createRelationshipTo(node, RelTypes.COVERB);
                // TODO kell valami property a kapcsolathoz?
            }
            //tx.success();
        } catch (Exception e) {
            System.err.println(e);
            tx.failure();
        } finally {
            //tx.finish();
        }
    }

    public Node addCategory(Category category) {
        if (tx == null) {
            throw new IllegalStateException("Must be in a transaction!");
        }

        Node node = findCategory(category.getTitle());

        if (node == null) {
            try {
                node = graphDb.createNode();

                node.setProperty(DetailConstants.PROP_KEY_OBJECT_ID, category.getTitle());
                node.setProperty(DetailConstants.PROP_KEY_TYPE, DetailConstants.PROP_TYPE_CAT);
            } catch (Exception e) {
                tx.failure();
            }
        }

        ArrayList<Category> linkedCategories = category.getLinkedCategories();

        if (linkedCategories != null) {
            for (Category linkedCat : linkedCategories) {
                Node otherNode = addCategory(linkedCat);
                if (node != null && !existsRelationship(node, otherNode, RelTypes.LINKED)) {
                    Relationship rel = node.createRelationshipTo(otherNode, RelTypes.LINKED);
                    // TODO kell valami property a kapcsolathoz?
                }
            }
        }

        return node;
    }

    public boolean existsRelationship(Node a, Node b, RelationshipType type) {
        if (a.hasRelationship(type) && b.hasRelationship(type)) {
            Iterable<Relationship> relationships = a.getRelationships();
            for (Relationship rel : relationships) {
                if (rel.getOtherNode(a).equals(b)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Node findWord(String word, WordType type) {
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        IndexHits<Node> nodes = autoNodeIndex.get(DetailConstants.PROP_KEY_OBJECT_ID, word);

        for (Node node : nodes) {
            if (DetailConstants.PROP_TYPE_WORD.equals(node.getProperty(DetailConstants.PROP_KEY_TYPE))) {
                if (type.name().equals(node.getProperty(DetailConstants.PROP_KEY_WORD_TYPE))) {
                    return node;
                }
            }
        }

        return null;
    }

    public ArrayList<Word> findWords(String word) {
        ArrayList<Word> words = new ArrayList<>();
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        IndexHits<Node> nodes = autoNodeIndex.get(DetailConstants.PROP_KEY_OBJECT_ID, word);

        for (Node node : nodes) {
            if (DetailConstants.PROP_TYPE_WORD.equals(node.getProperty(DetailConstants.PROP_KEY_TYPE))) {
                words.add(new Word(node));
            }
        }

        return words;
    }

    public Node findCategory(String category) {
        ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        IndexHits<Node> nodes = autoNodeIndex.get(DetailConstants.PROP_KEY_OBJECT_ID, category);
        for (Node node : nodes) {
            if (DetailConstants.PROP_TYPE_CAT.equals(node.getProperty(DetailConstants.PROP_KEY_TYPE))) {
                return node;
            }
        }

        return null;
    }

    private void addNodeToFullTextIndex(Node node, String index) {
        if (indexWords == null) {
            return;
        }

        indexWords.add(node, DetailConstants.PROP_KEY_OBJECT_ID, index);

        addNodeToAccentMarklessIndex(node, index);
    }

    private void addNodeToAccentMarklessIndex(Node node, String index) {
        if (indexWordsWoAccentMark == null) {
            return;
        }

        indexWordsWoAccentMark.add(node, DetailConstants.PROP_KEY_OBJECT_ID, Utils.removeAccentMarks(index));
    }

    public void findWordsOrderByScore(String word) {
        if (indexWords == null) {
            return;
        }

        IndexHits<Node> hits = indexWords.query(DetailConstants.PROP_KEY_OBJECT_ID, new QueryContext(word + "*").sortByScore());
        Iterator<Node> iter = hits.iterator();

        if (!iter.hasNext()) {
            System.err.println(word + " not found...");
        }

        while (iter.hasNext()) {
            // hits sorted by relevance (score)
            System.out.println(new Word(iter.next()).getWord() + " | " + hits.currentScore());
        }
    }

    // TODO csak teszt
    public void getAllNodes() {
        IndexHits<Node> hits = indexWords.query("*:*");
        int i = 0;
        for (Node hit : hits) {
            i++;
        }
        hits.close();

        System.out.println("Nodes: " + i);
    }

    public IndexHits<Node> getWordsStartingWith(String start) {
        IndexHits<Node> hits = indexWords.query(DetailConstants.PROP_KEY_OBJECT_ID, start + "*");

        if (!hits.hasNext()) {
            return null;
        }

        return hits;
    }
    
    public IndexHits<Node> getWordsWithFuzzy(String start) {
        IndexHits<Node> hits = indexWords.query(DetailConstants.PROP_KEY_OBJECT_ID, start + "~");

        if (!hits.hasNext()) {
            return null;
        }

        return hits;
    }
    
    public IndexHits<Node> getWordsWithFuzzy(String start, double number) {
        System.out.println(">> "+ start + "~" + number);
        IndexHits<Node> hits = indexWords.query(DetailConstants.PROP_KEY_OBJECT_ID, start + "~" + number);

        if (!hits.hasNext()) {
            return null;
        }

        return hits;
    }
}
