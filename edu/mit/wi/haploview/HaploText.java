package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.image.BufferedImage;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;

public class HaploText implements Constants{

    private boolean arg_nogui = false;
    private String arg_batchMode;
    private String arg_hapsfile;
    private String arg_infoFileName;
    private String arg_pedfile;
    private String arg_hapmapfile;
    private String arg_blockfile;
    private String arg_trackName;
    private boolean arg_showCheck = false;
    private boolean arg_skipCheck = false;
    private Vector arg_ignoreMarkers = new Vector();
    private boolean arg_quiet = false;
    private int arg_output;
    private boolean arg_check;
    private int arg_distance;
    private boolean arg_dprime;
    private boolean arg_png;
    private boolean arg_smallpng;

    public boolean isNoGui() {
        return arg_nogui;
    }

    public String getBatchMode() {
        return arg_batchMode;
    }

    public String getHapsFileName() {
        return arg_hapsfile;
    }

    public String getPedFileName() {
        return arg_pedfile;
    }

    public String getInfoFileName(){
        return arg_infoFileName;
    }

    public String getHapmapFileName(){
        return arg_hapmapfile;
    }

    public boolean isShowCheck() {
        return arg_showCheck;
    }

    public int getOutputType() {
        return arg_output;
    }

    public int getMaxDistance() {
        return arg_distance;
    }

    public HaploText(String[] args) {
        this.argHandler(args);

        if(!this.arg_batchMode.equals("")) {
            System.out.println(TITLE_STRING);
            this.doBatch();
        }

        if(!(this.arg_pedfile.equals("")) || !(this.arg_hapsfile.equals("")) || !(this.arg_hapmapfile.equals(""))){
            if(arg_nogui){
                System.out.println(TITLE_STRING);
                processTextOnly();
            }
        }

    }

