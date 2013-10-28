package com.takisoft.talkr.data;

import java.util.ArrayList;

/**
 *
 * @author Gericop
 */
public class PageDataParser {

    public static enum DataType {

        NO_MATCH, WORD, CATEGORY
    };
    
    PageData data;
    
    public PageDataParser(PageData data){
        this.data = data;
    }

    public DataType getType() {
        switch (data.getNamespace()) {
            case DetailConstants.NS_DEFAULT:
                return DataType.WORD;
            case DetailConstants.NS_CATEGORY:
                return DataType.CATEGORY;
        }
        return DataType.NO_MATCH;
    }

    public ArrayList<Word> parseWord() {
        return Word.getWords(data);
    }
    
    public Category parseCategory(){
        return Category.getCategories(data);
    }
}
