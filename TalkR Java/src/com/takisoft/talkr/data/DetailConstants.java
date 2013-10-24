package com.takisoft.talkr.data;

/**
 * Constants that occur in the descriptions of the words.
 * @author Gericop
 */
public class DetailConstants {

    // TYPES
    public static final String TYPE_NOUN = "\\{\\{hunfn\\}\\}";
    public static final String TYPE_PRE = "\\{\\{hunpre\\}\\}";
    public static final String TYPE_VERB = "\\{\\{hunige\\}\\}";
    public static final String TYPE_VERB2 = "\\{\\{hunige2\\}\\}";
    public static final String TYPE_ADJECT = "\\{\\{hunmell\\}\\}";
    public static final String TYPE_ADVERB = "\\{\\{hunhat\\}\\}";
    public static final String TYPE_GEO = "\\{\\{hungeo.*\\}\\}";
    public static final String TYPE_SYN = "\\{\\{hunsyn\\}\\}";
    public static final String TYPE_PREFIX = "\\{\\{hunnéve\\}\\}";
    /**
     * {{hunisz}} - indulatszó
     * {{hunksz}} - kötőszó
     * {{hunkif* - kifejezések pl.: {{hunkif|fej=[[a]] [[fej]]ét [[ráz]]za}}
     * {{hunkapcs* - szókapcsolatok pl.: {{hunkapcs|fej=[[családi]] [[vállalkozás]]}}
     */
    
    // CAT PRE
    public static final String CAT_GENERAL_PREFIX = "\\[\\[Kategória:hu:[^\\n]*\\]\\]";
    
    // MISC
    public static final String MISC_VERB_BINDINGS = "\\{\\{igekot.*\\}\\}";
    public static final String MISC_DERIVATIVES = "\\{\\{-der-\\}\\}";
}
