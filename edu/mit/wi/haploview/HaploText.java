package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;

import java.io.*;
import java.util.Vector;

public class HaploText {

    private boolean arg_nogui = false;
    private String arg_batchMode;
    private String arg_hapsfile;
    private String arg_infoFileName;
    private String arg_pedfile;
    private String arg_hapmapfile;
    private boolean arg_showCheck = false;
    private boolean arg_skipCheck = false;
    private Vector arg_ignoreMarkers = new Vector();
    private boolean arg_quiet = false;
    private int arg_output;
    private int arg_distance;
    private boolean arg_dprime;

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
            this.doBatch();
        }

        if(!(this.arg_pedfile.equals("")) || !(this.arg_hapsfile.equals("")) || !(this.arg_hapmapfile.equals(""))){
            //System.out.println("pedfile!\t" + pedFileName);
            if(arg_nogui){
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
        boolean showCheck = false;
        boolean skipCheck = false;
        Vector ignoreMarkers = new Vector();
        int outputType = -1;
        int maxDistance = -1;
        boolean quietMode = false;
        boolean outputDprime=false;

        for(int i =0; i < args.length; i++) {
            if(args[i].equals("-help") || args[i].equals("-h")) {
                System.out.println("HaploView command line options\n" +
                        "-h, -help                     print this message\n" +
                        "-n                            command line output only\n" +
                        "-q                            quiet mode- doesnt print any warnings or information to screen\n" +
                        "-p <pedfile>                  specify an input file in pedigree file format\n" +
                        "         pedfile specific options (nogui mode only): \n" +
                        "         --showcheck       displays the results of the various pedigree integrity checks\n" +
                        "         --skipcheck       skips the various pedfile checks\n" +
                        //TODO: fix ignoremarkers
                        //"         --ignoremarkers <markers> ignores the specified markers.<markers> is a comma\n" +
                        //"                                   seperated list of markers. eg. 1,5,7,19,25\n" +
                        "-ha <hapsfile>                specify an input file in .haps format\n" +
                        "-a <hapmapfile>               specify an input file in HapMap format\n" +
                        "-i <infofile>                 specify a marker info file\n" +
                        "-b <batchfile>                batch mode. batchfile should contain a list of files either all genotype or alternating genotype/info\n" +
                        "-d                      outputs dprime to <inputfile>.DPRIME\n" +
                        "             note: -d defaults to no blocks output. use -o to also output blocks\n" +
                        "-o <SFS,GAM,MJD,ALL>          output type. SFS, 4 gamete, MJD output or all 3. default is SFS.\n" +
                        "-m <distance>                 maximum comparison distance in kilobases (integer). default is 500");

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
                    else if(args[i].equals("ALL")) {
                        outputType = 3;
                    }
                }
                else {
                    //defaults to SFS output
                    outputType =0;
                    i--;
                }
            }
            else if(args[i].equals("-d") || args[i].equals("--dprime")) {
                outputDprime = true;
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

        if( outputType == -1 && ( !pedFileName.equals("") || !hapsFileName.equals("") || !batchMode.equals("")) && !outputDprime ) {
            outputType = 0;
            if(nogui && !quietMode) {
                System.out.println("No output type specified. Default of SFS will be used");
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
        arg_showCheck = showCheck;
        arg_skipCheck = skipCheck;
        arg_ignoreMarkers = ignoreMarkers;
        arg_output = outputType;
        arg_distance = maxDistance;
        arg_batchMode = batchMode;
        arg_quiet = quietMode;
        arg_dprime = outputDprime;
    }


    private void doBatch() {
        //TODO: batch files should contain lines of either <hapsfile> or <hapsfile infofile>  or <pedfile> or <pedfile infofile>
        Vector files;
        File batchFile;
        File dataFile;
        String line;

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
                dataFile = new File((String)files.get(i));
                if(dataFile.exists()) {
                    String name = dataFile.getName();
                    String infoMaybe ="";
                    if(files.size()>(i+1)) {
                        infoMaybe = (String)files.get(i+1);
                        if(infoMaybe.substring(infoMaybe.length()-5,infoMaybe.length()).equals(".info") ) {
                            //the file on the next line ends in .info, so we assume its the marker info file
                            //for the file on the current line
                            i++;
                        }
                        else {
                            infoMaybe = "";
                        }
                    }

                    if( name.substring(name.length()-4,name.length()).equals(".ped") ) {
                        processFile(name,1,infoMaybe);
                    }
                    else {
                        processFile(name,0,infoMaybe);
                    }
                }
                else {
                    if(!arg_quiet){
                        System.out.println("file " + dataFile.getName() + " listed in the batch file could not be found");
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
            fileType = 0;
        }
        else if (!this.arg_pedfile.equals("")){
            fileName = this.arg_pedfile;
            fileType = 1;
        }else{
            fileName = this.arg_hapmapfile;
            fileType = 2;
        }

        processFile(fileName,fileType,this.arg_infoFileName);

    }
    /**
     * this
     * @param fileName name of the file to process
     * @param fileType true means pedfilem false means hapsfile
     * @param infoFileName
     */
    private void processFile(String fileName,int fileType,String infoFileName){
        try {
            int outputType;
            long maxDistance;
            long negMaxDistance;
            HaploData textData;
            File OutputFile;
            File inputFile;

            inputFile = new File(fileName);
            if(!inputFile.exists()){
                System.out.println("input file: " + fileName + " does not exist");
                System.exit(1);
            }

            maxDistance = this.arg_distance * 1000;
            negMaxDistance = -maxDistance;
            outputType = this.arg_output;



            textData = new HaploData();
            Vector result = null;

            if(fileType == 0){
                //read in haps file
                textData.prepareHapsInput(inputFile);
            }
            else if (fileType == 1) {
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

                textData.linkageToChrom(inputFile, 3, arg_skipCheck);

            }else{
                //read in hapmapfile
                textData.linkageToChrom(inputFile,4,arg_skipCheck);
            }


            String name = fileName;
            String baseName = fileName.substring(0,name.length()-5);

            if(!infoFileName.equals("")) {
                File infoFile = new File(infoFileName);
                if(infoFile.exists()) {
                    textData.prepareMarkerInput(infoFile,maxDistance,null);
                    if(!arg_quiet){
                        System.out.println("Using marker file " + infoFile.getName());
                    }
                    textData.infoKnown = true;
                }
                else if(!this.arg_quiet) {
                    System.out.println("info file " + infoFileName + " does not exist");
                }
            }
            else {
                File maybeInfo = new File(baseName + ".info");
                if (maybeInfo.exists()){
                    textData.prepareMarkerInput(maybeInfo,maxDistance,null);
                    if(!arg_quiet){
                        System.out.println("Using marker file " + maybeInfo.getName());
                    }
                    textData.infoKnown = true;
                }
            }

            if(this.arg_showCheck && result != null) {
                System.out.println("Data check results:\n" +
                        "Name\t\tObsHET\tPredHET\tHWpval\t%Geno\tFamTrio\tMendErr");
                for(int i=0;i<result.size();i++){
                    MarkerResult currentResult = (MarkerResult)result.get(i);
                    System.out.println(
                            Chromosome.getMarker(i).getName()        +"\t"+
                            currentResult.getObsHet()      +"\t"+
                            currentResult.getPredHet()     +"\t"+
                            currentResult.getHWpvalue()    +"\t"+
                            currentResult.getGenoPercent() +"\t"+
                            currentResult.getFamTrioNum()  +"\t"+
                            currentResult.getMendErrNum());
                }

            }



            if(outputType != -1){
                textData.generateDPrimeTable(maxDistance);
                Haplotype[][] haplos;
                switch(outputType){
                    case 0:
                        OutputFile = new File(fileName + ".SFSblocks");
                        break;
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

                //this handles output type ALL
                if(outputType == 3) {
                    OutputFile = new File(fileName + ".SFSblocks");
                    textData.guessBlocks(0);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = new File(fileName + ".4GAMblocks");
                    textData.guessBlocks(1);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                    OutputFile = new File(fileName + ".MJDblocks");
                    textData.guessBlocks(2);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }else{
                    textData.guessBlocks(outputType);
                    haplos = textData.generateHaplotypes(textData.blocks, 1);
                    textData.saveHapsToText(orderHaps(haplos, textData), textData.getMultiDprime(), OutputFile);
                }
            }
            if(this.arg_dprime) {
                OutputFile = new File(fileName + ".DPRIME");
                if (textData.filteredDPrimeTable != null){
                    textData.saveDprimeToText(OutputFile);
                }else{
                    //this means that we're just writing dprime so we won't
                    //keep the (potentially huge) dprime table in memory but instead
                    //write out one line at a time forget
                    FileWriter saveDprimeWriter = new FileWriter(OutputFile);
                    if (textData.infoKnown){
                        saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tDist\n");
                        long dist;
                        PairwiseLinkage linkageResult;

                        for (int i = 0; i < Chromosome.getFilteredSize(); i++){
                            for (int j = 0; j < Chromosome.getFilteredSize(); j++){
                                //many "slots" in table aren't filled in because it is a 1/2 matrix
                                if (i < j){
                                    dist = (Chromosome.getFilteredMarker(j)).getPosition() - (Chromosome.getFilteredMarker(i)).getPosition();
                                    if (maxDistance > 0){
                                        if ((dist > maxDistance || dist < negMaxDistance)){
                                            continue;
                                        }
                                    }
                                    linkageResult = textData.computeDPrime(Chromosome.realIndex[i],Chromosome.realIndex[j]);
                                    if(linkageResult != null) {
                                        saveDprimeWriter.write(Chromosome.getFilteredMarker(i).getName() +
                                                "\t" + Chromosome.getFilteredMarker(j).getName() +
                                                "\t" + linkageResult.toString() + "\t" + dist + "\n");
                                    }
                                }
                            }
                        }
                    }else{
                        saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\n");
                        long dist;
                        PairwiseLinkage linkageResult;
                        for (int i = 0; i < Chromosome.getFilteredSize(); i++){
                            for (int j = 0; j < Chromosome.getFilteredSize(); j++){
                                //many "slots" in table aren't filled in because it is a 1/2 matrix
                                if (i < j){
                                    dist = (Chromosome.getFilteredMarker(j)).getPosition() - (Chromosome.getFilteredMarker(i)).getPosition();
                                    if (maxDistance > 0){
                                        if ((dist > maxDistance || dist < negMaxDistance)){
                                            continue;
                                        }
                                    }
                                    linkageResult = textData.computeDPrime(Chromosome.realIndex[i],Chromosome.realIndex[j]);
                                    if(linkageResult != null) {
                                        saveDprimeWriter.write((Chromosome.realIndex[i]+1) + "\t" + (Chromosome.realIndex[j]+1) + "\t" + linkageResult + "\n");
                                    }
                                }
                            }
                        }
                    }
                    saveDprimeWriter.close();
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
