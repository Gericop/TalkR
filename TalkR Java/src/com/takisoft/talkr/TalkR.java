package com.takisoft.talkr;

import com.takisoft.talkr.data.PageData;
import com.takisoft.talkr.data.XMLParser;
import com.takisoft.talkr.data.XMLParser.XMLParserListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * The Hungarian chatter bot.
 * @author Gericop
 */
public class TalkR extends JFrame implements XMLParserListener {

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
        File file = new File("huwiktionary-20131019-pages-articles.xml");
        if (file.exists()) {
            XMLParser parser = new XMLParser(this);
            parser.parse(file);
        } else {
            System.out.println("Could not find the XML in " + System.getProperty("user.dir"));
            JOptionPane.showMessageDialog(rootPane, "Could not find the XML in " + System.getProperty("user.dir"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    int i = 0;

    @Override
    public void onPageDataAvailable(PageData page) {
        i++;
        if (i % 1000 == 0) {
            System.out.println("Processed words: " + i + " | current: " + page.getTitle());
            if(i == 27000){
                System.out.println(page.getText());
            }
        }
    }

    @Override
    public void onXMLParsingFinished() {
        System.out.println("### END OF XML PROCESSING ###");
        System.out.println("Total words processed: " + i);
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
