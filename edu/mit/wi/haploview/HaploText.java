package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.CheckData;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.image.BufferedImage;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;

public class HaploText implements Constants{

    private boolean nogui = false;
    private String batchFileName;
    private String hapsFileName;
    private String infoFileName;
    private String pedFileName;
    private String hapmapFileName;
    private String blockFileName;
    private String trackFileName;
    private boolean skipCheck = false;
    private Vector ignoreMarkers = new Vector();
    private boolean quietMode = false;
    private int outputType;
    private boolean outputCheck;
    private boolean outputDprime;
    private boolean outputPNG;
    private boolean outputCompressedPNG;



    public String getBatchMode() {
        return batchFileName;
    }

    public String getHapsFileName() {
        return hapsFileName;
    }

    public String getPedFileName() {
        return pedFileName;
    }

    public String getInfoFileName(){
        return infoFileName;
    }

    public String getHapmapFileName(){
        return hapmapFileName;
    }


    public int getOutputType() {
        return outputType;
    }

    public HaploText(String[] args) {
        this.argHandler(args);

        if(this.batchFileName != null) {
            System.out.println(TITLE_STRING);
            this.doBatch();
        }

        if(!(this.pedFileName== null) || !(this.hapsFileName== null) || !(this.hapmapFileName== null)){
            if(nogui){
                System.out.println(TITLE_STRING);
                processTextOnly();
            }
        }

    }

