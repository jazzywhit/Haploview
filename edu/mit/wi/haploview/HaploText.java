package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.MarkerResult;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class HaploText {

    private boolean arg_nogui = false;
    private String arg_batchMode;
    private String arg_hapsfile;
    private String arg_pedfile;
    private boolean arg_showCheck = false;
    private int arg_output;
    private int arg_distance;

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

        if(!(this.arg_pedfile).equals("") || !(this.arg_hapsfile).equals("") ){
            //System.out.println("pedfile!\t" + pedFileName);
            if(arg_nogui){
                processTextOnly();
            }
        }

    }

    private void argHandler(String[] args){

        // TODO:-want to be able to output dprime
        //      -info file flag
        //      -info files in batch file
        //      -specify values from HaplotypeDisplayController (min hap percentage etc)
        boolean nogui = false;
        String batchMode = "";
        String hapsFileName = "";
        String pedFileName = "";
        boolean showCheck = false;
        int outputType = -1;
        int maxDistance = -1;

        for(int i =0; i < args.length; i++) {
            if(args[i].equals("-help") || args[i].equals("-h")) {
                System.out.println("HaploView command line options\n" +
                        "-h, -help                     print this message\n" +
                        "-n, -nogui                    command line output only\n" +
                        //"-p <pedfile>               specify an input file in pedigree file format\n" +
                        "-p <pedfile> [options]        specify an input file in pedigree file format\n" +
                        "              pedfile options (nogui mode only): \n" +
                        "              showcheck       displays the results of the various pedigree integrity checks\n" +
                        "-ha <hapsfile>                specify an input file in .haps format\n" +
                        "-b <batchfile>                batch mode. batchfile should contain a list of haps files\n" +
                        "-o <SFS,GAM,MJD>              output type. SFS, 4 gamete or MJD output. default is SFS.\n" +
                        "-m <distance>                 maximum comparison distance in kilobases (integer). default is 200");

                System.exit(0);

            }
            else if(args[i].equals("-nogui") || args[i].equals("-n")) {
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
                    if (args[i+1].equals("showcheck")){
                        showCheck = true;
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
                    if(!hapsFileName.equals("")){
                        System.out.println("multiple -ha arguments found. only last haps file listed will be used");
                    }
                    hapsFileName = args[i];
                }
            }
            else if(args[i].equals("-o")) {
                i++;
                if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    if(outputType != -1){
                        System.out.println("only one -o argument is allowed");
                        System.exit(1);
                    }
                    if(args[i].equals("SFS")){
                        outputType = 0;
                    }
                    else if(args[i].equals("GAM")){
                        outputType = 1;
                    }
                    else if(args[i].equals("MJD")){
                        outputType = 2;
                    }
                }
                else {
                    //defaults to SFS output
                    outputType =0;
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
            else {
                System.out.println("invalid parameter specified: " + args[i]);
            }
        }

        //mess with vars, set defaults, etc

        if( outputType == -1 && ( !pedFileName.equals("") || !hapsFileName.equals("") ) ) {
            outputType = 0;
            if(nogui) {
                System.out.println("No output type specified. Default of SFS will be used");
            }
        }
        if(showCheck && !nogui) {
            System.out.println("pedfile showcheck option only applies in nogui mode. ignored.");
        }
        if(maxDistance == -1){
            maxDistance = 200;
        }

        //set the global variables
        arg_nogui = nogui;
        arg_hapsfile = hapsFileName;
        arg_pedfile = pedFileName;
        arg_showCheck = showCheck;
        arg_output = outputType;
        arg_distance = maxDistance;
        arg_batchMode = batchMode;

    }

        /**
     * this method finds haplotypes and caclulates dprime without using any graphics
     */
    private void processTextOnly(){
        try {
            String fileName;
            int outputType;
            long maxDistance;
            HaploData textData;
            File OutputFile;
            File inputFile;
            //we use this boolean to keep track of the type of file we're processing (haps or ped)
            //false means haps, true means ped.
            boolean fileType = false;


            if(!this.arg_hapsfile.equals("")) {
                fileName = this.arg_hapsfile;
                fileType = false;
            }
            else {
                fileName = this.arg_pedfile;
                fileType = true;
            }

            inputFile = new File(fileName);
            if(!inputFile.exists()){
                System.out.println("input file: " + fileName + " does not exist");
                System.exit(1);
            }

            maxDistance = this.arg_distance * 1000;
            outputType = this.arg_output;

            switch(outputType){
                case 1:
                    OutputFile = new File(fileName + ".4GAMblocks");
                    break;
                case 2:
                    OutputFile = new File(fileName + ".MJDblocks");
                    break;
                default:
                    OutputFile = new File(fileName + ".SFSblocks");
                    break;
            }

            textData = new HaploData();

            if(!fileType){
                //read in haps file
                textData.prepareHapsInput(inputFile);
            }
            else {
                //read in ped file
                PedFile ped;
                Vector pedFileStrings;
                BufferedReader reader;
                String line;
                Vector result;
                boolean[] markerResultArray;

                ped = new PedFile();
                pedFileStrings = new Vector();
                reader = new BufferedReader(new FileReader(inputFile));

                while((line = reader.readLine())!=null){
                    pedFileStrings.add(line);
                }

                ped.parse(pedFileStrings);
                result = ped.check();

                if(this.arg_showCheck) {
                    System.out.println("Data check results:\n" +
                            "Name\tObsHET\tPredHET\tHWpval\t%Geno\tFamTrio\tMendErr\tRating");
                    for(int i=0;i<result.size();i++){
                        MarkerResult currentResult = (MarkerResult)result.get(i);
                        System.out.println(
                                currentResult.getName()        +"\t"+
                                currentResult.getObsHet()      +"\t"+
                                currentResult.getPredHet()     +"\t"+
                                currentResult.getHWpvalue()    +"\t"+
                                currentResult.getGenoPercent() +"\t"+
                                currentResult.getFamTrioNum()  +"\t"+
                                currentResult.getMendErrNum());
                    }

                }




                markerResultArray = new boolean[ped.getNumMarkers()];
                for (int i = 0; i < markerResultArray.length; i++){
                    if(((MarkerResult)result.get(i)).getRating() > 0) {
                        markerResultArray[i] = true;
                    }
                    else {
                        markerResultArray[i] = false;
                    }
                }

                textData.linkageToChrom(markerResultArray,ped);

            }


            String name = fileName;
            String baseName = fileName.substring(0,name.length()-5);
            File maybeInfo = new File(baseName + ".info");
            if (maybeInfo.exists()){
                textData.prepareMarkerInput(maybeInfo,arg_distance);
            }


            textData.generateDPrimeTable(maxDistance);
            Haplotype[][] haplos;

            textData.guessBlocks(outputType);
            haplos = textData.generateHaplotypes(textData.blocks, 1);
            new TextMethods().saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
        }
        catch(IOException e){}
    }


    public static Haplotype[][] orderHaps (Haplotype[][] haplos, HaploData theData){
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
