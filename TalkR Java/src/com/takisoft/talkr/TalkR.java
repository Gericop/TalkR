package com.takisoft.talkr;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * The Hungarian chatter bot.
 * @author Gericop
 */
public class TalkR extends JFrame {

    boolean isWordDbReady = false;

    public TalkR() {
    }

    public void initWindow() {
        setTitle("TalkR v0.1");
        setSize(640, 480);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void initData() {
        if (!isWordDbReady) {
            initDatabaseFromXML();
        }
    }

    private void initDatabaseFromXML() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TalkR talkr = new TalkR();
        talkr.initWindow();
        talkr.initData();
    }
}
