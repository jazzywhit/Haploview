package edu.mit.wi.haploview;


import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.CheckData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.*;
import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;

public class HaploView extends JFrame implements ActionListener{

    boolean DEBUG = false;

    //some constants etc.
    private static final String READ_GENOTYPES = "Open genotype data";
    private static final String READ_MARKERS = "Load marker data";
    JMenuItem readMarkerItem;

    private static final String EXPORT_TEXT = "Export current tab to text";
    private static final String EXPORT_PNG = "Export current tab to PNG";
    String exportItems[] = {
        EXPORT_TEXT, EXPORT_PNG
    };
    JMenuItem exportMenuItems[];
    JMenu keyMenu;

    private static final String CLEAR_BLOCKS = "Clear all blocks";
    private static final String CUST_BLOCKS = "Customize Block Definitions";
    JMenuItem clearBlocksItem;

    static final String QUIT = "Quit";

    static final String VIEW_DPRIME = "D Prime Plot";
    static final String VIEW_HAPLOTYPES = "Haplotypes";
    static final String VIEW_GENOTYPES = "Genotype Data";
    static final String VIEW_MARKERS = "Marker Data";
    static final String VIEW_CHECK_PANEL = "Check Markers";
    static final String VIEW_TDT = "TDT";

    static final int VIEW_D_NUM = 0;
    static final int VIEW_HAP_NUM = 1;
    static final int VIEW_CHECK_NUM = 2;
    static final int VIEW_TDT_NUM = 3;

    String viewItems[] = {
        VIEW_DPRIME, VIEW_HAPLOTYPES, VIEW_CHECK_PANEL, VIEW_TDT
    };
    JRadioButtonMenuItem viewMenuItems[];
    String zoomItems[] = {
        "Zoomed", "Medium", "Unzoomed"
    };
    JRadioButtonMenuItem zoomMenuItems[];
    String colorItems[] = {
        "Standard", "Confidence bounds", "4 Gamete"
    };
    JRadioButtonMenuItem colorMenuItems[];
    JRadioButtonMenuItem blockMenuItems[];
    String blockItems[] = {"Confidence intervals (Gabriel et al)",
                                  "Four Gamete Rule",
                                  "Solid spine of LD"};

    static final int PNG_MODE = 0;
    static final int TXT_MODE = 1;

    //static final String DISPLAY_OPTIONS = "Display Options";
    //JMenuItem displayOptionsItem;

    HaploData theData;
    private CheckDataPanel checkPanel;

    private int currentBlockDef = 0;
    private TDTPanel tdtPanel;
    int assocTest = 0;
    private javax.swing.Timer timer;
    long maxCompDist;

    static HaploView window;
    JFileChooser fc;
    DPrimeDisplay dPrimeDisplay;
    private JScrollPane hapScroller;
    private HaplotypeDisplay hapDisplay;
    private JTabbedPane tabs;
    private String[] inputOptions;
    NumberTextField hwcut, genocut, mendcut;

    //COMMAND LINE ARGUMENTS
    private HaploText argParser;


