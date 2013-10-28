package com.takisoft.talkr.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Gericop
 */
public class Word {

    public static enum WordType {

        UNKNOWN(null),
        NOUN(DetailConstants.TYPE_NOUN),
        VERB(DetailConstants.TYPE_VERB),
        ADVERB(DetailConstants.TYPE_ADVERB),
        ADJECTIVE(DetailConstants.TYPE_ADJECT),
        PREFIX(DetailConstants.TYPE_PREFIX),
        GEO(DetailConstants.TYPE_GEO),
        NUMERAL(DetailConstants.TYPE_NUMERAL),
        EMOTION(DetailConstants.TYPE_EMOTION),
        CONJUNCTION(DetailConstants.TYPE_CONJ),
        PROPERTY(DetailConstants.TYPE_PROP);
        private final String regex;

        WordType(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }
    String wordString;
    ArrayList<Synonym> synonyms;
    private ArrayList<Antonym> antonyms;
    private ArrayList<Coverb> coverbs;
    private ArrayList<Category> categories;
    WordType type = WordType.UNKNOWN;

    public Word(Node node) {
        wordString = (String) node.getProperty(DetailConstants.PROP_KEY_OBJECT_ID);
        type = WordType.valueOf((String) node.getProperty(DetailConstants.PROP_KEY_WORD_TYPE));
    }

    private Word() {
        this(null, WordType.UNKNOWN);
    }

    private Word(String wordString) {
        this(wordString, WordType.UNKNOWN);
    }

    private Word(String wordString, WordType type) {
        this.wordString = wordString;
        this.type = type;
    }

    public static ArrayList<Word> getWords(PageData data) {
        ArrayList<Word> words = new ArrayList<>();
        ArrayList<RegionHolder> regions = new ArrayList<>();

        Matcher matcher = null;
        Pattern pattern;

        for (WordType wordType : WordType.values()) {
            if (wordType == WordType.UNKNOWN) {
                continue;
            }

            pattern = Pattern.compile(wordType.getRegex(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            if (matcher == null) {
                matcher = pattern.matcher(data.getText());
            } else {
                matcher.usePattern(pattern);
                matcher.reset();
            }

            if (matcher.find()) {
                regions.add(new RegionHolder(wordType, matcher.start()));
            }
        }

        regions.add(new RegionHolder(WordType.UNKNOWN, matcher.regionEnd()));

        Collections.sort(regions);

        for (int i = 0; i < regions.size() - 1; i++) {
            RegionHolder currentRegion = regions.get(i);

            Word word = new Word(data.getTitle(), currentRegion.type);

            Matcher region = matcher.region(currentRegion.start, regions.get(i + 1).start);
            //System.out.println(currentRegion.type.name() + " | " + currentRegion.start + " - " + regions.get(i + 1).start);

            ArrayList<Synonym> synonyms = searchSynonyms(region);
            word.setSynonyms(synonyms);

            // reset does not work properly
            region = matcher.region(currentRegion.start, regions.get(i + 1).start);

            ArrayList<Antonym> antonym = searchAntonyms(region);
            word.setAntonyms(antonym);

            if (word.getType() == WordType.NOUN || regions.size() == 2) {
                word.setCategories(Category.getCategoriesForWord(data.getText()));
            }

            if (word.getType() == WordType.VERB) {
                ArrayList<Coverb> coverbs = searchCoverbs(word, data.getText());
                word.setCoverbs(coverbs);
            }

            words.add(word);
        }

        return words;
    }

    private static ArrayList<Coverb> searchCoverbs(Word word, String text) {
        ArrayList<Coverb> coverbs = new ArrayList<>();
        Pattern pattern = Pattern.compile(DetailConstants.MISC_COVERB, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            pattern = Pattern.compile(DetailConstants.MISC_COVERB_WORD, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            matcher = pattern.matcher(matcher.group());

            while (matcher.find()) {
                coverbs.add(new Coverb(matcher.group() + word.getWord()));
            }
        }

        return coverbs;
    }

    private static ArrayList<Synonym> searchSynonyms(Matcher region) {
        ArrayList<Synonym> synonyms = new ArrayList<>();
        Pattern pattern = Pattern.compile(DetailConstants.INTYPE_SYN, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

        region.usePattern(pattern);

        if (region.find()) {
            //System.out.println("Found: " + DetailConstants.INTYPE_SYN);

            int start = region.start();
            int end = region.regionEnd();
            pattern = Pattern.compile(DetailConstants.P_START, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            region.usePattern(pattern);

            if (region.find()) {
                end = region.start();
                //System.out.println("Found: " + DetailConstants.P_START + " | end: " + end);
            }

            // get the words
            Matcher regionSyn = region.region(start, end);
            pattern = Pattern.compile(DetailConstants.MISC_WORDS, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            regionSyn.usePattern(pattern);

            while (regionSyn.find()) {
                String raw = regionSyn.group();
                synonyms.add(new Synonym(raw));
                //synonyms.add(new Synonym(raw.substring(raw.indexOf("[[") + 2, raw.indexOf("]]"))));
                //String word = raw.substring(raw.indexOf("[[")+2, raw.indexOf("]]"));
                //System.out.println(word);
            }
        }

        return synonyms;
    }

    private static ArrayList<Antonym> searchAntonyms(Matcher region) {
        ArrayList<Antonym> antonyms = new ArrayList<>();
        Pattern pattern = Pattern.compile(DetailConstants.INTYPE_ANT, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

        region.usePattern(pattern);

        if (region.find()) {
            //System.out.println("Found: " + DetailConstants.INTYPE_SYN);

            int start = region.start();
            int end = region.regionEnd();
            pattern = Pattern.compile(DetailConstants.P_START, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            region.usePattern(pattern);

            if (region.find()) {
                end = region.start();
                //System.out.println("Found: " + DetailConstants.P_START + " | end: " + end);
            }

            // get the words
            Matcher regionSyn = region.region(start, end);
            pattern = Pattern.compile(DetailConstants.MISC_WORDS, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            regionSyn.usePattern(pattern);

            while (regionSyn.find()) {
                String raw = regionSyn.group();
                antonyms.add(new Antonym(raw));
                //antonyms.add(new Antonym(raw.substring(raw.indexOf("[[") + 2, raw.indexOf("]]"))));
                //String word = raw.substring(raw.indexOf("[[")+2, raw.indexOf("]]"));
                //System.out.println(word);
            }
        }

        return antonyms;
    }

    public String getWord() {
        return wordString;
    }

    private void setWord(String wordString) {
        this.wordString = wordString;
    }

    public WordType getType() {
        return type;
    }

    private void setType(WordType type) {
        this.type = type;
    }

    public ArrayList<Synonym> getSynonyms() {
        return synonyms;
    }

    private void setSynonyms(ArrayList<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    public ArrayList<Antonym> getAntonyms() {
        return antonyms;
    }

    private void setAntonyms(ArrayList<Antonym> antonyms) {
        this.antonyms = antonyms;
    }

    public ArrayList<Coverb> getCoverbs() {
        return coverbs;
    }

    private void setCoverbs(ArrayList<Coverb> coverbs) {
        this.coverbs = coverbs;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    private void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    static class RegionHolder implements Comparable<RegionHolder> {

        WordType type;
        int start;

        public RegionHolder(WordType type, int start) {
            this.type = type;
            this.start = start;
        }

        @Override
        public int compareTo(RegionHolder o) {
            return this.start - o.start;
        }
    }
}
