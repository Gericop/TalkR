package com.takisoft.talkr.data;

import org.neo4j.graphdb.RelationshipType;

/**
 * Constants that occur in the descriptions of the words.
 * @author Gericop
 */
public class DetailConstants {

    public enum RelTypes implements RelationshipType {

        SYNONYM, ANTONYM, LINKED, COVERB, GROUPED
    }
    // PARENTHESIS
    public static final String P_START = "\\{\\{";
    public static final String P_END = "\\}\\}";
    // NAMESPACES
    public static final int NS_DEFAULT = 0;
    public static final int NS_CATEGORY = 14;
    // TYPES
    public static final String TYPE_NOUN = "\\{\\{hunfn.*\\}\\}";
    public static final String TYPE_PRE = "\\{\\{hunpre.*\\}\\}";
    public static final String TYPE_VERB = "\\{\\{hunige.*\\}\\}";
    //public static final String TYPE_VERB2 = "\\{\\{hunige2\\}\\}";
    public static final String TYPE_ADJECT = "\\{\\{hunmell.*\\}\\}";
    public static final String TYPE_ADVERB = "\\{\\{hunhat.*\\}\\}";
    public static final String TYPE_GEO = "\\{\\{hungeo.*\\}\\}";
    public static final String TYPE_PREFIX = "\\{\\{hunnéve.*\\}\\}";
    public static final String TYPE_NUMERAL = "\\{\\{hunszn.*\\}\\}";
    public static final String TYPE_EMOTION = "\\{\\{hunisz.*\\}\\}";
    public static final String TYPE_CONJ = "\\{\\{hunksz.*\\}\\}";
    public static final String TYPE_PROP = "\\{\\{hunprop.*\\}\\}";
    /**
     * x {{hunszn}} - számnév
     * x {{hunisz}} - indulatszó
     * x {{hunksz}} vagy {{hunksz2}} - kötőszó
     * {{hunfmell}} - fokozott melléknév?
     * {{hunsuf}} - toldalék
     * {{huntsi}} - tárgyas ige
     * {{hunkif* - kifejezések pl.: {{hunkif|fej=[[a]] [[fej]]ét [[ráz]]za}}
     * {{hunkapcs* - szókapcsolatok pl.: {{hunkapcs|fej=[[családi]] [[vállalkozás]]}}
     * {{hunnm}} - névmás
     * {{hunkérd}} - kérdő névmás
     */
    // INNER TYPES
    public static final String INTYPE_SYN = "\\{\\{hunsyn\\}\\}";
    public static final String INTYPE_ANT = "\\{\\{hunant\\}\\}";
    public static final String INTYPE_END = "\\{\\{(?!rel-)"; // az olyanokat találja meg, amik nem "{{rel-"-lel kezdődnek
    
    // CAT PRE
    public static final String CAT_GENERAL_PREFIX_SUB = "Kategória:hu:";
    public static final String CAT_GENERAL_PREFIX = "\\[\\[Kategória:hu:.*\\]\\]";
    // MISC
    public static final String MISC_COVERB = "(?s)\\{\\{igekot.*?(?=\\}\\})";
    public static final String MISC_COVERB_WORD = "(?!\\|.*?)[\\wíóöúüőűáé]+(?=.*?=.*?\\+)";
    public static final String MISC_DERIVATIVES = "\\{\\{-der-\\}\\}";
    public static final String MISC_WORDS = "(?!\\[\\[)[\\w\\síóöúüőűáé]+(?=\\]\\])";
    //public static final String MISC_WORDS = "\\[\\[[\\w\\síóöúüőűáé]+\\]\\]";
    // PROPERTY KEYS
    public static final String PROP_KEY_OBJECT_ID = "objectId";
    public static final String PROP_KEY_TYPE = "type";
    public static final String PROP_KEY_WORD_TYPE = "wordType"; // szófaj
    // PROPERTY VALUES
    public static final String PROP_TYPE_WORD = "word";
    public static final String PROP_TYPE_CAT = "category";
    
    
    // PROPERTY GROUP and EXPRESSION
    public static final String PROP_TYPE_GROUP = "group";
    public static final String PROP_KEY_G_RESPONSE = "response";
    public static final String PROP_KEY_G_INDEX = "index";
    public static final String PROP_KEY_G_ID = "groupId";
    
    public static final String PROP_TYPE_EXPRESSION = "expression";
    public static final String PROP_KEY_E_VALUE = "value";
    public static final String PROP_KEY_E_NEUTRAL = "neutral";
}
