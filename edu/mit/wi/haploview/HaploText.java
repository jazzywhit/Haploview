package edu.mit.wi.haploview;

//import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;

import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;

//import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.validation.EnumValidator;

public class HaploText implements Constants{

    private String batchFileName;
    private String pedFileName;
    private String infoFileName;
    private String hapmapFileName;
    private String hapsFileName;
    private String blockFileName;
    private String trackFileName;
    //private Vector ignoreMarkers;
    private static boolean quietMode;
    private static boolean noGUI;
    private boolean outputPNG;
    private boolean outputSmallPNG;
    private boolean outputDprime;
    private boolean showCheck;
    private boolean skipCheck;
    private boolean outputCheck;
    private int outputType;
    private boolean checkForUpdate;


    private boolean debug=true;

    public static boolean isQuietMode() {
        return quietMode;
    }

    public static boolean isNoGui() {
        return noGUI;
    }

    public String getBatchMode() {
        return batchFileName;
    }

    public String getInfoFileName(){
        return infoFileName;
    }

    public String getHapmapFileName(){
        return hapmapFileName;
    }

    public String getHapsFileName() {
        return hapsFileName;
    }

    public String getPedFileName() {
        return pedFileName;
    }

    public HaploText(String[] args) {
        this.argHandler(args);

        if(HaploText.noGUI) {
            Configuration.readConfigFile();
            if(this.checkForUpdate) {
                UpdateChecker uc = new UpdateChecker();
                if(uc.checkForUpdate()) {
                    System.out.println("A newer version of Haploview is available (current version: "
                            + Constants.VERSION + "\t newest version: " + uc.getNewVersion() + ")");
                    System.out.println("please visit http://www.broad.mit.edu/mpg/haploview/ to download the new version");
                }
            }

            if(this.batchFileName != null) {
                System.out.println(TITLE_STRING);
                this.doBatch();
            }

            if(this.pedFileName != null || this.hapsFileName != null || this.hapmapFileName != null){
                if(HaploText.noGUI){
                    System.out.println(TITLE_STRING);
                    if(Options.getGenoFileType() == PED) {
                        processFile(this.pedFileName,PED,this.infoFileName);
                    }
                    else if(Options.getGenoFileType() == HAPS) {
                        processFile(this.hapsFileName,HAPS,this.infoFileName);
                    }
                    else {
                        processFile(this.hapmapFileName,HMP,this.infoFileName);
                    }
                }
            }
            Configuration.writeConfigFile();
        }
    }

    public void debugPrint(String d) {
        if(debug) {
            System.out.println(d);
        }
    }


