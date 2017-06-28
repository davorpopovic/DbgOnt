// Copyright (C) 2017 Alset Consulting Ltd.
// All rights reserved.

package mainpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
   
public class DbgOnt {
	
	// input parameters - defaults
	private static String outputFile        = "DbgOnt_out.txt";
	private static String outputPath        = ".";
	private static String baseUrl           = "http://www.infogo.gov.on.ca/infogo/home.html";
	private static String dirDelimiter      = "/";
	private static String treeTimeout       = "50";
	private static String recordTimeout     = "50";
	private static String connectionTimeout = "3000";
	private static String browser           = "CHROME";
	private static String orgProfile        = "0";
	private static String nextRecord        = "0";
	private static String continueProcessing = "NO";
	
	private static final String version     = "Build date: March-14-2017 Version: 1.0";
	
	public DbgOnt() {
	
	}
	
	public static void main(String[] args) {
		
		Calendar now  = Calendar.getInstance();
		
		System.out.println("\n[" + now.getTime() + "] DbgOnt starting....");
		System.out.println(version);
			
		if(args.length == 1) //argument (XML file name) from command line...
		{			 			
			 String html = "";
			 String line = "";
			
			 try 
			 {
			     FileReader fileReader = new FileReader(args[0]);
			     BufferedReader bufferedReader = new BufferedReader(fileReader);
			     while((line = bufferedReader.readLine()) != null) {
			         html = html + line.trim();
			     }
			
			     //System.out.println(html);
			     bufferedReader.close();
			 }
			 catch(IOException ex)
			 {
			     ex.printStackTrace();
			 }
						
			 Document doc = Jsoup.parse(html, "", Parser.xmlParser());
			
			 //System.out.println(doc.getAllElements().get(0)); //whole document
			 //System.out.println(doc.getAllElements().get(1)); //root tag
			 
			 // extract input parameter values
			 int numElements = doc.getAllElements().size();
			 			 
			 for( int j = 0; j < numElements; j++)
			 {
				 Element e = doc.getAllElements().get(j);
				 String attributes = e.attributes().toString();
				 //System.out.println("j=" + j + " attributes=" + attributes);
				 int k = attributes.indexOf("=");
				 if( k > -1 ) 
				 {
					 String key = attributes.substring(0,k).trim();
					 String value = e.attributes().get(key).trim();
					 //System.out.println("j=" + j + " key=" + key + " val=" + value );
					 
					 if(key.equals("TreeTimeout")) treeTimeout = value;
					 else if(key.equals("RecordTimeout")) recordTimeout = value;
					 else if(key.equals("ConnectionTimeout")) connectionTimeout = value;
					 else if(key.equals("OutputFile")) outputFile = value;
					 else if(key.equals("OutputPath")) outputPath = value;
					 else if(key.equals("BaseUrl")) baseUrl = value;
					 else if(key.equals("DirDelimiter")) dirDelimiter = value;
					 else if(key.equals("Browser")) browser = value;
					 else if(key.equals("OrgProfile")) orgProfile = value;  // if specified, do only this organisation ( the whole tree )
					 else if(key.equals("NextRecord")) nextRecord = value;  // if specified, APPEND to output data file recordCount starting with nextRecord
					 else if(key.equals("ContinueProcessing")) continueProcessing = value;  // if specified and set to "YES" start with orgProfile and continue to the end
				 }
				 //System.out.println("---------------------------------");
			 }
			 
			 System.out.println("\nInput parameters:");
		     System.out.println("Output data file   = " + outputFile);
		     System.out.println("Output path        = " + outputPath);
		     System.out.println("Data source URL    = " + baseUrl);
		     System.out.println("Dir. Delimiter     = " + dirDelimiter);
		     System.out.println("Query parameters:");
		     System.out.println("Tree Timeout       = " + treeTimeout);
		     System.out.println("Record Timeout     = " + recordTimeout);
		     System.out.println("Connection Timeout = " + connectionTimeout);
		     System.out.println("Browser            = " + browser);
		     System.out.println("OrgProfile         = " + orgProfile);
		     System.out.println(" ");
		    
		     // make all directory delimiters in paths the same as in dir delimiter...
		     if( dirDelimiter.charAt(0) == '/' )
		     {
		         outputPath = outputPath.replace('\\',dirDelimiter.charAt(0));
		     }
		     else
		     {
		         outputPath = outputPath.replace('/',dirDelimiter.charAt(0));
		     }
			 
			 //start DB processor...
			 String path = outputPath + dirDelimiter + outputFile;
			 String listPath = outputPath + dirDelimiter + "DbgOntList.txt";
			 System.out.println("Writing to output data file: " + path);
			 System.out.println(" ");
		
			 DbgOntProcessor dbProcessor = new DbgOntProcessor  (path,
					                                             listPath,
			                                                     baseUrl,
			                                                     treeTimeout,
			                                                     recordTimeout,
			                                                     connectionTimeout,
			                                                     browser,
			                                                     orgProfile);
			 dbProcessor.start();
			
		}
		else
		{
			System.out.println("Fatal Error: No parameter file specified, exiting...");
	        System.exit(1);
		}

	}

}
