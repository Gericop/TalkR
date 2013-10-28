package com.takisoft.talkr.data;

/**
 *
 * @author Gericop
 */
public class PageData {

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
        if (this.title == null) {
            this.title = title;
        } else {
            this.title = this.title + title;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (this.text == null) {
            this.text = text;
        } else {
            this.text = this.text + text;
        }
    }

    boolean checkText() {
        if ((namespace == DetailConstants.NS_DEFAULT && text != null && text.contains("{{hun")) ||
                (namespace == DetailConstants.NS_CATEGORY && text != null && title != null && title.contains(":hu:"))) {
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

    public boolean checkNamespace() {
        if (namespace == DetailConstants.NS_DEFAULT || namespace == DetailConstants.NS_CATEGORY) {
            return true;
        }

        return false;
    }
}
