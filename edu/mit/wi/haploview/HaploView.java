package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;

import java.awt.*;
import java.io.*;
//import java.util.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
//import java.awt.geom.*;
//import java.awt.image.*;

public class HaploView extends JFrame implements ActionListener{

    //some constants etc.
    static final String MARKER_DATA_EXT = ".info";

    static final String READ_GENOTYPES = "Open genotype data";
    static final String READ_MARKERS = "Load marker data";
    JMenuItem readMarkerItem;

    static final String EXPORT_TEXT = "Export data to text";
    static final String EXPORT_PNG = "Export data to PNG";
    static final String EXPORT_PS = "Export data to postscript";
    static final String EXPORT_PRINT = "Print";
    String exportItems[] = {
        EXPORT_TEXT, EXPORT_PNG, EXPORT_PS, EXPORT_PRINT
    };
    JMenuItem exportMenuItems[];

    static final String DEFINE_BLOCKS = "Define blocks";
    JMenuItem defineBlocksItem;

    static final String QUIT = "Quit";

    static final String VIEW_DPRIME = "D Prime Plot";
    static final String VIEW_HAPLOTYPES = "Haplotypes";
    static final String VIEW_GENOTYPES = "Genotype Data";
    static final String VIEW_MARKERS = "Marker Data";
    String viewItems[] = {
        VIEW_DPRIME, VIEW_HAPLOTYPES
    };
    JRadioButtonMenuItem viewMenuItems[];

    //static final String DISPLAY_OPTIONS = "Display Options";
    //JMenuItem displayOptionsItem;

    //start filechooser in current directory
    final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

    HaploData theData;
    JFrame checkWindow;
    private CheckDataPanel checkPanel;
    //private String hapInputFileName;
    //private BlockDisplay theBlocks;
    private boolean infoKnown = false;
    private javax.swing.Timer timer;

    DPrimeDisplay dPrimeDisplay;
    HaplotypeDisplay hapDisplay;
    JTabbedPane tabs;
    String[] filenames;


