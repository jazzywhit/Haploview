package edu.mit.wi.haploview;

import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;


public class HVWrap {

    HVWrap() {}

    public static void main(String[] args) {

        JFrame jf = new JFrame();
        String dir = System.getProperty("user.dir");
        String sep = System.getProperty("file.separator");
        String ver = System.getProperty("java.version");
        System.out.println(ver);
        String jarfile = System.getProperty("java.class.path");

        String argsToBePassed = new String();
        boolean headless = false;
        for (int a = 0; a < args.length; a++){
            argsToBePassed = argsToBePassed.concat(" " + args[a]);
            if (args[a].equals("-n")){
                headless=true;
            }
        }

        try {

            //if the nogui flag is present we force it into headless mode
            String runString = "java -Xmx650m -classpath " + jarfile;
            if (headless){
                runString += " -Djava.awt.headless=true";
            }
            runString += " edu.mit.wi.haploview.HaploView"+argsToBePassed;
            Process child = Runtime.getRuntime().exec(runString);

            int c;
            boolean dead = false;
            StringBuffer errorMsg = new StringBuffer();
            InputStream es = child.getErrorStream();
            InputStream is = child.getInputStream();

            //while the child is alive we wait for error messages
            while ((c=es.read())!=-1) {
                errorMsg.append((char)c);
                if (!dead){
                    child.destroy();
                    dead = true;
                }
            }

            //if the child has exited without throwing an error (which should've been caught and dealt
            //with above) we read all the accumulated msgs to stdout and print them...
            while ((c=is.read())!=-1){
                System.out.print((char)c);
            }
            if (dead){
                JOptionPane.showMessageDialog(jf, "Fatal Error:\n" + errorMsg, null, JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(jf, "Error:\nUnable to launch Haploview.", null, JOptionPane.ERROR_MESSAGE);
        }
        System.exit(0);
    }


}
