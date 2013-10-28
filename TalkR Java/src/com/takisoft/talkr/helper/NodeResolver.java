package com.takisoft.talkr.helper;

import com.takisoft.talkr.data.Antonym;
import com.takisoft.talkr.data.Category;
import com.takisoft.talkr.data.Coverb;
import com.takisoft.talkr.data.DetailConstants;
import com.takisoft.talkr.data.DetailConstants.RelTypes;
import com.takisoft.talkr.data.Synonym;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.data.Word.WordType;
import java.util.ArrayList;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.ReadableIndex;

/**
 *
 * @author Gericop
 */
public class NodeResolver {

    private GraphDatabaseService graphDb;
    private Transaction tx;

    public NodeResolver(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
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
    
    public void addCoverb(Node parent, Coverb word){
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
                if (!existsRelationship(node, otherNode, RelTypes.LINKED)) {
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
}
