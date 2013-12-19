package com.takisoft.talkr.analyzer;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Gericop
 */
public class RobotLife {

    String name = "Robo Róbert";
    String nick = "Mikrobi";
    Date dob;
    String location = "számítógép";

    SecureRandom random = new SecureRandom();

    String[][] moods = new String[][]{
        {"zsírul", "jól", "fantasztikusan", "csodálatosan", "faszán"}, // VERY GOOD
        {"megvagyok", "elvagyok", "okésan"}, // OKAY
        {"rosszul", "szomorúan", "szomorú vagyok"}, // SAD
        {"idegesítesz", "hagyjál"}
    };
    int moodIndex = 0;

    public RobotLife() {
        Calendar cal = Calendar.getInstance();
        cal.set(2013, Calendar.SEPTEMBER, 9);
        dob = cal.getTime();
    }

    public String getCurrentMood() {

        return moods[moodIndex][random.nextInt(moods[moodIndex].length)];
    }
    
    public String[][] getMoods(){
        return moods;
    }

    public String getAge() {
        Date date = new Date(System.currentTimeMillis() - dob.getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        if (year > 0) {
            int month = cal.get(Calendar.MONTH);
            if (month > 0) {
                return month + " hónap";
            } else {
                return cal.get(Calendar.DAY_OF_MONTH) + " nap";
            }
        } else {
            return year + " év";
        }
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }
}
