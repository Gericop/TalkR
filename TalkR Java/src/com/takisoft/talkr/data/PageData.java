package com.takisoft.talkr.data;

/**
 *
 * @author Gericop
 */
public class PageData {

    public static final int NS_DEFAULT = 0;
    public static final int NS_CATEGORY = 14;
    public static final String TAG_PAGE = "page";
    public static final String TAG_PAGE_NS = "ns";
    public static final String TAG_PAGE_TITLE = "title";
    public static final String TAG_PAGE_REVISION = "revision";
    public static final String TAG_PAGE_REVISION_TEXT = "text";
    private String title;
    private String text;
    private int namespace = -100;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    boolean checkText() {
//        if(text != null && text.contains("Kategória:magyar")){
//            return true;
//        }

        if (namespace == NS_DEFAULT && text != null && text.contains("{{hun")) {
            return true;
        }

        return false;
    }

    public int getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespaceStr) {
        try {
            this.namespace = Integer.parseInt(namespaceStr);
        } catch (NumberFormatException e) {
            System.err.println(e);
        }
    }
}
