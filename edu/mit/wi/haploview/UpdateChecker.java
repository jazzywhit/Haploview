package edu.mit.wi.haploview;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 24, 2004
 * Time: 3:33:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateChecker {
    private boolean newVersionAvailable;
    private double newVersion;

    public UpdateChecker() {

    }

    public boolean isNewVersionAvailable() {
        return newVersionAvailable;
    }

    public void setNewVersionAvailable(boolean newVersionAvailable) {
        this.newVersionAvailable = newVersionAvailable;
    }

    public double getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(double newVersion) {
        this.newVersion = newVersion;
    }

    public boolean checkForUpdate() throws IOException{

        try {
            URL url = new URL("http://www.broad.mit.edu/mpg/haploview/uc/version.txt");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("User-agent",Constants.TITLE_STRING);
            con.connect();

            int response = con.getResponseCode();

            if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
                //if something went wrong
                throw new IOException("Could not connect to update server.");
            }
            else {
                //all is well
                InputStream inputStream = con.getInputStream();
                byte[] buf = new byte[200];
                int size = con.getContentLength();
                int read;
                if(size > 200) {
                    read = inputStream.read(buf,0,200);
                }
                else {
                    read = inputStream.read(buf,0,size);
                }

                String data = "";
                if(read != 0)  {
                    data = new String(buf);
                    //System.out.println("\nfile contents:");
                    //System.out.println(data);
                    double newestVersion = Double.parseDouble(data);

                    if(newestVersion > Constants.VERSION) {
                        this.newVersion = newestVersion;
                        this.newVersionAvailable = true;
                    }
                    else {
                        this.newVersionAvailable = false;
                        this.newVersion = Constants.VERSION;
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
