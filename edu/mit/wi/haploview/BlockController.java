package edu.mit.wi.haploview;

import javax.swing.*;

public class BlockController extends JPanel{

    HaploData theData;
    BlockDisplay bd;

    public BlockController(HaploData h){
        theData = h;
        bd = new BlockDisplay(theData);
        //add(new JButton("Foo"));
        add(bd);
    }


}