    private void argHandler(String[] args){

        //TODO: -specify values from HaplotypeDisplayController (min hap percentage etc)
        //      -want to be able to output haps file from pedfile
        boolean nogui = false;
        String batchMode = "";
        String hapsFileName = "";
        String pedFileName = "";
        String infoFileName = "";
        String hapmapFileName = "";
        String blockFileName = "";
        boolean showCheck = false;
        boolean skipCheck = false;
        Vector ignoreMarkers = new Vector();
        int outputType = -1;
        int maxDistance = -1;
        boolean quietMode = false;
        boolean outputDprime=false;
        boolean outputPNG = false;
        boolean outputSmallPNG = false;
        boolean outputCheck=false;

        for(int i =0; i < args.length; i++) {
            if(args[i].equals("-help") || args[i].equals("-h")) {
                System.out.println(HELP_OUTPUT);
                System.exit(0);
            }
            else if(args[i].equals("-n")) {
                nogui = true;
            }
            else if(args[i].equals("-p")) {
                i++;
                if( i>=args.length || (args[i].charAt(0) == '-') || args[i].equals("showcheck") ){
                    System.out.println("-p requires a filename");
                    System.exit(1);
                }
                else{
                    if(!pedFileName.equals("")){
                        System.out.println("multiple -p arguments found. only last pedfile listed will be used");
                    }
                    pedFileName = args[i];
                }
            }
            else if (args[i].equals("--showcheck")){
                showCheck = true;
            }
            else if (args[i].equals("--skipcheck")){
                skipCheck = true;
            }
            //todo: fix ignoremarkers
           /* else if (args[i].equals("--ignoremarkers")){
                i++;
                if(i>=args.length || (args[i].charAt(0) == '-')){
                    System.out.println("--ignoremarkers requires a list of markers");
                    System.exit(1);
                }
                else {
                    StringTokenizer str = new StringTokenizer(args[i],",");
                    while(str.hasMoreTokens()) {
                        ignoreMarkers.add(str.nextToken());
                    }
                }
            } */
            else if(args[i].equals("-ha") || args[i].equals("-l")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-ha requires a filename");
                    System.exit(1);
                }
                else{
                    if(!hapsFileName.equals("")){
                        System.out.println("multiple -ha arguments found. only last haps file listed will be used");
                    }
                    hapsFileName = args[i];
                }
            }
            else if(args[i].equals("-i")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-i requires a filename");
                    System.exit(1);
                }
                else{
                    if(!infoFileName.equals("")){
                        System.out.println("multiple -i arguments found. only last info file listed will be used");
                    }
                    infoFileName = args[i];
                }
            } else if (args[i].equals("-a")){
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-a requires a filename");
                    System.exit(1);
                }
                else{
                    if(!hapmapFileName.equals("")){
                        System.out.println("multiple -a arguments found. only last hapmap file listed will be used");
                    }
                    hapmapFileName = args[i];
                }
            }
            else if(args[i].equals("-k")) {
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    blockFileName = args[i];
                    outputType = BLOX_CUSTOM;
                }else{
                    System.out.println("-k requires a filename");
                    System.exit(1);
                }
            }
            else if (args[i].equalsIgnoreCase("-png")){
                outputPNG = true;
            }
            else if (args[i].equals("-smallpng") || args[i].equals("-smallPNG")){
                outputSmallPNG = true;
            }
            else if (args[i].equals("-track")){
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                   arg_trackName = args[i];
                }else{
                    System.out.println("-track requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equals("-o")) {
                i++;
                if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    if(outputType != -1){
                        System.out.println("only one output argument is allowed");
                        System.exit(1);
                    }
                    if(args[i].equalsIgnoreCase("SFS") || args[i].equalsIgnoreCase("GAB")){
                        outputType = BLOX_GABRIEL;
                    }
                    else if(args[i].equalsIgnoreCase("GAM")){
                        outputType = BLOX_4GAM;
                    }
                    else if(args[i].equalsIgnoreCase("MJD") || args[i].equalsIgnoreCase("SPI")){
                        outputType = BLOX_SPINE;
                    }
                    else if(args[i].equalsIgnoreCase("ALL")) {
                        outputType = BLOX_ALL;
                    }
                }
                else {
                    //defaults to SFS output
                    outputType = BLOX_GABRIEL;
                    i--;
                }
            }
            else if(args[i].equals("-d") || args[i].equals("--dprime")) {
                outputDprime = true;
            }
            else if (args[i].equals("-c")){
                outputCheck = true;
            }
            else if(args[i].equals("-m")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-m requires an integer argument");
                    System.exit(1);
                }
                else {
                    if(maxDistance != -1){
                        System.out.println("only one -m argument allowed");
                        System.exit(1);
                    }
                    maxDistance = Integer.parseInt(args[i]);
                    if(maxDistance<0){
                        System.out.println("-m argument must be a positive integer");
                        System.exit(1);
                    }

                }
            }
            else if(args[i].equals("-b")) {
                //batch mode
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-b requires a filename");
                    System.exit(1);
                }
                else{
                    if(!batchMode.equals("")){
                        System.out.println("multiple -b arguments found. only last batch file listed will be used");
                    }
                    batchMode = args[i];
                }
            }
            else if(args[i].equals("-q")) {
                quietMode = true;
            }
            else {
                System.out.println("invalid parameter specified: " + args[i]);
            }
        }

        //mess with vars, set defaults, etc

        if( outputType == -1 && ( !pedFileName.equals("") ||
                !hapsFileName.equals("") || !batchMode.equals("") || !hapmapFileName.equals(""))
                && !outputDprime && !outputCheck && !outputPNG && !outputSmallPNG) {
            outputType = BLOX_GABRIEL;
            if(nogui && !quietMode) {
                System.out.println("No output type specified. Default of Gabriel will be used");
            }
        }
        if(showCheck && !nogui && !quietMode) {
            System.out.println("pedfile showcheck option only applies in nogui mode. ignored.");
        }
        if(skipCheck && !quietMode) {
            System.out.println("Skipping pedigree file check");
        }
        if(maxDistance == -1){
            maxDistance = 500;
        }

        //set the global variables
        arg_nogui = nogui;
        arg_hapsfile = hapsFileName;
        arg_infoFileName = infoFileName;
        arg_pedfile = pedFileName;
        arg_hapmapfile = hapmapFileName;
        arg_blockfile = blockFileName;
        arg_showCheck = showCheck;
        arg_skipCheck = skipCheck;
        arg_ignoreMarkers = ignoreMarkers;
        arg_output = outputType;
        arg_distance = maxDistance;
        arg_batchMode = batchMode;
        arg_quiet = quietMode;
        arg_dprime = outputDprime;
        arg_png = outputPNG;
        arg_smallpng = outputSmallPNG;
        arg_check = outputCheck;
    }


    private void doBatch() {
        Vector files;
        File batchFile;
        File dataFile;
        String line;
        StringTokenizer tok;
        String infoMaybe ="";

        files = new Vector();
        batchFile = new File(this.arg_batchMode);

        if(!batchFile.exists()) {
            System.out.println("batch file " + this.arg_batchMode + " does not exist");
            System.exit(1);
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(batchFile));
            while( (line = br.readLine()) != null ) {
                files.add(line);
            }
            br.close();

            for(int i = 0;i<files.size();i++){
                line = (String)files.get(i);
                tok = new StringTokenizer(line);
                infoMaybe = "";
                if(tok.hasMoreTokens()){
                    dataFile = new File(tok.nextToken());
                    if(tok.hasMoreTokens()){
                        infoMaybe = tok.nextToken();
                    }

                    if(dataFile.exists()) {
                        String name = dataFile.getName();
                        if( name.substring(name.length()-4,name.length()).equals(".ped") ) {
                            processFile(name,PED,infoMaybe);
                        }
                        else if(name.substring(name.length()-5,name.length()).equals(".haps")) {
                            processFile(name,HAPS,infoMaybe);
                        }
                        else if(name.substring(name.length()-4,name.length()).equals(".hmp")){
                            processFile(name,HMP,"");
                        }
                        else{
                            if (!arg_quiet){
                                System.out.println("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                        name + " is not properly formatted.");
                            }
                        }
                    }
                    else {
                        if(!arg_quiet){
                            System.out.println("file " + dataFile.getName() + " listed in the batch file could not be found");
                        }
                    }
                }

            }
        }
        catch(FileNotFoundException e){
            System.out.println("the following error has occured:\n" + e.toString());
        }
        catch(IOException e){
            System.out.println("the following error has occured:\n" + e.toString());
        }

    }

    /**
     * this method finds haplotypes and caclulates dprime without using any graphics
     */
    private void processTextOnly(){
        String fileName;
        int fileType;
        if(!this.arg_hapsfile.equals("")) {
            fileName = this.arg_hapsfile;
            fileType = HAPS;
        }
        else if (!this.arg_pedfile.equals("")){
            fileName = this.arg_pedfile;
            fileType = PED;
        }else{
            fileName = this.arg_hapmapfile;
            fileType = HMP;
        }

        processFile(fileName,fileType,this.arg_infoFileName);
    }
    /**
     * this
     * @param fileName name of the file to process
     * @param fileType true means pedfilem false means hapsfile
     * @param infoFileName
     */
    private void processFile(String fileName, int fileType, String infoFileName){
        try {
            int outputType;
            long maxDistance;
            HaploData textData;
            File OutputFile;
            File inputFile;

            if(!arg_quiet && fileName != null){
                System.out.println("Using data file " + fileName);
            }

            inputFile = new File(fileName);
            if(!inputFile.exists()){
                System.out.println("input file: " + fileName + " does not exist");
                System.exit(1);
            }

            maxDistance = this.arg_distance * 1000;
            outputType = this.arg_output;



            textData = new HaploData(0);
            Vector result = null;


            if(fileType == HAPS){
                //read in haps file
                textData.prepareHapsInput(inputFile);
            }
            else if (fileType == PED) {
                //read in ped file
              /*  if(this.arg_ignoreMarkers.size()>0) {
                    for(int i=0;i<this.arg_ignoreMarkers.size();i++){
                        int index = Integer.parseInt((String)this.arg_ignoreMarkers.get(i));
                        if(index>0 && index<markerResultArray.length){
                            markerResultArray[index] = false;
                            if(!this.arg_quiet) {
                                System.out.println("Ignoring marker " + (index));
                            }
                        }
                    }
                }*/

                result = textData.linkageToChrom(inputFile, 3, arg_skipCheck);

            }else{
                //read in hapmapfile
                result = textData.linkageToChrom(inputFile,4,arg_skipCheck);
            }

            File infoFile;
            if(infoFileName.equals("")) {
                infoFile = null;
            }else{
                infoFile = new File(infoFileName);
            }
            if (result != null){
                textData.prepareMarkerInput(infoFile,maxDistance,textData.getPedFile().getHMInfo());
            }else{
                textData.prepareMarkerInput(infoFile,maxDistance,null);
            }
            if(!arg_quiet && infoFile != null){
                System.out.println("Using marker file " + infoFile.getName());
            }


            if(this.arg_showCheck && result != null) {
                CheckDataPanel cp = new CheckDataPanel(textData, false);
                cp.printTable(null);
            }

            if(this.arg_check && result != null){
                CheckDataPanel cp = new CheckDataPanel(textData, false);
                cp.printTable(new File (fileName + ".CHECK"));
            }
            Vector cust = new Vector();
            if(outputType != -1){
                textData.generateDPrimeTable();
                Haplotype[][] haplos;
                switch(outputType){
                    case BLOX_GABRIEL:
                        OutputFile = new File(fileName + ".GABRIELblocks");
                        break;
                    case BLOX_4GAM:
                        OutputFile = new File(fileName + ".4GAMblocks");
                        break;
                    case BLOX_SPINE:
                        OutputFile = new File(fileName + ".SPINEblocks");
                        break;
                    case BLOX_CUSTOM:
                        OutputFile = new File(fileName + ".CUSTblocks");
                        //read in the blocks file
                        File blocksFile = new File(arg_blockfile);
                        cust = textData.readBlocks(blocksFile);
                        break;
                    default:
                        OutputFile = new File(fileName + ".GABRIELblocks");
                        break;

                }

                //this handles output type ALL
                int start = 0;
                int stop = Chromosome.getFilteredSize();
                if(outputType == BLOX_ALL) {
                    OutputFile = new File(fileName + ".GABRIELblocks");
                    textData.guessBlocks(BLOX_GABRIEL);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = new File(fileName + ".4GAMblocks");
                    textData.guessBlocks(BLOX_4GAM);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = new File(fileName + ".SPINEblocks");
                    textData.guessBlocks(BLOX_SPINE);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }else{
                    textData.guessBlocks(outputType, cust);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }
            }
            if(this.arg_dprime) {
                OutputFile = new File(fileName + ".DPRIME");
                if (textData.filteredDPrimeTable != null){
                    textData.saveDprimeToText(OutputFile, TABLE_TYPE, 0, Chromosome.getFilteredSize());
                }else{
                    textData.saveDprimeToText(OutputFile, LIVE_TYPE, 0, Chromosome.getFilteredSize());
                }
            }
            if (this.arg_png || this.arg_smallpng){
                OutputFile = new File(fileName + ".LD.PNG");
                if (textData.filteredDPrimeTable == null){
                    textData.generateDPrimeTable();
                    textData.guessBlocks(BLOX_CUSTOM, new Vector());
                }
                if (this.arg_trackName != null){
                    textData.readAnalysisTrack(new File(arg_trackName));
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                BufferedImage i = dpd.export(0,Chromosome.getSize(),this.arg_smallpng);
                try{
                    Jimi.putImage("image/png", i, OutputFile.getName());
                }catch(JimiException je){
                    System.out.println(je.getMessage());
                }
            }

            //if(fileType){
                //TDT.calcTrioTDT(textData.chromosomes);
                //TODO: Deal with this.  why do we calc TDT? and make sure not to do it except when appropriate
            //}
        }
        catch(IOException e){
            System.err.println("An error has occured. This probably has to do with file input or output");
        }
        catch(HaploViewException e){
            System.err.println(e.getMessage());
        }
        catch(PedFileException pfe) {
            System.err.println(pfe.getMessage());
        }
    }


    public static Haplotype[][] orderHaps (Haplotype[][] haplos, HaploData theData) throws HaploViewException{
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
}