    public HaploView(){
        fc = new JFileChooser(System.getProperty("user.dir"));
        //menu setup
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenuItem menuItem;

        //file menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        menuItem = new JMenuItem(READ_GENOTYPES);
        setAccelerator(menuItem, 'O', false);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        /*
        viewGenotypesItem = new JMenuItem(VIEW_GENOTYPES);
        viewGenotypesItem.addActionListener(this);
        //viewGenotypesItem.setEnabled(false);
        fileMenu.add(viewGenotypesItem);
        */

        readMarkerItem = new JMenuItem(READ_MARKERS);
        setAccelerator(readMarkerItem, 'I', false);
        readMarkerItem.addActionListener(this);
        readMarkerItem.setEnabled(false);
        fileMenu.add(readMarkerItem);

        /*
        viewMarkerItem = new JMenuItem(VIEW_MARKERS);
        viewMarkerItem.addActionListener(this);
        //viewMarkerItem.setEnabled(false);
        fileMenu.add(viewMarkerItem);
        */

        fileMenu.addSeparator();

        exportMenuItems = new JMenuItem[exportItems.length];
        for (int i = 0; i < exportItems.length; i++) {
            exportMenuItems[i] = new JMenuItem(exportItems[i]);
            exportMenuItems[i].addActionListener(this);
            exportMenuItems[i].setEnabled(false);
            fileMenu.add(exportMenuItems[i]);
        }

        fileMenu.addSeparator();
        fileMenu.setMnemonic(KeyEvent.VK_F);

        menuItem = new JMenuItem(QUIT);
        setAccelerator(menuItem, 'Q', false);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        /// display menu

        JMenu displayMenu = new JMenu("Display");
        displayMenu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(displayMenu);

        ButtonGroup group = new ButtonGroup();
        viewMenuItems = new JRadioButtonMenuItem[viewItems.length];
        for (int i = 0; i < viewItems.length; i++) {
            viewMenuItems[i] = new JRadioButtonMenuItem(viewItems[i], i == 0);
            viewMenuItems[i].addActionListener(this);

            KeyStroke ks = KeyStroke.getKeyStroke('1' + i, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            viewMenuItems[i].setAccelerator(ks);

            displayMenu.add(viewMenuItems[i]);
            viewMenuItems[i].setEnabled(false);
            group.add(viewMenuItems[i]);
        }
        displayMenu.addSeparator();
        //a submenu
        ButtonGroup zg = new ButtonGroup();
        JMenu zoomMenu = new JMenu("D prime zoom");
        zoomMenu.setMnemonic(KeyEvent.VK_Z);
        zoomMenuItems = new JRadioButtonMenuItem[zoomItems.length];
        for (int i = 0; i < zoomItems.length; i++){
            zoomMenuItems[i] = new JRadioButtonMenuItem(zoomItems[i], i==0);
            zoomMenuItems[i].addActionListener(this);
            zoomMenuItems[i].setActionCommand("zoom" + i);
            zoomMenu.add(zoomMenuItems[i]);
            zg.add(zoomMenuItems[i]);
        }
        displayMenu.add(zoomMenu);
        //another submenu
        ButtonGroup cg = new ButtonGroup();
        JMenu colorMenu = new JMenu("D prime color scheme");
        colorMenu.setMnemonic(KeyEvent.VK_C);
        colorMenuItems = new JRadioButtonMenuItem[colorItems.length];
        for (int i = 0; i< colorItems.length; i++){
            colorMenuItems[i] = new JRadioButtonMenuItem(colorItems[i],i==0);
            colorMenuItems[i].addActionListener(this);
            colorMenuItems[i].setActionCommand("color" + i);
            colorMenu.add(colorMenuItems[i]);
            cg.add(colorMenuItems[i]);
            if (i != 0){
                colorMenuItems[i].setEnabled(false);
            }
        }
        displayMenu.add(colorMenu);

        //analysis menu
        JMenu analysisMenu = new JMenu("Analysis");
        analysisMenu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(analysisMenu);
        //a submenu
        ButtonGroup bg = new ButtonGroup();
        JMenu blockMenu = new JMenu("Define Blocks");
        blockMenu.setMnemonic(KeyEvent.VK_B);
        blockMenuItems = new JRadioButtonMenuItem[blockItems.length];
        for (int i = 0; i < blockItems.length; i++){
            blockMenuItems[i] = new JRadioButtonMenuItem(blockItems[i], i==0);
            blockMenuItems[i].addActionListener(this);
            blockMenuItems[i].setActionCommand("block" + i);
            blockMenu.add(blockMenuItems[i]);
            bg.add(blockMenuItems[i]);
            if (i != 0){
                blockMenuItems[i].setEnabled(false);
            }
        }
        analysisMenu.add(blockMenu);
        clearBlocksItem = new JMenuItem(CLEAR_BLOCKS);
        setAccelerator(clearBlocksItem, 'C', false);
        clearBlocksItem.addActionListener(this);
        clearBlocksItem.setEnabled(false);
        analysisMenu.add(clearBlocksItem);
        JMenuItem customizeBlocksItem = new JMenuItem(CUST_BLOCKS);
        customizeBlocksItem.addActionListener(this);
        analysisMenu.add(customizeBlocksItem);

        //color key
        keyMenu = new JMenu("Key");
        changeKey(1);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(keyMenu);


        /** NEEDS FIXING
         helpMenu = new JMenu("Help");
         menuBar.add(Box.createHorizontalGlue());
         menuBar.add(helpMenu);

         menuItem = new JMenuItem("Tutorial");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
         **/

        /*
        clearBlocksMenuItem.addActionListener(this);
        clearBlocksMenuItem.setEnabled(false);
        toolMenu.add(clearBlocksMenuItem);
        */

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                quit();
            }
        });

        addComponentListener(new ResizeListener());

    }

    public void argHandler(String[] args) {
      this.argParser = new HaploText(args);
    }


    // function workaround for overdesigned, underthought swing api -fry
    void setAccelerator(JMenuItem menuItem, char what, boolean shift) {
        menuItem.setAccelerator(KeyStroke.getKeyStroke(what, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | (shift ? ActionEvent.SHIFT_MASK : 0)));
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == READ_GENOTYPES){
            ReadDataDialog readDialog = new ReadDataDialog("Open new data", this);
            readDialog.pack();
            readDialog.setVisible(true);
        } else if (command == READ_MARKERS){
            //JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.setSelectedFile(null);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                readMarkers(fc.getSelectedFile());
            }
        }else if (command == CUST_BLOCKS){
            TweakBlockDefsDialog tweakDialog = new TweakBlockDefsDialog("Customize Blocks", this);
            tweakDialog.pack();
            tweakDialog.setVisible(true);
        }else if (command == CLEAR_BLOCKS){
            colorMenuItems[0].setSelected(true);
            for (int i = 1; i< colorMenuItems.length; i++){
                colorMenuItems[i].setEnabled(false);
            }
            changeBlocks(3,1);

        //blockdef clauses
        }else if (command.startsWith("block")){
            int method = Integer.valueOf(command.substring(5)).intValue();
            changeBlocks(method,1);
            for (int i = 1; i < colorMenuItems.length; i++){
                if (method+1 == i){
                    colorMenuItems[i].setEnabled(true);
                }else{
                    colorMenuItems[i].setEnabled(false);
                }
            }
            colorMenuItems[0].setSelected(true);

        //zooming clauses
        }else if (command.startsWith("zoom")){
            dPrimeDisplay.zoom(Integer.valueOf(command.substring(4)).intValue());

        //coloring clauses
        }else if (command.startsWith("color")){
            dPrimeDisplay.refresh(Integer.valueOf(command.substring(5)).intValue()+1);
            changeKey(Integer.valueOf(command.substring(5)).intValue()+1);

        //exporting clauses
        }else if (command == EXPORT_PNG){
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                export(tabs.getSelectedIndex(), PNG_MODE, fc.getSelectedFile());
            }
        }else if (command == EXPORT_TEXT){
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                export(tabs.getSelectedIndex(), TXT_MODE, fc.getSelectedFile());
            }
        }else if (command == "Select All"){
            checkPanel.selectAll();
        }else if (command == "Rescore Markers"){
            String cut = hwcut.getText();
            if (cut.equals("")){
                cut = "0";
            }
            CheckData.hwCut = Double.parseDouble(cut);

            cut = genocut.getText();
            if (cut.equals("")){
                cut="0";
            }
            CheckData.failedGenoCut = Integer.parseInt(cut);

            cut = mendcut.getText();
            if (cut.equals("")){
                cut="0";
            }
            CheckData.numMendErrCut = Integer.parseInt(cut);

            checkPanel.redoRatings();
        }else if (command == "Tutorial"){
            showHelp();
        } else if (command == QUIT){
            quit();
        } else {
            for (int i = 0; i < viewItems.length; i++) {
                if (command == viewItems[i]) tabs.setSelectedIndex(i);
            }
        }
    }

    private void changeKey(int scheme) {
        keyMenu.removeAll();
        if (scheme == 1){
            JMenuItem keyItem = new JMenuItem("High D'");
            Dimension size = keyItem.getPreferredSize();
            keyItem.setBackground(Color.red);
            keyMenu.add(keyItem);
            for (int i = 1; i < 4; i++){
                double blgr = (255-32)*2*(0.5*i/3);
                keyItem = new JMenuItem("");
                keyItem.setPreferredSize(size);
                keyItem.setBackground(new Color(255,(int)blgr, (int)blgr));
                keyMenu.add(keyItem);
            }
            keyItem = new JMenuItem("Low D'");
            keyItem.setBackground(Color.white);
            keyMenu.add(keyItem);
            keyItem = new JMenuItem("High D' / Low LOD");
            keyItem.setBackground(new Color(192, 192, 240));
            keyMenu.add(keyItem);
            keyItem = new JMenuItem("High LOD / Low D'");
            keyItem.setBackground(new Color(224, 224, 224));
            keyMenu.add(keyItem);
        } else if (scheme == 2){
            JMenuItem keyItem = new JMenuItem("Strong Linkage");
            keyItem.setBackground(Color.darkGray);
            keyItem.setForeground(Color.white);
            keyMenu.add(keyItem);
            keyItem = new JMenuItem("Uninformative");
            keyItem.setBackground(Color.lightGray);
            keyMenu.add(keyItem);
            keyItem = new JMenuItem("Recombination");
            keyItem.setBackground(Color.white);
            keyMenu.add(keyItem);
        } else if (scheme == 3){
            JMenuItem keyItem = new JMenuItem("< 4 Gametes");
            keyItem.setBackground(Color.darkGray);
            keyItem.setForeground(Color.white);
            keyMenu.add(keyItem);
            keyItem = new JMenuItem("4 Gametes");
            keyItem.setBackground(Color.white);
            keyMenu.add(keyItem);
        }
    }

    void quit(){
        //any handling that might need to take place here
        System.exit(0);
    }

    void readPedGenotypes(String[] f){
        //input is a 3 element array with
        //inputOptions[0] = ped file
        //inputOptions[1] = info file (null if none)
        //inputOptions[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)

        inputOptions = f;
        File pedFile = new File(inputOptions[0]);
        //pop open checkdata window
        //checkWindow = new JFrame();
        try {
            if (pedFile.length() < 1){
                throw new HaploViewException("Pedfile is empty or nonexistent: " + pedFile.getName());
            }

            checkPanel = new CheckDataPanel(pedFile);
            checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            theData = new HaploData(assocTest);
            JTable table = checkPanel.getTable();
            boolean[] markerResultArray = new boolean[table.getRowCount()];
            for (int i = 0; i < table.getRowCount(); i++){
                markerResultArray[i] = ((Boolean)table.getValueAt(i,7)).booleanValue();
            }

            theData.linkageToChrom(markerResultArray,checkPanel.getPedFile());
            processData();
        }catch(IOException ioexec) {
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch(PedFileException pfe){
            JOptionPane.showMessageDialog(this,
                    pfe.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }catch (HaploViewException hve){
            JOptionPane.showMessageDialog(this,
                    hve.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }


    }

    void readPhasedGenotypes(String[] f){
        //input is a 3 element array with
        //inputOptions[0] = haps file
        //inputOptions[1] = info file (null if none)
        //inputOptions[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)

        //these are not available for non ped files
        viewMenuItems[VIEW_CHECK_NUM].setEnabled(false);
        viewMenuItems[VIEW_TDT_NUM].setEnabled(false);
        checkPanel = null;
        assocTest = 0;


        inputOptions = f;
        theData = new HaploData();
        try{
            theData.prepareHapsInput(new File(inputOptions[0]));
            processData();
        }catch(IOException ioexec) {
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }catch(HaploViewException ex){
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    void processData() {
        if (inputOptions[2].equals("")){
            inputOptions[2] = "0";
        }
        maxCompDist = Long.parseLong(inputOptions[2])*1000;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final SwingWorker worker = new SwingWorker(){
            public Object construct(){
                dPrimeDisplay=null;
                theData.infoKnown = false;
                if (!(inputOptions[1].equals(""))){
                    readMarkers(new File(inputOptions[1]));
                }
                theData.generateDPrimeTable(maxCompDist);
                theData.guessBlocks(0);
                colorMenuItems[0].setSelected(true);
                colorMenuItems[1].setEnabled(true);
                theData.blocksChanged = false;
                Container contents = getContentPane();
                contents.removeAll();

                int currentTab = 0;
                /*if (!(tabs == null)){
                currentTab = tabs.getSelectedIndex();
                } */

                tabs = new JTabbedPane();
                tabs.addChangeListener(new TabChangeListener());

                //first, draw the D' picture
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                dPrimeDisplay = new DPrimeDisplay(theData);
                JScrollPane dPrimeScroller = new JScrollPane(dPrimeDisplay);
                dPrimeScroller.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
                dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
                panel.add(dPrimeScroller);
                tabs.addTab(viewItems[VIEW_D_NUM], panel);
                viewMenuItems[VIEW_D_NUM].setEnabled(true);

                //compute and show haps on next tab
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                try {
                    hapDisplay = new HaplotypeDisplay(theData);
                } catch(HaploViewException e) {
                    JOptionPane.showMessageDialog(window,
                            e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                HaplotypeDisplayController hdc =
                        new HaplotypeDisplayController(hapDisplay);
                hapScroller = new JScrollPane(hapDisplay);
                hapScroller.getVerticalScrollBar().setUnitIncrement(60);
                hapScroller.getHorizontalScrollBar().setUnitIncrement(60);
                panel.add(hapScroller);
                panel.add(hdc);
                tabs.addTab(viewItems[VIEW_HAP_NUM], panel);
                viewMenuItems[VIEW_HAP_NUM].setEnabled(true);

                //check data panel
                if (checkPanel != null){
                    JPanel metaCheckPanel = new JPanel();
                    metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
                    JLabel countsLabel = new JLabel("Using " + theData.numSingletons + " singletons and "
                            + theData.numTrios + " trios from "
                            + theData.numPeds + " families.");
                    if (theData.numTrios + theData.numSingletons == 0){
                        countsLabel.setForeground(Color.red);
                    }
                    countsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    metaCheckPanel.add(countsLabel);
                    metaCheckPanel.add(checkPanel);

                    JPanel failPanel = new JPanel();
                    failPanel.setLayout(new BoxLayout(failPanel,BoxLayout.Y_AXIS));
                    JPanel holdPanel = new JPanel();
                    holdPanel.add(new JLabel("HW p-value cutoff: "));
                    hwcut = new NumberTextField(String.valueOf(CheckData.hwCut),6,true);
                    holdPanel.add(hwcut);
                    failPanel.add(holdPanel);
                    holdPanel = new JPanel();
                    holdPanel.add(new JLabel("Min genotype %: "));
                    genocut = new NumberTextField(String.valueOf(CheckData.failedGenoCut),2, false);
                    holdPanel.add(genocut);
                    failPanel.add(holdPanel);
                    holdPanel = new JPanel();
                    holdPanel.add(new JLabel("Max # mendel errors: "));
                    mendcut = new NumberTextField(String.valueOf(CheckData.numMendErrCut),2,false);
                    holdPanel.add(mendcut);
                    failPanel.add(holdPanel);
                    JButton rescore = new JButton("Rescore Markers");
                    rescore.addActionListener(window);
                    failPanel.add(rescore);

                    JButton selAll = new JButton("Select All");
                    selAll.addActionListener(window);

                    JPanel ctrlPanel = new JPanel();
                    ctrlPanel.add(failPanel);
                    ctrlPanel.add(selAll);
                    metaCheckPanel.add(ctrlPanel);

                    tabs.addTab(viewItems[VIEW_CHECK_NUM], metaCheckPanel);
                    viewMenuItems[VIEW_CHECK_NUM].setEnabled(true);
                    currentTab=VIEW_CHECK_NUM;
                }

                //TDT panel
                if(assocTest > 0) {
                    tdtPanel = new TDTPanel(theData.chromosomes, assocTest);
                    tabs.addTab(viewItems[VIEW_TDT_NUM], tdtPanel);
                    viewMenuItems[VIEW_TDT_NUM].setEnabled(true);
                }

                tabs.setSelectedIndex(currentTab);
                contents.add(tabs);

                repaint();
                setVisible(true);

                theData.finished = true;
                return null;
            }
        };

        timer = new javax.swing.Timer(50, new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                if (theData.finished){
                    timer.stop();
                    for (int i = 0; i < blockMenuItems.length; i++){
                        blockMenuItems[i].setEnabled(true);
                    }
                    clearBlocksItem.setEnabled(true);
                    readMarkerItem.setEnabled(true);
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        worker.start();
        timer.start();
    }

    void readMarkers(File inputFile){
        try {
            theData.prepareMarkerInput(inputFile,maxCompDist);
            if (checkPanel != null){
                checkPanel.refreshNames();
            }
            if (tdtPanel != null){
                tdtPanel.refreshNames();
            }
        }catch (HaploViewException e){
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }catch (IOException ioexec){
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (dPrimeDisplay != null && tabs.getSelectedIndex() == VIEW_D_NUM){
            dPrimeDisplay.refresh(0);
        }
    }

    public int getCurrentBlockDef() {
        return currentBlockDef;
    }

    public void changeBlocks(int method, int color){
        theData.guessBlocks(method);
        dPrimeDisplay.refresh(color);
        changeKey(color);
        currentBlockDef = method;

        try{
            if (tabs.getSelectedIndex() == VIEW_HAP_NUM){
                hapDisplay.getHaps();
            }
        }catch(HaploViewException hve) {
            JOptionPane.showMessageDialog(this,
                    hve.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        hapScroller.setViewportView(hapDisplay);
    }

    class TabChangeListener implements ChangeListener{
        public void stateChanged(ChangeEvent e) {
            int tabNum = tabs.getSelectedIndex();
            if (tabNum == VIEW_D_NUM || tabNum == VIEW_HAP_NUM){
                exportMenuItems[0].setEnabled(true);
                exportMenuItems[1].setEnabled(true);
            }else if (tabNum == VIEW_TDT_NUM || tabNum == VIEW_CHECK_NUM){
                exportMenuItems[0].setEnabled(true);
                exportMenuItems[1].setEnabled(false);
            }else{
                exportMenuItems[0].setEnabled(false);
                exportMenuItems[1].setEnabled(false);
            }

            if (tabNum == VIEW_D_NUM){
                keyMenu.setEnabled(true);
            }else{
                keyMenu.setEnabled(false);
            }

            viewMenuItems[tabs.getSelectedIndex()].setSelected(true);
            if (checkPanel != null && checkPanel.changed){
                window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                JTable table = checkPanel.getTable();
                boolean[] markerResults = new boolean[table.getRowCount()];
                for (int i = 0; i < table.getRowCount(); i++){
                    markerResults[i] = ((Boolean)table.getValueAt(i,7)).booleanValue();
                }
                int count = 0;
                for (int i = 0; i < Chromosome.getSize(); i++){
                    if (markerResults[i]){
                        count++;
                    }
                }
                Chromosome.realIndex = new int[count];
                int k = 0;
                for (int i =0; i < Chromosome.getSize(); i++){
                    if (markerResults[i]){
                        Chromosome.realIndex[k] = i;
                        k++;
                    }
                }
                theData.filteredDPrimeTable = theData.getFilteredTable();

                //after editing the filtered marker list, needs to be prodded into
                //resizing correctly
                Dimension size = dPrimeDisplay.getSize();
                Dimension pref = dPrimeDisplay.getPreferredSize();
                Rectangle visRect = dPrimeDisplay.getVisibleRect();
                if (size.width != pref.width && pref.width > visRect.width){
                    ((JViewport)dPrimeDisplay.getParent()).setViewSize(pref);
                }

                hapDisplay.theData = theData;

                changeBlocks(currentBlockDef, dPrimeDisplay.currentScheme);

                if (tdtPanel != null){
                    tdtPanel.refreshTable();
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                checkPanel.changed=false;
            }

            if (hapDisplay != null && theData.blocksChanged){
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try{
                    hapDisplay.getHaps();
                }catch(HaploViewException hv){
                    JOptionPane.showMessageDialog(window,
                            hv.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                hapScroller.setViewportView(hapDisplay);

                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                theData.blocksChanged = false;
            }
        }
    }

    class ResizeListener implements ComponentListener{
        public void componentResized(ComponentEvent e) {
            if (dPrimeDisplay != null){
                dPrimeDisplay.refresh(0);
            }
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }

    }

    void export(int tabNum, int format, File outfile){
        if (format == PNG_MODE){
            BufferedImage image;
            if (tabNum == VIEW_D_NUM){
                Dimension pref = dPrimeDisplay.getPreferredSize();
                image = new BufferedImage(pref.width, pref.height,
                        BufferedImage.TYPE_3BYTE_BGR);
                dPrimeDisplay.export(image);
            }else if (tabNum == VIEW_HAP_NUM){
                Dimension size = hapDisplay.getPreferredSize();
                image = new BufferedImage(size.width, size.height,
                        BufferedImage.TYPE_3BYTE_BGR);
                hapDisplay.export(image);
            }else{
                image = new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
            }

            try{
                String filename = outfile.getPath();
                if (! (filename.endsWith(".png") || filename.endsWith(".PNG"))){
                    filename += ".png";
                }
                Jimi.putImage("image/png", image, filename);
            }catch(JimiException je){
                JOptionPane.showMessageDialog(this,
                        je.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (format == TXT_MODE){
            try{
                if (tabNum == VIEW_D_NUM){
                    theData.saveDprimeToText(outfile);
                }else if (tabNum == VIEW_HAP_NUM){
                    theData.saveHapsToText(hapDisplay.filteredHaplos,hapDisplay.multidprimeArray, outfile);
                }else if (tabNum == VIEW_CHECK_NUM){
                    JTable table = checkPanel.getTable();

                    FileWriter checkWriter = new FileWriter(outfile);
                    int numCols = table.getColumnCount();
                    StringBuffer header = new StringBuffer();
                    for (int i = 0; i < numCols; i++){
                        header.append(table.getColumnName(i)).append("\t");
                    }
                    header.append("\n");
                    checkWriter.write(header.toString());
                    for (int i = 0; i < table.getRowCount(); i++){
                        StringBuffer sb = new StringBuffer();
                        //don't print the true/false vals in last column
                        for (int j = 0; j < numCols-1; j++){
                            sb.append(table.getValueAt(i,j)).append("\t");
                        }
                        //print BAD if last column is false
                        if (((Boolean)table.getValueAt(i, numCols-1)).booleanValue()){
                            sb.append("\n");
                        }else{
                            sb.append("BAD\n");
                        }
                        checkWriter.write(sb.toString());
                    }
                    checkWriter.close();
                }else if (tabNum == VIEW_TDT_NUM){
                    JTable table = tdtPanel.getTable();
                    FileWriter checkWriter = new FileWriter(outfile);
                    int numCols = table.getColumnCount();
                    StringBuffer header = new StringBuffer();
                    for (int i = 0; i < numCols; i++){
                        header.append(table.getColumnName(i)).append("\t");
                    }
                    header.append("\n");
                    checkWriter.write(header.toString());
                    for (int i = 0; i < table.getRowCount(); i++){
                        StringBuffer sb = new StringBuffer();
                        for (int j = 0; j < numCols; j++){
                            sb.append(table.getValueAt(i,j)).append("\t");
                        }
                        sb.append("\n");
                        checkWriter.write(sb.toString());
                    }
                    checkWriter.close();
                }
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(this,
                        ioe.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void showHelp(){

        //Help Text:
        String helpText = new String();
        try{
            File helpFile = new File(System.getProperty("java.class.path") + File.separator + "haplohelp.txt");
            BufferedReader inHelp = new BufferedReader(new FileReader(helpFile));
            helpText = inHelp.readLine();
            String currentLine = new String();
            while ((currentLine = inHelp.readLine()) != null){
                helpText += ("\n" + currentLine);
            }
            inHelp.close();
        }catch (IOException ioexec){
            helpText = "Help file not found.\n";
        }

        JFrame helpFrame = new JFrame("HaploView Help");
        JTextArea helpTextArea = new JTextArea();
        JScrollPane helpDisplayPanel = new JScrollPane(helpTextArea);
        helpDisplayPanel.setBackground(Color.white);
        helpTextArea.setText(helpText);
        helpDisplayPanel.setOpaque(true);
        helpDisplayPanel.setPreferredSize(new Dimension(450,500));
        helpFrame.setContentPane(helpDisplayPanel);
        helpFrame.pack();
        helpFrame.setVisible(true);
    }

    public static void main(String[] args) {
        boolean nogui = false;
        //HaploView window;
        for(int i = 0;i<args.length;i++) {
            if(args[i].equals("-n") || args[i].equals("-h")) {
                nogui = true;
            }
        }
        if(nogui) {
            HaploText textOnly = new HaploText(args);
        }
        else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { }
            //System.setProperty("swing.disableFileChooserSpeedFix", "true");

            window  =  new HaploView();
            window.argHandler(args);

            //setup view object
            window.setTitle("HaploView beta");
            window.setSize(800,600);

            //center the window on the screen
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            window.setLocation((screen.width - window.getWidth()) / 2,
                    (screen.height - window.getHeight()) / 2);

            window.setVisible(true);
            ReadDataDialog readDialog = new ReadDataDialog("Welcome to HaploView", window);
            readDialog.pack();
            readDialog.setVisible(true);

        }
    }
}

