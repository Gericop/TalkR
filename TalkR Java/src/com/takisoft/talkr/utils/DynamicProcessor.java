package com.takisoft.talkr.utils;

import com.takisoft.talkr.ai.Group;
import com.takisoft.talkr.ai.Expression;
import com.takisoft.talkr.data.Antonym;
import com.takisoft.talkr.data.Word;
import com.takisoft.talkr.data.Synonym;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Gericop
 */
public class DynamicProcessor {

    private final DynamicHelper helper;

    public DynamicProcessor(DynamicHelper helper) {
        Matcher matcher;
        Pattern pattern;
        this.helper = helper;
    }

    public Expression findExpression(String exp) {
        return helper.findExpression(exp);
    }

    public Group findGroup(String groupId) {
        return helper.findGroup(groupId);
    }

    public Group findGroup(int groupIndex) {
        return helper.findGroup(groupIndex);
    }

    public Word findName(String name) {
        return helper.findName(name);
    }

    public void setName(String name) {
        helper.getHuman().setName(name);
    }

    public Word findPlace(String name) {
        return helper.findPlace(name);
    }

    public void setLocation(String name) {
        helper.getHuman().setLocation(name);
    }

    public void setAge(int age) {
        helper.getHuman().setAge(age);
    }

    public ArrayList<Synonym> findSynonyms(String word) {
        return helper.findSynonyms(word);
    }

    public ArrayList<Antonym> findAntonyms(String word) {
        return helper.findAntonyms(word);
    }

    public String getMood() {
        return helper.getRobot().getCurrentMood();
    }

    public String getAge() {
        return helper.getRobot().getAge();
    }

    public String getName() {
        return helper.getRobot().getName();
    }

    public String getNick() {
        return helper.getRobot().getNick();
    }

    public String getLocation() {
        return helper.getRobot().getLocation();
    }
}
