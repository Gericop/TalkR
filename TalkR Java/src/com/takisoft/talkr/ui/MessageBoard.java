package com.takisoft.talkr.ui;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author RedMax
 */
public class MessageBoard extends JPanel {

    private static final int MAX_MESSAGES = 30;

    public MessageBoard() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public Component add(Component comp) {
        if (MAX_MESSAGES <= this.getComponentCount()) {
            remove(0);
            invalidate();
        }
        Component c = super.add(comp);
        
        Rectangle s = getBounds(null);
        
        Rectangle r = new Rectangle(0, s.height, s.width, s.height);
        scrollRectToVisible(r);
        
        return c;
    }
}
