package com.takisoft.talkr.data;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Gericop
 */
public class Category implements Comparable<Category> {

    private String title;
    private ArrayList<Category> linkedCategories;

    public Category(Node node) {
        title = (String) node.getProperty(DetailConstants.PROP_KEY_OBJECT_ID);
    }

    private Category(String title) {
        this(title, false);
    }

    private Category(String title, boolean removeExtra) {
        if (removeExtra) {
            this.title = title.substring(DetailConstants.CAT_GENERAL_PREFIX_SUB.length()).trim();
        } else {
            this.title = title;
        }
    }

    public static Category getCategories(PageData data) {
        Category category = new Category(data.getTitle(), true);
        Pattern pattern = Pattern.compile(DetailConstants.CAT_GENERAL_PREFIX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(data.getText().trim());

        while (matcher.find()) {
            String raw = matcher.group();
            category.addLinkedCategory(new Category(raw.substring(raw.indexOf("[[") + 2, raw.indexOf("]]")), true));
        }

        return category;
    }

    public static ArrayList<Category> getCategoriesForWord(String text) {
        ArrayList<Category> categories = new ArrayList<>();
        Pattern pattern = Pattern.compile(DetailConstants.CAT_GENERAL_PREFIX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(text.trim());

        while (matcher.find()) {
            String raw = matcher.group();
            categories.add(new Category(raw.substring(raw.indexOf("[[") + 2, raw.indexOf("]]")), true));
        }

        return categories;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Category> getLinkedCategories() {
        return linkedCategories;
    }

    private void addLinkedCategory(Category category) {
        if (linkedCategories == null) {
            linkedCategories = new ArrayList<>();
        }

        linkedCategories.add(category);
    }

    @Override
    public int compareTo(Category o) {
        if (o == this) {
            return 0;
        }

        if (o == null) {
            return 1;
        }

        return this.getTitle().compareToIgnoreCase(o.getTitle());
    }
}
