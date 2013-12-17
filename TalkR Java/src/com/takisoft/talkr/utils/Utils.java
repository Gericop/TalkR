package com.takisoft.talkr.utils;

/**
 *
 * @author Gericop
 */
public class Utils {

    private final static String[] accentMarks = new String[]{
        "áéíóöőúüű",
        "aeiooouuu"
    };

    public static String removeAccentMarks(String word) {
        char[] oldChs = accentMarks[0].toCharArray();
        char[] newChs = accentMarks[1].toCharArray();

        for (int i = 0; i < oldChs.length; i++) {
            word = word.replace(oldChs[i], newChs[i]);
        }

        oldChs = accentMarks[0].toUpperCase().toCharArray();
        newChs = accentMarks[1].toUpperCase().toCharArray();

        for (int i = 0; i < oldChs.length; i++) {
            word = word.replace(oldChs[i], newChs[i]);
        }

        return word;
    }
    
    public static String removePunctuation(String word){
        return word.replaceAll("[^a-zA-ZíóöúüőűáéÍÓÖÚÜŐŰÁÉ]", "");
    }
}
