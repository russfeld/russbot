/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;
import russbot.plugins.Plugin;

/**
 *  Main Class for russbot
 *
 *  Handles loading command line options, loading plugins, and starting the bot
 *  @author russfeld
 */
public class Russbot {

    /**
     * Constructor for Russbot
     *
     * @param args - command line arguments to be parsed
     */
    public Russbot(String [] args){

        // Uses Apache Commons CLI to handle command-line arguments
        // https://commons.apache.org/proper/commons-cli/
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();

        //add options to available list of options
        options.addOption(Option.builder("?")
                .longOpt("help")
                .desc("Print this help message")
                .build());
        options.addOption(Option.builder("t")
                .longOpt("test")
                .desc("Test russbot plugins on command line")
                .build());

        //create parser and parse command line
        CommandLineParser clparser = new DefaultParser();
        CommandLine line = null;
        try {
            line = clparser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("./gradlew run -PrunArgs=\"['opt1','opt2', ...]\"", options);
            //e.printStackTrace();
            System.exit(1);
        }

        //If help is requested, print help message and exit
        if (line.hasOption("?")) {
            formatter.printHelp("./gradlew run -PrunArgs=\"['opt1','opt2', ...]\"", options);
            System.exit(0);
        }

        //Print helpful debugging information when run from command line
        Logger.getLogger(Session.class.getName()).log(Level.INFO, "To run with arguments: ./gradlew run -PrunArgs=\"['opt1','opt2', ...]\"");
        Logger.getLogger(Session.class.getName()).log(Level.INFO, "To see usage: ./gradlew run -PrunArgs=\"['-?']\"");

        //Read option for test mode
        boolean test = false;
        if (line.hasOption("t")) {
            test = true;
        }

        //Load plugins from file
        loadPlugins();

        //Connect to Slack
        connect(test);
    }

    /**
     * Use Java Reflection to load plugins at runtime from the plugins folder or an associated JAR file
     *
     * See for reference:
     * http://stackoverflow.com/questions/12730463/how-do-i-call-a-constructor-from-a-class-loaded-at-runtime-java
     * http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
     */
    public void loadPlugins(){
        //Check if the program is running from a JAR file
        File file = new File(System.getProperty("user.dir")+ File.separator + "russbot.jar");
        if(file.exists()){
            loadFromJar(file);
        }

        //Check if the program is running from compiled class files
        file  = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + "classes" + File.separator);
        if(file.exists()){
            loadFromFile(file);
        }
    }

    /**
     * Load plugins from a JAR file package of Russbot
     * This is generally not used in the current setup
     *
     * @param file = the JAR file to load plugins from
     */
    private void loadFromJar(File file){
        try {
            //Access the JAR file
            URL url = null;
            url = file.toURI().toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            JarInputStream jarFile = new JarInputStream(new FileInputStream(file));

            //Loop over entries in the JAR file
            JarEntry jarEntry = jarFile.getNextJarEntry();
            while (jarEntry != null) {

                //If we find a class in the Plugins folder, try to load it
                if (jarEntry.getName().startsWith("russbot/plugins") && jarEntry.getName().endsWith(".class")) {
                    try {
                        String className = jarEntry.getName().replace("/", ".").replace(".class", "");

                        //EXCEPT don't load the base Plugin class - it doesn't work
                        if(!className.equals("russbot.plugins.Plugin")){
                            Class clas = cl.loadClass(className);
                            Constructor con = clas.getConstructor();
                            Plugin plugin = (Plugin) con.newInstance();
                            Session.getInstance().addPlugin(plugin);
                        }

                    //Catch errors and crash
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(6);
                    }
                }

                //Load next JAR file entry
                jarEntry = jarFile.getNextJarEntry();
            }

        //Catch errors and crash
        } catch (IOException ex) {
            Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(7);
        }
    }
    /**
     * Load plugins from a directory of class files
     *
     * @param file = the directory containing the files
     */
    private void loadFromFile(File file){

        //Access the directory and get the URLS of the files
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(4);
        }
        URL[] urls = new URL[]{url};

        //Create a Java class loader for those URLs
        ClassLoader cl = new URLClassLoader(urls);

        //Get a directory listing of the files in the plugins folder
        file  = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + "classes" + File.separator + "main" + File.separator + "russbot" + File.separator + "plugins" + File.separator);
        File[] listOfFiles = file.listFiles();

        //iterate over all files in the plugins folder
        for(int i = 0; i < listOfFiles.length; i++){
            try {

                //ignore partial classes and internal classes
                if (listOfFiles[i].isFile() && !listOfFiles[i].getName().contains("$")) {
                    String className = listOfFiles[i].getName().replace(".class", "");

                    //don't load the base Plugin file - it doesn't work
                    if(!className.equals("Plugin")){
                        Class clas = cl.loadClass("russbot.plugins." + className);
                        Constructor con = clas.getConstructor();
                        Plugin plugin = (Plugin) con.newInstance();
                        Session.getInstance().addPlugin(plugin);
                    }
                }

            //Catch errors and crash
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(5);
            }
        }
    }

    /**
     * Connects to Slack by loading a Session instance
     *
     * @param test - boolean to load test mode from command-line args
     */
    public void connect(boolean test){
        Session.getInstance().connect(test);
    }

    /**
     * Main function for Russbot - simply creates a new Russbot instance
     *
     * @param args - the command-line arguments
     */
    public static void main(String[] args){
        new Russbot(args);
    }

}