    private void argHandler(String[] args){

        //TODO: -specify values from HaplotypeDisplayController (min hap percentage etc)
        //TODO:      -want to be able to output haps file from pedfile
       /* boolean nogui = false;
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
        boolean outputCheck=false;*/

        int maxDistance = -1;
        //this means that user didn't specify any output type if it doesn't get changed below
        outputType = -1;
        double hapThresh = -1;
        double minimumMAF=-1;
        double spacingThresh = -1;


        for(int i =0; i < args.length; i++) {
            if(args[i].equals("-help") || args[i].equals("-h")) {
                System.out.println(HELP_OUTPUT);
                System.exit(0);
            }
            else if(args[i].equals("-n") || args[i].equals("-nogui")) {
                nogui = true;
            }
            else if(args[i].equals("-p") || args[i].equals("-pedfile")) {
                i++;
                if( i>=args.length || (args[i].charAt(0) == '-')){
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
                else{
                    if(pedFileName != null){
                        System.out.println("multiple "+args[i-1] + " arguments found. only last pedfile listed will be used");
                    }
                    pedFileName = args[i];
                }
            }
            else if (args[i].equals("-skipcheck") || args[i].equals("--skipcheck")){
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
            else if(args[i].equals("-ha") || args[i].equals("-l") || args[i].equals("-haps")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
                else{
                    if(hapsFileName != null){
                        System.out.println("multiple "+args[i-1] + " arguments found. only last haps file listed will be used");
                    }
                    hapsFileName = args[i];
                }
            }
            else if(args[i].equals("-i") || args[i].equals("-info")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
                else{
                    if(infoFileName != null){
                        System.out.println("multiple "+args[i-1] + " arguments found. only last info file listed will be used");
                    }
                    infoFileName = args[i];
                }
            } else if (args[i].equals("-a") || args[i].equals("-hapmap")){
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
                else{
                    if(hapmapFileName != null){
                        System.out.println("multiple "+args[i-1] + " arguments found. only last hapmap file listed will be used");
                    }
                    hapmapFileName = args[i];
                }
            }
            else if(args[i].equals("-k") || args[i].equals("-blocks")) {
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    blockFileName = args[i];
                    outputType = BLOX_CUSTOM;
                }else{
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
            }
            else if (args[i].equalsIgnoreCase("-png")){
                outputPNG = true;
            }
            else if (args[i].equalsIgnoreCase("-smallpng") || args[i].equalsIgnoreCase("-compressedPNG")){
                outputCompressedPNG = true;
            }
            else if (args[i].equals("-track")){
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                   trackFileName = args[i];
                }else{
                    System.out.println("-track requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equals("-o") || args[i].equals("-output") || args[i].equalsIgnoreCase("-blockoutput")) {
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
            else if(args[i].equals("-d") || args[i].equals("--dprime") || args[i].equals("-dprime")) {
                outputDprime = true;
            }
            else if (args[i].equals("-c") || args[i].equals("-check")){
                outputCheck = true;
            }
            else if(args[i].equals("-m") || args[i].equals("-maxdistance")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println(args[i-1] + " requires an integer argument");
                    System.exit(1);
                }
                else {
                    if(maxDistance != -1){
                        System.out.println("only one "+args[i-1] + " argument allowed");
                        System.exit(1);
                    }
                    maxDistance = Integer.parseInt(args[i]);
                    if(maxDistance<0){
                        System.out.println(args[i-1] + " argument must be a positive integer");
                        System.exit(1);
                    }

                }
            }
            else if(args[i].equals("-b") || args[i].equals("-batch")) {
                //batch mode
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
                else{
                    if(batchFileName != null){
                        System.out.println("multiple " + args[i-1] +  " arguments found. only last batch file listed will be used");
                    }
                    batchFileName = args[i];
                }
            }
            else if(args[i].equals("-hapthresh")) {
                try {
                    i++;
                    if(i>=args.length || ((args[i].charAt(0)) == '-'))   {
                        System.out.println("-hapthresh requires a value between 0 and 1");
                        System.exit(1);
                    }
                    hapThresh = Double.parseDouble(args[i]);
                    if(hapThresh<0 || hapThresh>1) {
                        System.out.println("Haplotype threshold must be between 0 and 1");
                        System.exit(1);
                    }

                }catch(NumberFormatException nfe) {
                    System.out.println("Haplotype threshold must be a number between 0 and 1");
                    System.exit(1);
                }

            }
            else if(args[i].equals("-spacing")) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')) {
                    System.out.println("-spacing requires a value between 0 and 1");
                    System.exit(1);
                }
                try {
                    spacingThresh = Double.parseDouble(args[i]);
                    if(spacingThresh<0 || spacingThresh>1) {
                        System.out.println("-spacing argument must be between 0 and 1");
                        System.exit(1);
                    }
                }catch(NumberFormatException nfe) {
                    System.out.println("-spacing argument must be a number between 0 and 1");
                    System.exit(1);
                }
            }
            else if(args[i].equalsIgnoreCase("-minMAF")) {
               i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')) {
                    System.out.println("-minMAF requires a value between 0 and 1");
                    System.exit(1);
                }
                try {
                    double thresh = Double.parseDouble(args[i]);
                    if(thresh<0 || thresh>1) {
                        System.out.println("-minMAF argument must be a value between 0 and 1");
                        System.exit(1);
                    }
                    minimumMAF = thresh;
                }catch(NumberFormatException nfe) {
                    System.out.println("-minMAF argument must be a value between 0 and 1");
                    System.exit(1);
                }


            }



            else if(args[i].equals("-q") || args[i].equals("-quiet")) {
                quietMode = true;
            }
            else {
                System.out.println("invalid parameter specified: " + args[i]);
            }
        }

        int countOptions = 0;
        if(pedFileName != null) {
            countOptions++;
        }
        if(hapsFileName != null) {
            countOptions++;
        }
        if(hapmapFileName != null) {
            countOptions++;
        }
        if(batchFileName != null) {
            countOptions++;
        }
        if(countOptions > 1) {
            System.out.println("Only one genotype input file may be specified on the command line.");
            System.exit(1);
        }
        else if(countOptions == 0 && nogui) {
            System.out.println("You must specify a genotype input file.");
            System.exit(1);
        }

        //mess with vars, set defaults, etc

        if( outputType == -1 && ( pedFileName != null ||
                hapsFileName != null || batchFileName != null || hapmapFileName != null)
                && !outputDprime && !outputCheck && !outputPNG && !outputCompressedPNG) {
            outputType = BLOX_GABRIEL;
            if(nogui && !quietMode) {
                System.out.println("No output type specified. Default of Gabriel will be used");
            }
        }
        if(skipCheck && !quietMode) {
            System.out.println("Skipping genotype file check");
        }
        if(maxDistance == -1){
            maxDistance = 500;
        }

        Options.setMaxDistance(maxDistance);

        if(hapThresh != -1) {
            Options.setHaplotypeDisplayThreshold((int)(hapThresh*100));
        }
        
        if(minimumMAF != -1) {
            CheckData.mafCut = minimumMAF;
        }
        if(spacingThresh != -1) {
            Options.setSpacingThreshold(spacingThresh);
        }
    }