    private void argHandler(String[] args) {

        //ArrayList outputOptions = new ArrayList();
        HashSet outputOptions = new HashSet();
        outputOptions.add("GAB");
        outputOptions.add("GAM");
        outputOptions.add("SPI");
        outputOptions.add("MJD");
        outputOptions.add("SFS");
        outputOptions.add("ALL");

        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        this.outputType=-1;

        final Group options =
            gbuilder
                .withName("Haploview")
                .withOption(
                    obuilder
                        .withShortName("help")
                        .withShortName("h")
                        .withDescription("print this message")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("pedfile")
                        .withShortName("p")
                        .withDescription("use the specified pedigree file")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("hapmap")
                        .withShortName("a")
                        .withDescription("use the specified hapmap file")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("haps")
                        .withShortName("ha")
                        .withDescription("use the specified haps file")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("batch")
                        .withShortName("b")
                        .withDescription("batch mode. each line of the batch file should either have just a genotype file or a genotype file and an info file seperated by a space")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("info")
                        .withShortName("i")
                        .withDescription("marker information file")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("skipcheck")
                        .withDescription("skip the various pedfile checks")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("showcheck")
                        .withDescription("displays the results of the various pedigree integrity checks")
                        .create())
/*                .withOption(
                    obuilder
                        .withShortName("ignoremarkers")
                        .withDescription("print this message")
                        .create())*/
                .withOption(
                    obuilder
                        .withShortName("maxdistance")
                        .withShortName("m")
                        .withArgument(
                            abuilder
                            .withName("maximum distance")
                            .withMinimum(1)
                            .withMaximum(1)
                            .create())
                        .withDescription("maximum comparison distance in kilobases (integer). default is 500. use value of 0 for no maximum distance")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("n")
                        .withDescription("no gui mode (command line output only)")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("quiet")
                        .withShortName("q")
                        .withDescription("mimimal output")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("blockfile")
                        .withShortName("k")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .withDescription("blocks file, one block per line, will force output for these blocks")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("png")
                        .withDescription("output a PNG file")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("smallpng")
                        .withDescription("output a small PNG file")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("output")
                        .withShortName("o")
                        .withArgument(
                            abuilder
                                .withName("output type")
                                .withValidator(new EnumValidator(outputOptions))
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .withDescription("output type. GAB (gabriel),GAM (4 gamete),SPI (spine) or ALL. default is GAB")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("dprime")
                        .withShortName("d")
                        .withDescription("output dprime to <inputfile>.DPRIME")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("check")
                        .withShortName("c")
                        .withDescription("output check to <inputfile>.CHECK")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("track")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .withDescription("use analysis track from specified file")
                        .create())
                .withOption(
                        obuilder
                        .withShortName("update")
                        .withDescription("check for update")
                        .create())
                .create();


        Parser parser = new Parser();

        parser.setGroup(options);

        int maxDistance;

        parser.setGroup(options);
        //Parser parser = new Parser();

        try {
            HelpFormatter helpFormatter = new HelpFormatter();
            //ignore all printed
            //helpFormatter.setPrintWriter(new PrintWriter(new StringWriter()));
            helpFormatter.setGroup(options);
            helpFormatter.setShellCommand("haploview");

            parser.setHelpFormatter(helpFormatter);
            parser.setHelpOption(options.findOption("-h"));
            // parse the command line arguments
            CommandLine line = parser.parse(args );
            if(line.hasOption("-h")) {
                try {
                    helpFormatter.print();
                } catch(IOException ioe) {
                    System.err.println(ioe);
                    System.exit(1);
                }
            }
            if(line.hasOption("-update")) {
                this.checkForUpdate = true;
            }
            if(line.hasOption("-p")) {
                if(this.pedFileName != null
                        || this.hapmapFileName != null
                        || this.hapsFileName != null
                        || this.batchFileName != null) {
                    throw new OptionException(options.findOption("-p"),"only allowed one option from: -p,-ha,-a,-b");
                }
                this.pedFileName = (String)line.getValue("-p");
                Options.setGenoFileType(PED);
                debugPrint("found pedfile:\t" + this.pedFileName);
            }
            if(line.hasOption("-ha")) {
                if(this.pedFileName != null
                        || this.hapmapFileName != null
                        || this.hapsFileName != null
                        || this.batchFileName != null) {
                    throw new OptionException(options.findOption("-ha"),"only allowed one option from: -p,-ha,-a,-b");
                }
                this.hapsFileName = (String)line.getValue("-ha");
                Options.setGenoFileType(HAPS);
                debugPrint("found hapsfile:\t" + this.hapsFileName);
            }
            if(line.hasOption("-a")) {
                if(this.pedFileName != null
                        || this.hapmapFileName != null
                        || this.hapsFileName != null
                        || this.batchFileName != null) {
                    throw new OptionException(options.findOption("-a"),"only allowed one option from: -p,-ha,-a,-b");
                }
                this.hapmapFileName = (String)line.getValue("-a");
                Options.setGenoFileType(HMP);
                debugPrint("found pedfile:\t" + this.hapmapFileName);
            }
            if(line.hasOption("-b")){
                if(this.pedFileName != null
                        || this.hapmapFileName != null
                        || this.hapsFileName != null
                        || this.batchFileName != null) {
                    throw new OptionException(options.findOption("-b"),"only allowed one option from: -p,-ha,-a,-b");
                }
                this.batchFileName = (String)line.getValue("-b");
                debugPrint("using batch mode file:\t" + this.batchFileName);
            }
            if(line.hasOption("-i")) {
                this.infoFileName = (String)line.getValue("-i");
                debugPrint("found infofile:\t" + this.infoFileName);
            }

            if(line.hasOption("-n")) {
                HaploText.noGUI = true;
                debugPrint("no gui mode");
            }

            if(line.hasOption("-showcheck")) {
                this.showCheck = true;
                debugPrint("showing check");
            }
            if(line.hasOption("-skipcheck")) {
                this.skipCheck = true;
                debugPrint("skiping check");
            }
            //if(line.hasOption("-ignoremarkers")){
                //String[] markers = (String)line.getValues("-ignoremarkers");
                //debugPrint("ignoring markers:\t" + markers.toString());
            //}

            if(line.hasOption("-k")) {
                this.blockFileName = (String)line.getValue("-k");
                this.outputType = BLOX_CUSTOM;
                debugPrint("using blocks file:\t" + this.blockFileName);
            }
            if(line.hasOption("-png")) {
                this.outputPNG = true;
                debugPrint("outputing png");
            }
            if(line.hasOption("-smallpng")) {
                this.outputSmallPNG = true;
                debugPrint("outputing small png");
            }
            if(line.hasOption("-track"))   {
                this.trackFileName = (String)line.getValue("-track");
                debugPrint("using track file:\t" + this.trackFileName);
            }
            if(line.hasOption("-o") && this.outputType != BLOX_CUSTOM) {
                String temp = (String)line.getValue("-o");
                if(temp.equalsIgnoreCase("SFS") || temp.equalsIgnoreCase("GAB")){
                    this.outputType = BLOX_GABRIEL;
                }
                else if(temp.equalsIgnoreCase("GAM")){
                    this.outputType =BLOX_4GAM;
                }
                else if(temp.equalsIgnoreCase("MJD") || temp.equalsIgnoreCase("SPI")){
                    this.outputType =BLOX_SPINE;
                }
                else if(temp.equalsIgnoreCase("ALL")) {
                    this.outputType = BLOX_ALL;
                }
                else {
                    throw new OptionException(options.findOption("-o"),"unknown output type specified");
                }
            }
            if(line.hasOption("-d")){
                this.outputDprime = true;
                debugPrint("outputing dprime");
            }
            if(line.hasOption("-c")){
                this.outputCheck = true;
                debugPrint("outputing check");
            }
            if(line.hasOption("-m")){
                try {
                    maxDistance = Integer.parseInt((String)line.getValue("-m"));
                }
                catch(NumberFormatException nfe) {
                    throw new OptionException(options.findOption("-m"),"invalid value for maximum distance");
                }
                if(maxDistance < 0 ) {
                    throw new OptionException(options.findOption("-m"),"maximum distance cannot be negative");
                }
                Options.setMaxDistance(maxDistance);
                debugPrint("using max distance:\t" + maxDistance);
            }


            if(line.hasOption("-q")) {
                HaploText.quietMode = true;
                debugPrint("quiet mode");
            }

            if( this.outputType == -1 && ( this.pedFileName != null ||
                    this.hapsFileName != null || this.batchFileName != null || this.hapmapFileName != null)
                    && !this.outputDprime && !this.outputCheck && !this.outputPNG && !this.outputSmallPNG) {
                this.outputType = BLOX_GABRIEL;
                if(HaploText.noGUI && !HaploText.quietMode) {
                    System.out.println("No output type specified. Default of Gabriel will be used");
                }
            }


        }
        catch( OptionException exp ) {
            // oops, something went wrong
            System.err.println( "There was a problem with at least one of the options used: " + exp.getMessage() );
            System.exit(1);
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
        batchFile = new File(this.batchFileName);

        if(!batchFile.exists()) {
            System.out.println("batch file " + this.batchFileName + " does not exist");
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
                            if (!HaploText.quietMode){
                                System.out.println("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                        name + " is not properly formatted.");
                            }
                        }
                    }
                    else {
                        if(!HaploText.quietMode){
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
     * this
     * @param fileName name of the file to process
     * @param fileType true means pedfilem false means hapsfile
     * @param infoFileName
     */
    private void processFile(String fileName, int fileType, String infoFileName){
        try {
            //int outputType;
            HaploData textData;
            File OutputFile;
            File inputFile;

            if(!HaploText.quietMode && fileName != null){
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
                            if(!this.arg_quiet) {
                                System.out.println("Ignoring marker " + (index));
                            }
                        }
                    }
                }*/

                result = textData.linkageToChrom(inputFile, 3, this.skipCheck);

            }else{
                //read in hapmapfile
                result = textData.linkageToChrom(inputFile,4,this.skipCheck);
            }

            File infoFile = null;

            if(infoFileName != null){
                infoFile = new File(infoFileName);
            }

            if (result != null){
                textData.prepareMarkerInput(infoFile,textData.getPedFile().getHMInfo());
            }else{
                textData.prepareMarkerInput(infoFile,null);
            }

            if(!HaploText.quietMode && infoFile != null){
                System.out.println("Using marker file " + infoFile.getName());
            }

            if(this.showCheck && result != null) {
                CheckDataPanel cp = new CheckDataPanel(textData, false);
                cp.printTable(null);
            }

            if(this.outputCheck && result != null){
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
                        File blocksFile = new File(this.blockFileName);
                        cust = textData.readBlocks(blocksFile);
                        break;
                    default:
                        OutputFile = new File(fileName + ".GABRIELblocks");
                        break;

                }

                //this handles output type ALL
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
            if(this.outputDprime) {
                OutputFile = new File(fileName + ".DPRIME");
                if (textData.dpTable != null){
                    textData.saveDprimeToText(OutputFile, TABLE_TYPE, 0, Chromosome.getSize());
                }else{
                    textData.saveDprimeToText(OutputFile, LIVE_TYPE, 0, Chromosome.getSize());
                }
            }
            if (this.outputPNG || this.outputSmallPNG){
                OutputFile = new File(fileName + ".LD.PNG");
                if (textData.dpTable == null){
                    textData.generateDPrimeTable();
                    textData.guessBlocks(BLOX_CUSTOM, new Vector());
                }
                if (this.trackFileName != null){
                    textData.readAnalysisTrack(new File(this.trackFileName));
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                BufferedImage i = dpd.export(0,Chromosome.getSize(),this.outputSmallPNG);
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
