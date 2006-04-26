package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.*;

public class HaploviewTab extends JPanel{

    private Component primary;

    public HaploviewTab(){
        primary = this;
    }

    public HaploviewTab(Component primary){
        this.primary = primary;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    }

    public Component getPrimary() {
        return primary;
    }

}