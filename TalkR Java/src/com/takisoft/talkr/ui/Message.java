package com.takisoft.talkr.ui;

import com.takisoft.talkr.TalkR;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 *
 * @author RedMax
 */
public class Message extends JPanel {

    public static enum Who {

        HUMAN, ROBOT
    }
    Who who;
    String message;
    JLabel labelName, labelMessage;

    public Message() {
        labelName = new JLabel();
        labelMessage = new JLabel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //add(labelName);
        add(labelMessage);
    }

    public Message(Who who, String message) {
        this();
        setWho(who);
        setMessage(message);
    }

    public final void setWho(Who who) {
        this.who = who;

        if (who == Who.HUMAN) {
//            labelName.setText("HUMAN");
//            labelName.setHorizontalAlignment(SwingConstants.LEFT);
//            labelName.setAlignmentX(Component.LEFT_ALIGNMENT);
            setBackground(Color.WHITE);
            //setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.red));
            TitledBorder title;
            title = BorderFactory.createTitledBorder("HUMAN");
            setBorder(title);
        } else {
//            labelName.setText("ROBOT");
//            labelName.setHorizontalAlignment(SwingConstants.RIGHT);
//            labelName.setAlignmentX(Component.RIGHT_ALIGNMENT);
//            setBackground(Color.LIGHT_GRAY);

//            labelName.setText("ROBOT");
//            labelName.setHorizontalAlignment(SwingConstants.RIGHT);
//            labelName.setAlignmentX(Component.RIGHT_ALIGNMENT);
            setBackground(new Color(242, 242, 242));
            //setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.red));
            TitledBorder title;
            title = BorderFactory.createTitledBorder("ROBOT");
            setBorder(title);
        }
    }

    public final void setMessage(String message) {
        this.message = message;

        labelMessage.setText("<html><p>" + message + "</p></html>");
//        if (who == Who.HUMAN) {
//            labelMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
//        } else {
//            labelMessage.setAlignmentX(Component.RIGHT_ALIGNMENT);
//        }
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;
        //size.width = TalkR.getFrames()[0].getSize().width;
        return size;
    }
}
