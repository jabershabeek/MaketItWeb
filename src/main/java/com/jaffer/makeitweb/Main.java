package com.jaffer.makeitweb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.jaffer.makeitweb.server.JmsService;
import com.jaffer.makeitweb.server.SwingServer;


public class Main {
    
    public static void main(String[] args) throws Exception {	
    	initializeApp();
        JmsService.startService();
        SwingServer.startServer();

    }
    
    public static void initializeApp()
    {
    	 CommandLineParser parser = new PosixParser();

         // create the Options
         Options options = new Options();
         options.addOption("h", "host", true, "Local interface address where the web server will listen.(localhost)");
         options.addOption("p", "port", true, "Port where the web server will listen.(8080)");
         options.addOption("c", "clients", true, "Maximum number of simultaneous swing connections.(10)");
         options.addOption("m", "mainClass", true, "Swing application main class.");
         options.addOption("a", "args", true, "Commandline arguments for swing application.");
         options.addOption("v", "vmargs", true, "JVM arguments for swing application.");

         try {
             // parse the command line arguments
             CommandLine line = parser.parse(options, readConfigurations());
             // validate that main class has been set
        /*     if (!line.hasOption("m")) {
                 System.out.println("Please specify swing application's main class");
                 HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("makeitweb", options);
                 System.exit(1);
             }*/
             ConfigurationImpl cimpl = (ConfigurationImpl) Configuration.getInstance();
             if (line.getOptionValue('h') != null) {
                 cimpl.setHost(line.getOptionValue('h'));
             }
             if (line.getOptionValue('p') != null) {
                 cimpl.setPort(line.getOptionValue('p'));
             }
             if (line.getOptionValue('m') != null) {
             	String mains[]=line.getOptionValue('m').split(",");
             	for(int i=0;i<mains.length;i++)
                 cimpl.setMain(i,mains[i]);
             }
             if (line.getOptionValue('a') != null) {
                 cimpl.setArgs(line.getOptionValue('a'));
             }
             if (line.getOptionValue('v') != null) {
                 cimpl.setVmargs(line.getOptionValue('v'));
             }
             if (line.getOptionValue('c') != null) {
                 cimpl.setClients(Integer.valueOf(line.getOptionValue('c')));
             }
         } catch (ParseException exp) {
             System.out.println(exp.getMessage());
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("makeitweb", options);
         }

    }
    
    public static  String[] readConfigurations()
    {
   /* 	
    	Properties prop = new Properties();
    	 
    	try {
    		//set the properties value
    		prop.setProperty("host", "localhost");
    		prop.setProperty("port", "mkyong");
    		prop.setProperty("swingmainclass", "password");
 
    		//save properties to project root folder
    		prop.store(new FileOutputStream("config.properties"), null);
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }*/
    	String[] args=new String[6];
    	Properties prop = new Properties();
 
    	try {
               //load a properties file
    		prop.load(new FileInputStream("config.properties"));
 
               //get the property value and print it out
    		args[0]="-h";
    		args[1]=prop.getProperty("host");
    		args[2]="-p";
    		args[3]=prop.getProperty("port");
    		args[4]="-m";
    		args[5]=prop.getProperty("swingmainclass","sample");
    		//System.out.println();
    		 System.out.println("args"+args[5]);
           return args;
    	} catch (IOException ex) {
    		  System.out.println("Please specify swing configurations");
        
              System.exit(0);
        }
		return args;
 
    }
    

}
