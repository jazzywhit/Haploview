package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.*;

public class HaploviewTab extends JPanel{

    public HaploviewTab(Component c){
        this.add(c);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    }
}