    private void doBatch() {
        Vector files;
        File batchFile;
        File dataFile;
        String line;
        StringTokenizer tok;
        String infoMaybe ="";

        files = new Vector();
        if(batchFileName == null) {
            return;
        }
        batchFile = new File(this.batchFileName);

        if(!batchFile.exists()) {
            System.out.println("batch file " + batchFileName + " does not exist");
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
                            if (!quietMode){
                                System.out.println("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                        name + " is not properly formatted.");
                            }
                        }
                    }
                    else {
                        if(!quietMode){
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

    private File validateOutputFile(String fn){
        File f = new File(fn);
        if (f.exists() && !quietMode){
            System.out.println("File " + f.getName() + " already exists and will be overwritten.");
        }
        return f;
    }


    /**
     * this method finds haplotypes and caclulates dprime without using any graphics
     */
    private void processTextOnly(){
        String fileName;
        int fileType;
        if(hapsFileName != null) {
            fileName = hapsFileName;
            fileType = HAPS;
        }
        else if (pedFileName != null){
            fileName = pedFileName;
            fileType = PED;
        }else{
            fileName = hapmapFileName;
            fileType = HMP;
        }

        processFile(fileName,fileType,infoFileName);
    }
    /**
     * this
     * @param fileName name of the file to process
     * @param fileType true means pedfilem false means hapsfile
     * @param infoFileName
     */
    private void processFile(String fileName, int fileType, String infoFileName){
        try {
            HaploData textData;
            File OutputFile;
            File inputFile;



            if(!quietMode && fileName != null){
                System.out.println("Using data file " + fileName);
            }

            inputFile = new File(fileName);
            if(!inputFile.exists()){
                System.out.println("input file: " + fileName + " does not exist");
                System.exit(1);
            }



            textData = new HaploData();
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
                            if(!this.quietMode) {
                                System.out.println("Ignoring marker " + (index));
                            }
                        }
                    }
                }*/

                result = textData.linkageToChrom(inputFile, 3, skipCheck);

            }else{
                //read in hapmapfile
                result = textData.linkageToChrom(inputFile,4,skipCheck);
            }

            File infoFile = null;
            if (infoFileName != null){
                infoFile = new File(infoFileName);
            }
            if (result != null){
                textData.prepareMarkerInput(infoFile,textData.getPedFile().getHMInfo());
            }else{
                textData.prepareMarkerInput(infoFile,null);
            }
            if(!quietMode && infoFile != null){
                System.out.println("Using marker file " + infoFile.getName());
            }
            if(outputCheck && result != null){
                CheckDataPanel cp = new CheckDataPanel(textData, false);
                cp.printTable(validateOutputFile(fileName + ".CHECK"));
            }
            Vector cust = new Vector();
            if(outputType != -1){
                textData.generateDPrimeTable();
                Haplotype[][] haplos;
                switch(outputType){
                    case BLOX_GABRIEL:
                        OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                        break;
                    case BLOX_4GAM:
                        OutputFile = validateOutputFile(fileName + ".4GAMblocks");
                        break;
                    case BLOX_SPINE:
                        OutputFile = validateOutputFile(fileName + ".SPINEblocks");
                        break;
                    case BLOX_CUSTOM:
                        OutputFile = validateOutputFile(fileName + ".CUSTblocks");
                        //read in the blocks file
                        File blocksFile = new File(blockFileName);
                        cust = textData.readBlocks(blocksFile);
                        break;
                    default:
                        OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                        break;

                }

                //this handles output type ALL
                if(outputType == BLOX_ALL) {
                    OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                    textData.guessBlocks(BLOX_GABRIEL);
                    haplos = textData.generateHaplotypes(textData.blocks, 1, false);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = validateOutputFile(fileName + ".4GAMblocks");
                    textData.guessBlocks(BLOX_4GAM);
                    haplos = textData.generateHaplotypes(textData.blocks, 1, false);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = validateOutputFile(fileName + ".SPINEblocks");
                    textData.guessBlocks(BLOX_SPINE);
                    haplos = textData.generateHaplotypes(textData.blocks, 1, false);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }else{
                    textData.guessBlocks(outputType, cust);
                    haplos = textData.generateHaplotypes(textData.blocks, 1, false);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }
            }
            if(outputDprime) {
                OutputFile = validateOutputFile(fileName + ".LD");
                if (textData.dpTable != null){
                    textData.saveDprimeToText(OutputFile, TABLE_TYPE, 0, Chromosome.getSize());
                }else{
                    textData.saveDprimeToText(OutputFile, LIVE_TYPE, 0, Chromosome.getSize());
                }
            }
            if (outputPNG || outputCompressedPNG){
                OutputFile = validateOutputFile(fileName + ".LD.PNG");
                if (textData.dpTable == null){
                    textData.generateDPrimeTable();
                    textData.guessBlocks(BLOX_CUSTOM, new Vector());
                }
                if (trackFileName != null){
                    textData.readAnalysisTrack(new File(trackFileName));
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                BufferedImage i = dpd.export(0,Chromosome.getSize(),outputCompressedPNG);
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
