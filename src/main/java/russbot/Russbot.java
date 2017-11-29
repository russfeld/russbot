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
 *
 * @author russfeld
 */
public class Russbot {

    public Russbot(String [] args){
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();

        options.addOption(Option.builder("?")
                .longOpt("help")
                .desc("Print this help message")
                .build());
        options.addOption(Option.builder("t")
                .longOpt("test")
                .desc("Test russbot plugins on command line")
                .build());

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

        //print help and exit
        if (line.hasOption("?")) {
            formatter.printHelp("./gradlew run -PrunArgs=\"['opt1','opt2', ...]\"", options);
            System.exit(0);
        }

        boolean test = false;

        Logger.getLogger(Session.class.getName()).log(Level.INFO, "To run with arguments: ./gradlew run -PrunArgs=\"['opt1','opt2', ...]\"");
        Logger.getLogger(Session.class.getName()).log(Level.INFO, "To see usage: ./gradlew run -PrunArgs=\"['-?']\"");

        if (line.hasOption("t")) {
            test = true;
        }

        loadPlugins();
        connect(test);
    }

    public void loadPlugins(){
        //http://stackoverflow.com/questions/12730463/how-do-i-call-a-constructor-from-a-class-loaded-at-runtime-java
        //http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
        File file = new File(System.getProperty("user.dir")+ File.separator + "russbot.jar");
        if(file.exists()){
            loadFromJar(file);
        }
        file  = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + "classes" + File.separator);
        if(file.exists()){
            loadFromFile(file);
        }
    }

    private void loadFromJar(File file){
        try {
            URL url = null;
            url = file.toURI().toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            JarInputStream jarFile = new JarInputStream(new FileInputStream(file));
            JarEntry jarEntry = jarFile.getNextJarEntry();
            while (jarEntry != null) {
                if (jarEntry.getName().startsWith("russbot/plugins") && jarEntry.getName().endsWith(".class")) {
                    try {
                        String className = jarEntry.getName().replace("/", ".").replace(".class", "");
                        if(!className.equals("russbot.plugins.Plugin")){
                            Class clas = cl.loadClass(className);
                            Constructor con = clas.getConstructor();
                            Plugin plugin = (Plugin) con.newInstance();
                            Session.getInstance().addPlugin(plugin);
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(6);
                    }
                }
                jarEntry = jarFile.getNextJarEntry();
            }
        } catch (IOException ex) {
            Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(7);
        }
    }

    private void loadFromFile(File file){
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(4);
        }
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);

        file  = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + "classes" + File.separator + "main" + File.separator + "russbot" + File.separator + "plugins" + File.separator);
        File[] listOfFiles = file.listFiles();
        for(int i = 0; i < listOfFiles.length; i++){
            try {
                if (listOfFiles[i].isFile() && !listOfFiles[i].getName().contains("$")) {
                    String className = listOfFiles[i].getName().replace(".class", "");
                    if(!className.equals("Plugin")){
                        Class clas = cl.loadClass("russbot.plugins." + className);
                        Constructor con = clas.getConstructor();
                        Plugin plugin = (Plugin) con.newInstance();
                        Session.getInstance().addPlugin(plugin);
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(5);
            }
        }
    }

    public void connect(boolean test){
        Session.getInstance().connect(test);
    }

    public static void main(String[] args){
        new Russbot(args);
    }

}
