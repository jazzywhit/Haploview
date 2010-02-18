package edu.mit.wi.haploview;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
//import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class UpdateChecker {
    private boolean newVersionAvailable;
    private boolean finalReleaseAvailable;
    private double newVersion;
    private int newBetaVersion = -1;

    public UpdateChecker() {

    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable;
    }

    public boolean isFinalVersionAvailable() {
        return finalReleaseAvailable;
    }

    public void setNewVersionAvailable(boolean newVersionAvailable) {
        this.newVersionAvailable = newVersionAvailable;
    }

    public double getNewVersion() {
        return newVersion;
    }

    public int getNewBetaVersion() {
        return newBetaVersion;
    }

    public void setNewVersion(double newVersion) {
        this.newVersion = newVersion;
    }

    public boolean checkForUpdate() throws IOException{

        try {
            URL url = new URL("http://www.broadinstitute.org/ftp/pub/mpg/haploview/newversion.txt");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("User-agent",Constants.USER_AGENT);
            con.connect();

            int response = con.getResponseCode();

            if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
                //if something went wrong
                throw new IOException("Could not connect to update server.");
            }else {
                //all is well
                BufferedReader betaReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String versionLine = betaReader.readLine();
                String betaLine = betaReader.readLine();
                double newestVersion;
                int newestBetaVersion;
                try{
                    newestVersion = Double.parseDouble(versionLine);
                    newestBetaVersion = Integer.parseInt(betaLine);
                }catch(NumberFormatException nfe){
                    return false;
                }


                if(Constants.VERSION < newestVersion) { //you're using an older final version
                    this.newVersion = newestVersion;
                    this.newVersionAvailable = true;
                }else if (Constants.VERSION == newestVersion){
                    if(Constants.BETA_VERSION == 0){ //you're using the current final version
                        this.newVersion = Constants.VERSION;
                        this.newVersionAvailable = false;
                    }else{ //you're using a beta of the current final version
                        this.newVersion = Constants.VERSION;
                        this.newVersionAvailable = true;
                        this.finalReleaseAvailable = true;
                    }
                }else if (Constants.VERSION > newestVersion) {
                    if(Constants.BETA_VERSION == 0){ //you're using a newer final version
                        this.newVersionAvailable = false;
                        this.newVersion = Constants.VERSION;
                    }else{
                        if (newestBetaVersion == -1 || Constants.BETA_VERSION >= newestBetaVersion){ //you're using a beta of a newer final version
                            this.newVersionAvailable = false;
                            this.newVersion = Constants.VERSION;
                        }else if (Constants.BETA_VERSION < newestBetaVersion){ //you're using an older beta of a newer final version
                            this.newVersion = Constants.VERSION;
                            this.newVersionAvailable = true;
                            this.newBetaVersion = newestBetaVersion;
                        }
                    }
                }
            }
            con.disconnect();

        } catch(MalformedURLException mue) {
            //System.err.println("the following url exception occured:" + mue);
        }


        return this.newVersionAvailable;
    }


}
