package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.CheckData;
import edu.mit.wi.haploview.association.*;
import edu.mit.wi.haploview.tagger.TaggerController;
import edu.mit.wi.tagger.Tagger;

import java.io.*;
import java.util.*;
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
    private String customAssocTestsFileName;
    private boolean skipCheck = false;
    private Vector excludedMarkers = new Vector();
    private boolean quietMode = false;
    private int blockOutputType;
    private boolean outputCheck;
    private boolean outputDprime;
    private boolean outputPNG;
    private boolean outputCompressedPNG;
    private boolean doPermutationTest;
    private int permutationCount;
    private boolean doTagging;
    private double tagRSquaredCutOff = -1;
    private Vector forceIncludeTags;
    private String forceIncludeFileName;
    private Vector forceExcludeTags;
    private String forceExcludeFileName;


    public boolean isNogui() {
        return nogui;
    }

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

    public int getBlockOutputType() {
        return blockOutputType;
    }

    private double getDoubleArg(String[] args, int valueIndex, String argName, double min, double max) {
        double argument = 0;
        if(valueIndex>=args.length || ((args[valueIndex].charAt(0)) == '-')) {
            System.out.println( argName + " requires a value between " + min + " and " + max);
            System.exit(1);
        }
        try {
            argument = Double.parseDouble(args[valueIndex]);
            if(argument<min || argument>max) {
                System.out.println(argName + " requires a value between " + min + " and " + max);
                System.exit(1);
            }
        }catch(NumberFormatException nfe) {
            System.out.println(argName + " requires a value between " + min + " and " + max);
            System.exit(1);
        }
        return argument;
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

        int maxDistance = -1;
        //this means that user didn't specify any output type if it doesn't get changed below
        blockOutputType = -1;
        double hapThresh = -1;
        double minimumMAF=-1;
        double spacingThresh = -1;
        double minimumGenoPercent = -1;
        double hwCutoff = -1;
        double missingCutoff = -1;
        int maxMendel = -1;
        boolean assocTDT = false;
        boolean assocCC = false;
        permutationCount = 0;


        for(int i =0; i < args.length; i++) {
            if(args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h")) {
                System.out.println(HELP_OUTPUT);
                System.exit(0);
            }
            else if(args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("-nogui")) {
                nogui = true;
            }
            else if(args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-pedfile")) {
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
            else if (args[i].equalsIgnoreCase("-pcloadletter")){
                System.err.println("PC LOADLETTER?! What the fuck does that mean?!");
                System.exit(31337);
            }
            else if (args[i].equalsIgnoreCase("-skipcheck") || args[i].equalsIgnoreCase("--skipcheck")){
                skipCheck = true;
            }
            else if (args[i].equalsIgnoreCase("-excludeMarkers")){
                i++;
                if(i>=args.length || (args[i].charAt(0) == '-')){
                    System.out.println("-excludeMarkers requires a list of markers");
                    System.exit(1);
                }
                else {
                    StringTokenizer str = new StringTokenizer(args[i],",");
                    try {
                        if (!quietMode) System.out.print("Excluding markers: ");
                        while(str.hasMoreTokens()) {
                            String token = str.nextToken();
                            if(token.indexOf("..") != -1) {
                                int lastIndex = token.indexOf("..");
                                int rangeStart = Integer.parseInt(token.substring(0,lastIndex));
                                int rangeEnd = Integer.parseInt(token.substring(lastIndex+2,token.length()));
                                for(int j=rangeStart;j<=rangeEnd;j++) {
                                    if (!quietMode) System.out.print(j+" ");
                                    excludedMarkers.add(new Integer(j));
                                }
                            } else {
                                if (!quietMode) System.out.println(token+" ");
                                excludedMarkers.add(new Integer(token));
                            }
                        }
                        if (!quietMode) System.out.println();
                    } catch(NumberFormatException nfe) {
                        System.out.println("-excludeMarkers argument should be of the format: 1,3,5..8,12");
                        System.exit(1);
                    }
                }
            }
            else if(args[i].equalsIgnoreCase("-ha") || args[i].equalsIgnoreCase("-l") || args[i].equalsIgnoreCase("-haps")) {
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
            else if(args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("-info")) {
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
            } else if (args[i].equalsIgnoreCase("-a") || args[i].equalsIgnoreCase("-hapmap")){
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
            else if(args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-blocks")) {
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    blockFileName = args[i];
                    blockOutputType = BLOX_CUSTOM;
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
            else if (args[i].equalsIgnoreCase("-track")){
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                   trackFileName = args[i];
                }else{
                    System.out.println("-track requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("-output") || args[i].equalsIgnoreCase("-blockoutput")) {
                i++;
                if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    if(blockOutputType != -1){
                        System.out.println("only one output argument is allowed");
                        System.exit(1);
                    }
                    if(args[i].equalsIgnoreCase("SFS") || args[i].equalsIgnoreCase("GAB")){
                        blockOutputType = BLOX_GABRIEL;
                    }
                    else if(args[i].equalsIgnoreCase("GAM")){
                        blockOutputType = BLOX_4GAM;
                    }
                    else if(args[i].equalsIgnoreCase("MJD") || args[i].equalsIgnoreCase("SPI")){
                        blockOutputType = BLOX_SPINE;
                    }
                    else if(args[i].equalsIgnoreCase("ALL")) {
                        blockOutputType = BLOX_ALL;
                    }
                }
                else {
                    //defaults to SFS output
                    blockOutputType = BLOX_GABRIEL;
                    i--;
                }
            }
            else if(args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dprime") || args[i].equalsIgnoreCase("-dprime")) {
                outputDprime = true;
            }
            else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("-check")){
                outputCheck = true;
            }
            else if(args[i].equalsIgnoreCase("-m") || args[i].equalsIgnoreCase("-maxdistance")) {
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
                    try {
                        maxDistance = Integer.parseInt(args[i]);
                        if(maxDistance<0){
                            System.out.println(args[i-1] + " argument must be a positive integer");
                            System.exit(1);
                        }
                    } catch(NumberFormatException nfe) {
                        System.out.println(args[i-1] + " argument must be a positive integer");
                        System.exit(1);
                    }
                }
            }
            else if(args[i].equalsIgnoreCase("-b") || args[i].equalsIgnoreCase("-batch")) {
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
            else if(args[i].equalsIgnoreCase("-hapthresh")) {
                i++;
                hapThresh = getDoubleArg(args,i,"-hapthresh",0,1);
            }
            else if(args[i].equalsIgnoreCase("-spacing")) {
                i++;
                spacingThresh = getDoubleArg(args,i,"-spacing",0,1);
            }
            else if(args[i].equalsIgnoreCase("-minMAF")) {
                i++;
                minimumMAF = getDoubleArg(args,i,"-minMAF",0,0.5);
            }
            else if(args[i].equalsIgnoreCase("-minGeno") || args[i].equalsIgnoreCase("-minGenoPercent")) {
                i++;
                minimumGenoPercent = getDoubleArg(args,i,"-minGeno",0,1);
            }
            else if(args[i].equalsIgnoreCase("-hwcutoff")) {
               i++;
                hwCutoff = getDoubleArg(args,i,"-hwcutoff",0,1);
            }
            else if(args[i].equalsIgnoreCase("-maxMendel") ) {
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-maxMendel requires an integer argument");
                    System.exit(1);
                }
                else {
                    try {
                        maxMendel = Integer.parseInt(args[i]);
                        if(maxMendel<0){
                            System.out.println("-maxMendel argument must be a positive integer");
                            System.exit(1);
                        }
                    } catch(NumberFormatException nfe) {
                        System.out.println("-maxMendel argument must be a positive integer");
                        System.exit(1);
                    }
                }
            }
            else if(args[i].equalsIgnoreCase("-missingcutoff")) {
                i++;
                missingCutoff = getDoubleArg(args,i,"-missingCutoff",0,1);
            }
            else if(args[i].equalsIgnoreCase("-assoctdt")) {
                assocTDT = true;
            }
            else if(args[i].equalsIgnoreCase("-assoccc")) {
                assocCC = true;
            }
                     
            else if(args[i].equalsIgnoreCase("-ldcolorscheme")) {
                i++;
                if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    if(args[i].equalsIgnoreCase("default")){
                        Options.setLDColorScheme(STD_SCHEME);
                    }
                    else if(args[i].equalsIgnoreCase("RSQ")){
                        Options.setLDColorScheme(RSQ_SCHEME);
                    }
                    else if(args[i].equalsIgnoreCase("DPALT") ){
                        Options.setLDColorScheme(WMF_SCHEME);
                    }
                    else if(args[i].equalsIgnoreCase("GAB")) {
                        Options.setLDColorScheme(GAB_SCHEME);
                    }
                    else if(args[i].equalsIgnoreCase("GAM")) {
                        Options.setLDColorScheme(GAM_SCHEME);
                    }
                }
                else {
                    //defaults to STD color scheme
                    Options.setLDColorScheme(STD_SCHEME);
                    i--;
                }
            }
            else if(args[i].equalsIgnoreCase("-permtests")) {
                i++;
                int permCount=0;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    System.out.println("-permtests requires an integer argument");
                    System.exit(1);
                }
                else {
                    try {
                        permCount = Integer.parseInt(args[i]);
                        if(permCount<0){
                            System.out.println("-permtests argument must be a positive integer");
                            System.exit(1);
                        }
                    } catch(NumberFormatException nfe) {
                        System.out.println("-permtests argument must be a positive integer");
                        System.exit(1);
                    }
                }
                doPermutationTest = true;
                permutationCount = permCount;
            }
            else if(args[i].equalsIgnoreCase("-customassoc")) {
                i++;
                if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    customAssocTestsFileName = args[i];
                }else{
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equalsIgnoreCase("-doTagging")) {
                doTagging = true;
            }
            else if(args[i].equalsIgnoreCase("-tagrSqCutoff")) {
                i++;
                tagRSquaredCutOff = getDoubleArg(args,i,"-tagrSqCutoff",0,1);
            }
            else if(args[i].equalsIgnoreCase("-includeTags")) {
                i++; 
                if(i>=args.length || args[i].charAt(0) == '-') {
                    System.out.println(args[i-1] + " requires a list of marker names.");
                    System.exit(1);
                }
                StringTokenizer str = new StringTokenizer(args[i],",");
                forceIncludeTags = new Vector();
                while(str.hasMoreTokens()) {
                    forceIncludeTags.add(str.nextToken());
                }
            }
            else if (args[i].equalsIgnoreCase("-includeTagsFile")) {
                i++;
                if(!(i>=args.length) && !(args[i].charAt(0) == '-')) {
                    forceIncludeFileName =args[i];
                }else {
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equalsIgnoreCase("-excludeTags")) {
                i++;
                if(i>=args.length || args[i].charAt(0) == '-') {
                    System.out.println("-excludeTags requires a list of marker names.");
                    System.exit(1);
                }
                StringTokenizer str = new StringTokenizer(args[i],",");
                forceExcludeTags = new Vector();
                while(str.hasMoreTokens()) {
                    forceExcludeTags.add(str.nextToken());
                }
            }
            else if (args[i].equalsIgnoreCase("-excludeTagsFile")) {
                i++;
                if(!(i>=args.length) && !(args[i].charAt(0) == '-')) {
                    forceExcludeFileName =args[i];
                }else {
                    System.out.println(args[i-1] + " requires a filename");
                    System.exit(1);
                }
            }
            else if(args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("-quiet")) {
                quietMode = true;
            }
            else {
                System.out.println("invalid parameter specified: " + args[i]);
                System.exit(1);
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
        if(skipCheck && !quietMode) {
            System.out.println("Skipping genotype file check");
        }
        if(maxDistance == -1){
            maxDistance = 500;
        }else{
            if (!quietMode) System.out.println("Max LD comparison distance = " +maxDistance);
        }

        Options.setMaxDistance(maxDistance);

        if(hapThresh != -1) {
            Options.setHaplotypeDisplayThreshold((int)(hapThresh*100));
            if (!quietMode) System.out.println("Haplotype display threshold = " + hapThresh);
        }
        
        if(minimumMAF != -1) {
            CheckData.mafCut = minimumMAF;
            if (!quietMode) System.out.println("Minimum MAF = " + minimumMAF);
        }

        if(minimumGenoPercent != -1) {
            CheckData.failedGenoCut = (int)(minimumGenoPercent*100);
            if (!quietMode) System.out.println("Minimum SNP genotype % = " + minimumGenoPercent);
        }

        if(hwCutoff != -1) {
            CheckData.hwCut = hwCutoff;
            if (!quietMode) System.out.println("Hardy Weinberg equilibrium p-value cutoff = " + hwCutoff);
        }

        if(maxMendel != -1) {
            CheckData.numMendErrCut = maxMendel;
            if (!quietMode) System.out.println("Maximum number of Mendel errors = "+maxMendel);
        }

        if(spacingThresh != -1) {
            Options.setSpacingThreshold(spacingThresh);
            if (!quietMode) System.out.println("LD display spacing value = "+spacingThresh);
        }

        if(missingCutoff != -1) {
            Options.setMissingThreshold(missingCutoff);
            if (!quietMode) System.out.println("Maximum amount of missing data allowed per individual = "+missingCutoff);
        }

        if(assocTDT) {
            Options.setAssocTest(ASSOC_TRIO);
        }
        else if(assocCC) {
            Options.setAssocTest(ASSOC_CC);
        }

        if(doPermutationTest) {
            if(!assocCC && !assocTDT) {
                System.out.println("An association test type must be specified for permutation tests to be performed.");
                System.exit(1);
            }
        }

        if(customAssocTestsFileName != null) {
            if(!assocCC && !assocTDT) {
                System.out.println("An association test type must be specified when using a custom association test file.");
                System.exit(1);
            }
            if(infoFileName == null) {
                System.out.println("A marker info file must be specified when using a custom association test file.");
                System.exit(1);
            }
        }

        if(doTagging) {
            if(infoFileName == null) {
                System.out.println("A marker info file must be specified when using -doTagging");
                System.exit(1);
            }

            if(blockOutputType == -1) {
                System.out.println("a block output type must be specified when using tagger");
                System.exit(1);
            }

            if(forceExcludeTags == null) {
                forceExcludeTags = new Vector();
            } else if (forceExcludeFileName != null) {
                System.out.println("-excludeTags and -excludeTagsFile cannot both be used");
                System.exit(1);
            }

            if(forceExcludeFileName != null) {
                File excludeFile = new File(forceExcludeFileName);
                forceExcludeTags = new Vector();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(excludeFile));
                    String line;
                    while((line = br.readLine()) != null) {
                        if(line.length() > 0 && line.charAt(0) != '#'){
                            forceExcludeTags.add(line);
                        }
                    }
                }catch(IOException ioe) {
                    System.out.println("An error occured while reading the file specified by -excludeTagsFile.");
                    System.exit(1);
                }
            }

            if(forceIncludeTags == null ) {
                forceIncludeTags = new Vector();
            } else if (forceIncludeFileName != null) {
                System.out.println("-includeTags and -includeTagsFile cannot both be used");
                System.exit(1);
            }

            if(forceIncludeFileName != null) {
                File includeFile = new File(forceIncludeFileName);
                forceIncludeTags = new Vector();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(includeFile));
                    String line;
                    while((line = br.readLine()) != null) {
                        if(line.length() > 0 && line.charAt(0) != '#'){
                            forceIncludeTags.add(line);
                        }
                    }
                }catch(IOException ioe) {
                    System.out.println("An error occured while reading the file specified by -includeTagsFile.");
                    System.exit(1);
                }
            }

            //check that there isn't any overlap between include/exclude lists
            Vector tempInclude = (Vector) forceIncludeTags.clone();
            tempInclude.retainAll(forceExcludeTags);
            if(tempInclude.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tempInclude.size(); i++) {
                    String s = (String) tempInclude.elementAt(i);
                    sb.append(s).append(",");
                }
                System.out.println("The following markers appear in both the include and exclude lists: " + sb.toString());
            }

            if(tagRSquaredCutOff != -1) {
                Options.setTaggerRsqCutoff(tagRSquaredCutOff);
            }

        } else if(forceExcludeTags != null || forceIncludeTags != null || tagRSquaredCutOff != -1) {
            System.out.println("-tagrSqCutoff, -excludeTags, -excludeTagsFile, -includeTags and -includeTagsFile cannot be used without -doTagging");
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
        if(batchFileName == null) {
            return;
        }
        batchFile = new File(this.batchFileName);

        if(!batchFile.exists()) {
            System.out.println("batch file " + batchFileName + " does not exist");
            System.exit(1);
        }

        if (!quietMode) System.out.println("Processing batch input file: " + batchFile);

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
                        if( name.substring(name.length()-4,name.length()).equalsIgnoreCase(".ped") ) {
                            processFile(name,PED_FILE,infoMaybe);
                        }
                        else if(name.substring(name.length()-5,name.length()).equalsIgnoreCase(".haps")) {
                            processFile(name,HAPS_FILE,infoMaybe);
                        }
                        else if(name.substring(name.length()-4,name.length()).equalsIgnoreCase(".hmp")){
                            processFile(name,HMP_FILE,"");
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
        if (!quietMode) System.out.println("Writing output to "+f.getName());
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
            fileType = HAPS_FILE;
        }
        else if (pedFileName != null){
            fileName = pedFileName;
            fileType = PED_FILE;
        }else{
            fileName = hapmapFileName;
            fileType = HMP_FILE;
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
            AssociationTestSet customAssocSet;

            if(!quietMode && fileName != null){
                System.out.println("Using data file: " + fileName);
            }

            inputFile = new File(fileName);
            if(!inputFile.exists()){
                System.out.println("input file: " + fileName + " does not exist");
                System.exit(1);
            }

            textData = new HaploData();
            //Vector result = null;

            if(fileType == HAPS_FILE){
                //read in haps file
                textData.prepareHapsInput(inputFile);
            }
            else if (fileType == PED_FILE) {
                //read in ped file
                textData.linkageToChrom(inputFile, PED_FILE);

                if(textData.getPedFile().isBogusParents()) {
                    System.out.println("Error: One or more individuals in the file reference non-existent parents.\nThese references have been ignored.");
                }
            }else{
                //read in hapmapfile
                textData.linkageToChrom(inputFile,HMP_FILE);
            }


            File infoFile = null;
            if (infoFileName != null){
                infoFile = new File(infoFileName);
            }
            if (fileType != HAPS_FILE){
                textData.prepareMarkerInput(infoFile,textData.getPedFile().getHMInfo());
            }else{
                textData.prepareMarkerInput(infoFile,null);
            }

            HashSet whiteListedCustomMarkers = new HashSet();
            if (customAssocTestsFileName != null){
                customAssocSet = new AssociationTestSet(customAssocTestsFileName);
                whiteListedCustomMarkers = customAssocSet.getWhitelist();
            }else{
                customAssocSet = null;
            }

            Hashtable snpsByName = new Hashtable();
            for(int i=0;i<Chromosome.getUnfilteredSize();i++) {
                SNP snp = Chromosome.getUnfilteredMarker(i);
                snpsByName.put(snp.getName(), snp);
            }

            for(int i=0;i<forceIncludeTags.size();i++) {
                if(snpsByName.containsKey(forceIncludeTags.get(i))) {
                    whiteListedCustomMarkers.add(snpsByName.get(forceIncludeTags.get(i)));
                }
            }



            textData.setWhiteList(whiteListedCustomMarkers);

            boolean[] markerResults = new boolean[Chromosome.getUnfilteredSize()];
            Vector result = null;
            if (fileType != HAPS_FILE){
                result = textData.getPedFile().getResults();
                //once check has been run we can filter the markers
                for (int i = 0; i < result.size(); i++){
                    if (((((MarkerResult)result.get(i)).getRating() > 0 || skipCheck) &&
                            Chromosome.getUnfilteredMarker(i).getDupStatus() != 2)){
                        markerResults[i] = true;
                    }else{
                        markerResults[i] = false;
                    }
                }
            }else{
                //we haven't done the check (HAPS files)
                Arrays.fill(markerResults, true);
            }

            for (int i = 0; i < excludedMarkers.size(); i++){
                int cur = ((Integer)excludedMarkers.elementAt(i)).intValue();
                if (cur < 1 || cur > markerResults.length){
                    System.out.println("Excluded marker out of bounds has been ignored: " + cur +
                            "\nMarkers must be between 1 and N, where N is the total number of markers.");
                    System.exit(1);
                }else{
                    markerResults[cur-1] = false;
                }
            }

            for(int i=0;i<Chromosome.getUnfilteredSize();i++) {
                if(textData.isWhiteListed(Chromosome.getUnfilteredMarker(i))) {
                    markerResults[i] = true;
                }
            }

            Chromosome.doFilter(markerResults);

            if(!quietMode && infoFile != null){
                System.out.println("Using marker information file: " + infoFile.getName());
            }
            if(outputCheck && result != null){
                CheckDataPanel cp = new CheckDataPanel(textData);
                cp.printTable(validateOutputFile(fileName + ".CHECK"));
            }
            Vector cust = new Vector();
            AssociationTestSet blockTestSet = null;

            if(blockOutputType != -1){
                textData.generateDPrimeTable();
                Haplotype[][] haplos;
                Haplotype[][] filtHaplos;
                switch(blockOutputType){
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
                        if(!quietMode) {
                            System.out.println("Using custom blocks file " + blockFileName);
                        }
                        cust = textData.readBlocks(blocksFile);
                        break;
                    case BLOX_ALL:
                        //handled below, so we don't do anything here
                        OutputFile = null;
                        break;
                    default:
                        OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                        break;

                }

                //this handles output type ALL
                if(blockOutputType == BLOX_ALL) {
                    OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                    textData.guessBlocks(BLOX_GABRIEL);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null){
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                    }else if (!quietMode){
                        System.out.println("Skipping block output: no valid Gabriel blocks.");
                    }

                    OutputFile = validateOutputFile(fileName + ".4GAMblocks");
                    textData.guessBlocks(BLOX_4GAM);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null){
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);;
                    }else if (!quietMode){
                        System.out.println("Skipping block output: no valid 4 Gamete blocks.");
                    }

                    OutputFile = validateOutputFile(fileName + ".SPINEblocks");
                    textData.guessBlocks(BLOX_SPINE);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null){
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                    }else if (!quietMode){
                        System.out.println("Skipping block output: no valid LD Spine blocks.");
                    }

                }else{
                    //guesses blocks based on output type determined above.
                    textData.guessBlocks(blockOutputType, cust);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null){

                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                    }else if (!quietMode){
                        System.out.println("Skipping block output: no valid blocks.");
                    }
                }

                if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                    if (blockOutputType == BLOX_ALL){
                        System.out.println("Haplotype association results cannot be used with block output \"ALL\"");
                    }else{
                        if (haplos != null){
                            blockTestSet = new AssociationTestSet(haplos,null);
                            blockTestSet.saveHapsToText(validateOutputFile(fileName + ".HAPASSOC"));

                        }else if (!quietMode){
                            System.out.println("Skipping block association output: no valid blocks.");
                        }
                    }
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
                    if(!quietMode) {
                        System.out.println("Using analysis track file " + trackFileName);
                    }
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                BufferedImage i = dpd.export(0,Chromosome.getUnfilteredSize(),outputCompressedPNG);
                try{
                    Jimi.putImage("image/png", i, OutputFile.getName());
                }catch(JimiException je){
                    System.out.println(je.getMessage());
                }
            }

            AssociationTestSet markerTestSet =null;
            if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC){
                markerTestSet = new AssociationTestSet(textData.getPedFile(),null,Chromosome.getAllMarkers());
                markerTestSet.saveSNPsToText(validateOutputFile(fileName + ".ASSOC"));
            }

            if(customAssocSet != null) {
                if(!quietMode) {
                    System.out.println("Using custom association test file " + customAssocTestsFileName);
                }
                try {
                    customAssocSet.runFileTests(textData,markerTestSet.getMarkerAssociationResults());
                    customAssocSet.saveResultsToText(validateOutputFile(fileName + ".CUSTASSOC"));

                }catch(IOException ioe) {
                    System.out.println("An error occured writing the custom association results file.");
                    customAssocSet = null;
                }
            }

            if(doPermutationTest) {
                AssociationTestSet permTests = null;
                if( customAssocSet != null) {
                    permTests = customAssocSet;
                }else {
                    permTests = new AssociationTestSet();
                    permTests.cat(markerTestSet);
                    permTests.cat(blockTestSet);
                }
                final PermutationTestSet pts = new PermutationTestSet(permutationCount,textData.getSavedEMs(),textData.getPedFile(),permTests);
                Thread permThread = new Thread(new Runnable() {
                    public void run() {
                        pts.doPermutations();
                    }
                });

                permThread.start();

                if(!quietMode) {
                    System.out.println("Starting " + permutationCount + " permutation tests (each . printed represents 1% of tests completed)");
                }

                int dotsPrinted =0;
                while(pts.getPermutationCount() - pts.getPermutationsPerformed() > 0) {
                    while(( (double)pts.getPermutationsPerformed() / pts.getPermutationCount())*100 > dotsPrinted) {
                        System.out.print(".");
                        dotsPrinted++;
                    }
                    try{
                        Thread.currentThread().sleep(100);
                    }catch(InterruptedException ie) {}
                }
                System.out.println();

                try {
                    pts.writeResultsToFile(validateOutputFile(fileName  + ".PERMUT"));
                } catch(IOException ioe) {
                    System.out.println("An error occured while writing the permutation test results to file.");
                }
            }


            if(doTagging) {
                Vector snps = Chromosome.getAllMarkers();
                HashSet names = new HashSet();
                Hashtable idsByName = new Hashtable();
                for (int i = 0; i < snps.size(); i++) {
                    SNP snp = (SNP) snps.elementAt(i);
                    names.add(snp.getName());
                    idsByName.put(snp.getName(),new Integer(i));
                }

                Vector sitesToCapture = new Vector();
                for(int i=0;i<Chromosome.getSize();i++) {
                    sitesToCapture.add(Chromosome.getMarker(i));
                }

                for (int i = 0; i < forceIncludeTags.size(); i++) {
                    String s = (String) forceIncludeTags.elementAt(i);
                    if(!names.contains(s)) {
                        System.out.println("Marker " + s + " in the list of forced included tags does not appear in the marker info file.");
                        System.exit(1);
                    }
                }

                for (int i = 0; i < forceExcludeTags.size(); i++) {
                    String s = (String) forceExcludeTags.elementAt(i);
                    if(!names.contains(s)) {
                        System.out.println("Marker " + s + " in the list of forced excluded tags does not appear in the marker info file.");
                        System.exit(1);
                    }
                }

                if(!quietMode) {
                    System.out.println("Starting tagging.");
                }

                TaggerController tc = new TaggerController(textData,forceIncludeTags,forceExcludeTags,sitesToCapture,
                        Tagger.AGGRESSIVE_TRIPLE);
                tc.runTagger();

                while(!tc.isTaggingCompleted()) {
                    try {
                        Thread.sleep(100);
                    }catch(InterruptedException ie) {}
                }

                tc.saveResultsToFile(validateOutputFile(fileName + ".TAGS"));
            }



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

    public Haplotype[][] filterHaplos(Haplotype[][] haplos) {
        if (haplos == null){
            return null;
        }
        Haplotype[][] filteredHaplos = new Haplotype[haplos.length][];
        for (int i = 0; i < haplos.length; i++){
            Vector tempVector = new Vector();
            for (int j = 0; j < haplos[i].length; j++){
                if (haplos[i][j].getPercentage()*100 > Options.getHaplotypeDisplayThreshold()){
                    tempVector.add(haplos[i][j]);
                }
            }
            filteredHaplos[i] = new Haplotype[tempVector.size()];
            tempVector.copyInto(filteredHaplos[i]);
        }

        return filteredHaplos;

    }

    private Vector readMarkerListFile(File fileName) {


        Vector markerList = new Vector();


        return markerList;
    }

}
