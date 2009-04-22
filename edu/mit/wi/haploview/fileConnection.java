package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFileException;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: executedisaster
 * Date: Apr 22, 2009
 * Time: 11:45:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class fileConnection {

    private BufferedReader fileReader;

    public fileConnection() {

    }

    public fileConnection(String fileLocation) throws IOException{

        InputStream inputStream;

        try {
            try {
                URL fileURL = new URL(fileLocation);
                HttpURLConnection fileConnection = (HttpURLConnection) fileURL.openConnection();
                fileConnection.setRequestProperty("User-agent", Constants.USER_AGENT);
                if(Options.getGzip()){
                    fileConnection.setRequestProperty("Accept-Encoding", "gzip");
                }
                fileConnection.connect();
                int response = fileConnection.getResponseCode();
                if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
                    throw new IOException("Could not connect to HapMap database.");
                }

                inputStream = fileConnection.getInputStream();

            } catch (MalformedURLException mfe) {
                File sampleFile = new File(fileLocation);
                if (sampleFile.length() < 1) {
                    System.out.println("Sample File Sucks");
                }
                inputStream = new FileInputStream(sampleFile);
            }
            if (Options.getGzip()) {
                GZIPInputStream sampleInputStream = new GZIPInputStream(inputStream);
                fileReader = new BufferedReader(new InputStreamReader(sampleInputStream));
            } else {
                fileReader = new BufferedReader(new InputStreamReader(inputStream));
            }
            
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    public BufferedReader getBufferedReader(){
        return fileReader;
    }
}