    public HaploView(){
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

        menuItem = new JMenuItem(QUIT);
        setAccelerator(menuItem, 'Q', false);
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        /// display menu

        JMenu displayMenu = new JMenu("Display");
        menuBar.add(displayMenu);

        ButtonGroup group = new ButtonGroup();
        viewMenuItems = new JRadioButtonMenuItem[viewItems.length];
        for (int i = 0; i < viewItems.length; i++) {
            viewMenuItems[i] = new JRadioButtonMenuItem(viewItems[i], i == 0);
            viewMenuItems[i].addActionListener(this);

            KeyStroke ks = KeyStroke.getKeyStroke('1' + i, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            viewMenuItems[i].setAccelerator(ks);

            displayMenu.add(viewMenuItems[i]);
            group.add(viewMenuItems[i]);
        }

        //analysis menu
        JMenu analysisMenu = new JMenu("Analysis");
        menuBar.add(analysisMenu);
        defineBlocksItem = new JMenuItem(DEFINE_BLOCKS);
        setAccelerator(defineBlocksItem, 'B', false);
        defineBlocksItem.addActionListener(this);
        defineBlocksItem.setEnabled(false);
        analysisMenu.add(defineBlocksItem);

        // maybe
        //displayMenu.addSeparator();
        //displayOptionsItem = new JMenuItem(DISPLAY_OPTIONS);
        //setAccelerator(displayOptionsItem, 'D', false);

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

        guessBlocksMenuItem.addActionListener(this);
        guessBlocksMenuItem.setEnabled(false);
        toolMenu.add(guessBlocksMenuItem);
        toolMenu.addSeparator();
        */

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                quit();
            }
        });
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

        }else if (command == "Continue"){
        //TODO: change it so writing to a file is a checkbox on the CheckDataPanel
            theData = new HaploData();
            JTable table = checkPanel.getTable();
            checkWindow.dispose();
            boolean[] markerResultArray = new boolean[table.getRowCount()];
            for (int i = 0; i < table.getRowCount(); i++){
                markerResultArray[i] = ((Boolean)table.getValueAt(i,7)).booleanValue();
            }
            /*
            try{
                new TextMethods().linkageToHaps(markerResultArray,checkPanel.getPedFile(),filenames[0]+".haps");
            }catch (IOException ioexec){
                JOptionPane.showMessageDialog(this,
                        ioexec.getMessage(),
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
            } */
            theData.linkageToChrom(markerResultArray,checkPanel.getPedFile());
            processData();
            //processInput(new File(hapInputFileName+".haps"));
        } else if (command == READ_MARKERS){
            fc.setSelectedFile(null);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                readMarkers(fc.getSelectedFile());
            }
        }else if (command == "Clear All Blocks"){
            //theBlocks.clearBlocks();
        }else if (command == DEFINE_BLOCKS){
            defineBlocks();
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


    void quit(){
        //any handling that might need to take place here
        System.exit(0);
    }


    void readPedGenotypes(String[] f){
        //input is a 3 element array with
        //filenames[0] = ped file
        //filenames[1] = info file (null if none)
        //filenames[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)

        filenames = f;
        File pedFile = new File(filenames[0]);

        //pop open checkdata window
        checkWindow = new JFrame();
        checkPanel = new CheckDataPanel(pedFile);
        checkWindow.setTitle("Checking markers..." + pedFile.getName());
        JPanel metaCheckPanel = new JPanel();
        metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
        JButton checkContinueButton = new JButton("Continue");
        checkContinueButton.addActionListener(this);
        checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaCheckPanel.add(checkPanel);
        checkContinueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaCheckPanel.add(checkContinueButton);
        JLabel infoLabel = new JLabel("(this will create a haplotype file named " + pedFile.getName() + ".haps)");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaCheckPanel.add(infoLabel);
        checkWindow.setContentPane(metaCheckPanel);
        checkWindow.pack();
        checkWindow.setVisible(true);
    }

    void readPhasedGenotypes(String[] f){
        //input is a 3 element array with
        //filenames[0] = haps file
        //filenames[1] = info file (null if none)
        //filenames[2] = max comparison distance (don't compute d' if markers are greater than this dist apart)

        filenames = f;
        theData = new HaploData();
        try{
            theData.prepareGenotypeInput(new File(filenames[0]));
            processData();
        }catch(IOException ioexec) {
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    void processData(){
        final long maxCompDist = Long.parseLong(filenames[2])*1000;
        try{
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final SwingWorker worker = new SwingWorker(){
                public Object construct(){
                    dPrimeDisplay=null;
                    if (!(filenames[1].equals(""))){
                        readMarkers(new File(filenames[1]));
                    }
                    theData.generateDPrimeTable(maxCompDist);
                    theData.guessBlocks(0);
                    drawPicture(theData);
                    theData.finished = true;
                    return "";
                }
            };

            timer = new javax.swing.Timer(50, new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    if (theData.finished){
                        timer.stop();
                        defineBlocksItem.setEnabled(true);
                        readMarkerItem.setEnabled(true);
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });

            worker.start();
            timer.start();
        }catch (RuntimeException rtexec){
            JOptionPane.showMessageDialog(this,
                    "An error has occured. It is probably related to file format:\n"+rtexec.toString(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    void readMarkers(File inputFile){
        try {
            int good = theData.prepareMarkerInput(inputFile);
            if (good == -1){
                JOptionPane.showMessageDialog(this,
                        "Number of markers in info file does not match number of markers in dataset.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }else{
                infoKnown=true;
                if (dPrimeDisplay != null){
                    dPrimeDisplay.loadMarkers(theData.markerInfo);
                }
            }
        }catch (IOException ioexec){
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }catch (RuntimeException rtexec){
            JOptionPane.showMessageDialog(this,
                    "An error has occured. It is probably related to file format:\n"+rtexec.toString(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    void drawPicture(HaploData theData){
        Container contents = getContentPane();
        contents.removeAll();

        //remember which tab we're in if they've already been set up
        int currentTabIndex = 0;
        if (!(tabs == null)){
            currentTabIndex = tabs.getSelectedIndex();
        }

        tabs = new JTabbedPane();
        tabs.addChangeListener(new TabChangeListener());

        //first, draw the D' picture
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        dPrimeDisplay = new DPrimeDisplay(theData.dPrimeTable, infoKnown, theData.markerInfo);
        JScrollPane dPrimeScroller = new JScrollPane(dPrimeDisplay);
        dPrimeScroller.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
        dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
        panel.add(dPrimeScroller);
        tabs.addTab(viewItems[0], panel);

        //compute and show haps on next tab
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        hapDisplay = new HaplotypeDisplay(theData);
        HaplotypeDisplayController hdc =
                new HaplotypeDisplayController(hapDisplay);
        JScrollPane hapScroller = new JScrollPane(hapDisplay);
        panel.add(hapScroller);
        panel.add(hdc);
        tabs.addTab(viewItems[1], panel);
        tabs.setSelectedIndex(currentTabIndex);
        contents.add(tabs);

        //next add a little spacer
        //ontents.add(Box.createRigidArea(new Dimension(0,5)));

        //and then add the block display
        //theBlocks = new BlockDisplay(theData.markerInfo, theData.blocks, dPrimeDisplay, infoKnown);
        //contents.setBackground(Color.black);

        //put the block display in a scroll pane in case the data set is very large.
        //JScrollPane blockScroller = new JScrollPane(theBlocks,
        //						    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        //					    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //blockScroller.getHorizontalScrollBar().setUnitIncrement(60);
        //blockScroller.setMinimumSize(new Dimension(800, 100));
        //contents.add(blockScroller);
        repaint();
        setVisible(true);
    }

    class TabChangeListener implements ChangeListener{
        public void stateChanged(ChangeEvent e) {
            viewMenuItems[tabs.getSelectedIndex()].setSelected(true);
        }
    }

    /**void doExportDPrime(){
     fc.setSelectedFile(null);
     int returnVal = fc.showSaveDialog(this);
     if (returnVal == JFileChooser.APPROVE_OPTION){
     try {
     DrawingMethods dm = new DrawingMethods();
     Dimension theSize = dm.dPrimeGetPreferredSize(theData.dPrimeTable.length, infoKnown);
     BufferedImage image = new BufferedImage((int)theSize.getWidth(), (int)theSize.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
     dm.dPrimeDraw(theData.dPrimeTable, infoKnown, theData.markerInfo, image.getGraphics());
     dm.saveImage(image, fc.getSelectedFile().getPath());
     } catch (IOException ioexec){
     JOptionPane.showMessageDialog(this,
     ioexec.getMessage(),
     "File Error",
     JOptionPane.ERROR_MESSAGE);
     }
     }
     }**/

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


    void saveDprimeToText(){
        fc.setSelectedFile(null);
        try{
            fc.setSelectedFile(null);
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                new TextMethods().saveDprimeToText(theData.dPrimeTable, fc.getSelectedFile(), infoKnown, theData.markerInfo);
            }
        }catch (IOException ioexec){
            JOptionPane.showMessageDialog(this,
                    ioexec.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    void defineBlocks(){
        String[] methodStrings = {"95% of informative pairwise comparisons show strong LD via confidence intervals (SFS)",
                                  "Four Gamete Rule",
                                  "Solid block of strong LD via D prime (MJD)"};
        JComboBox methodList = new JComboBox(methodStrings);
        JOptionPane.showMessageDialog(this,
                methodList,
                "Select a block-finding algorithm",
                JOptionPane.QUESTION_MESSAGE);
        theData.guessBlocks(methodList.getSelectedIndex());
        hapDisplay.getHaps();
        if (tabs.getSelectedIndex() == 0) dPrimeDisplay.repaint();
    }


    private static void pedTextOnly(String pedFileName) {
        try {
            File theFile;
            PedFile ped;
            Vector pedFileStrings;
            BufferedReader reader;
            String line;


            theFile  = new File(pedFileName);
            ped = new PedFile();
            pedFileStrings = new Vector();
            reader = new BufferedReader(new FileReader(theFile));


            while((line = reader.readLine())!=null){
                pedFileStrings.add(line);
            }

            ped.parse(pedFileStrings);
            Vector result = ped.check();
        }
        catch(IOException e){}
    }


    /**
     * this method finds haplotypes and caclulates dprime without using any graphics
     */


    private static void hapsTextOnly(String hapsFile,int outputType, int maxDistance){
        try {
            HaploData theData;
            File OutputFile;
            File inputFile = new File(hapsFile);
            long maxDist = maxDistance * 1000;
            if(!inputFile.exists()){
                System.out.println("haps input file " + hapsFile + " does not exist");
            }
            switch(outputType){
                case 1:
                    OutputFile = new File(hapsFile + ".4GAMblocks");
                    break;
                case 2:
                    OutputFile = new File(hapsFile + ".MJDblocks");
                    break;
                default:
                    OutputFile = new File(hapsFile + ".SFSblocks");
                    break;
            }
            //TODO FIX THIS
            theData = new HaploData();
            String name = hapsFile;
            String baseName = hapsFile.substring(0,name.length()-5);
            File maybeInfo = new File(baseName + ".info");
            if (maybeInfo.exists()){
                theData.prepareMarkerInput(maybeInfo);
            }
            theData.generateDPrimeTable(maxDist);
            Haplotype[][] haplos;

            theData.guessBlocks(outputType);
            haplos = theData.generateHaplotypes(theData.blocks, 1);
            new TextMethods().saveHapsToText(orderHaps(haplos, theData), theData.getMultiDprime(), OutputFile);
        }
        catch(IOException e){}
    }

    public static Haplotype[][] orderHaps (Haplotype[][] haplos, HaploData theData) throws IOException{
        Haplotype[][] orderedHaplos = new Haplotype[haplos.length][];
        for (int i = 0; i < haplos.length; i++){
            Vector orderedHaps = new Vector();
            //step through each haplotype in this block
            for (int hapCount = 0; hapCount < haplos[i].length; hapCount++){
                if (orderedHaps.size() == 0){
                    orderedHaps.add(haplos[i][hapCount]);
                }else{
                    for (int j = 0; j < orderedHaps.size(); j++){
                        if (((Haplotype)(orderedHaps.elementAt(j))).getPercentage() < haplos[i][hapCount].getPercentage()){
                            orderedHaps.add(j, haplos[i][hapCount]);
                            break;
                        }
                        if ((j+1) == orderedHaps.size()){
                            orderedHaps.add(haplos[i][hapCount]);
                            break;
                        }
                    }
                }
            }
            orderedHaplos[i] = new Haplotype[orderedHaps.size()];
            for (int z = 0; z < orderedHaps.size(); z++){
                orderedHaplos[i][z] = (Haplotype)orderedHaps.elementAt(z);
            }

        }
        return theData.generateCrossovers(orderedHaplos);
    }


    private static void handleFlags(String[] args){
        // TODO:-want to be able to output dprime
        //      -info file flag
        //      -info files in batch file
        //      -specify values from HaplotypeDisplayController (min hap percentage etc)
        boolean arg_nogui = false;
        String arg_hapsfile = "";
        String arg_pedfile = "";
        int arg_output = -1;
        int arg_distance = -1;


        if(args.length>0) {
            //assume for now that if there are any arguments given that we want text-only mode
            arg_nogui = true;
        }

        for(int i =0; i < args.length; i++) {
            if(args[i].equals("-help") || args[i].equals("-h")) {
                System.out.println("HaploView command line options\n" +
                        "-h, -help          print this message\n" +
                        "-n, -nogui         command line output only\n" +
                        "-p <pedfile>       specify an input file in pedigree file format\n" +
                        "-ha <hapsfile>     specify an input file in .haps format\n" +
                        "-b <batchfile>     batch mode. batchfile should contain a list of haps files\n" +
                        "-o <SFS,GAM,MJD>   output type. SFS, 4 gamete or MJD output. default is SFS.\n" +
                        "-m <distance>      maximum comparison distance in kilobases (integer). default is 200 ");
                System.exit(0);

            }
            else if(args[i].equals("-nogui") || args[i].equals("-n")) {
                arg_nogui = true;
            }
            else if(args[i].equals("-p")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-p requires a filename");
                    System.exit(1);
                }
                else{
                    if(arg_pedfile.equals("")){
                        arg_pedfile = args[i];
                    }
                    else {
                        System.out.println("only one -p argument is allowed");
                        System.exit(1);
                    }
                }
            }
            else if(args[i].equals("-ha")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-ha requires a filename");
                    System.exit(1);
                }
                else{
                    if(arg_hapsfile.equals("")){
                        arg_hapsfile = args[i];
                    }
                    else {
                        System.out.println("only one -h argument is allowed");
                        System.exit(1);
                    }
                }
            }
            else if(args[i].equals("-o")) {
                i++;
                if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    if(arg_output != -1){
                        System.out.println("only one -o argument is allowed");
                        System.exit(1);
                    }
                    if(args[i].equals("SFS")){
                        arg_output = 0;
                    }
                    else if(args[i].equals("GAM")){
                        arg_output = 1;
                    }
                    else if(args[i].equals("MJD")){
                        arg_output = 2;
                    }
                }
                else {
                    //defaults to SFS output
                    arg_output =0;
                    i--;
                }
            }
            else if(args[i].equals("-m")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-m requires an integer argument");
                    System.exit(1);
                }
                else {
                    if(arg_distance != -1){
                        System.out.println("only one -m argument allowed");
                        System.exit(1);
                    }
                    arg_distance = Integer.parseInt(args[i]);
                    if(arg_distance<0){
                        System.out.println("-m argument must be a positive integer");
                        System.exit(1);
                    }

                }
            }
            else if(args[i].equals("-b")) {
                //batch mode
            }
            else {
                System.out.println("invalid parameter specified: " + args[i]);
            }
        }

        if(arg_nogui) {
            //System.out.println("nogui!");
        }
        if(arg_output == -1) {
            arg_output = 0;
        }
        if(arg_distance == -1){
            arg_distance = 200;
        }
        if(!arg_pedfile.equals("")){
            //System.out.println("pedfile!\t" + arg_pedfile);

        }
        else if(!arg_hapsfile.equals("")){
            //System.out.println("hapsfile!\t" + arg_hapsfile);
            hapsTextOnly(arg_hapsfile,arg_output, arg_distance);
        }

    }



    public static void main(String[] args) {//throws IOException{

        if(args.length>0){
            handleFlags(args);
        }
        else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) { }

            //setup view object
            HaploView window = new HaploView();
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

