// Copyright (C) 2017 Alset Consulting Ltd.
// All rights reserved.

package mainpackage;

import java.io.FileWriter;
import java.io.IOException;       
import java.io.PrintWriter;
import java.net.MalformedURLException;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

public class DbgOntProcessor extends Thread {
	
	public class SubOrganisation {
		
		private  String url;
		private  String name;
				
		public SubOrganisation(String url, String name) {
			this.url = url;
			this.name = name;
		
		}

		// return the url
		public String getUrl() {
			return url;
		}

		// return the name
		public String getName() {
			return name;
		}
	}
	
	private PrintWriter    osd = null;
	private PrintWriter    osl = null;
	
    private String firstName   = "";
    private String lastName    = "";
    private String telephone   = "";
    private String email       = "";
    private String fax         = "";
    private String address1    = "";
    private String address2    = "";
    private String city        = "";
    private String province    = "";
    private String postalCode  = "";
    private String jobTitle    = "";

    private String baseUrl     = "";

    private int    treeTimeout = 0;
    private int    recordTimeout= 0;
    private int    connectionTimeout = 0;
    private String browser = "";
    
    private long   pageCount = 0;
    private long   recordCount = 0;
    private String orgProfile = "0";

    private static final char  TMPCHAR = 0x1c;

    private int recursionLevel = 0;
    

	public DbgOntProcessor( String outputFile,
			                String outputListFile,
				            String baseUrl,
				            String treeTimeout,
				            String recordTimeout,
				            String connectionTimeout,
				            String browser,
				            String orgProfile) 
	{
	   this.baseUrl           = baseUrl;
	   this.treeTimeout       = (new Integer(treeTimeout)).intValue();
	   this.recordTimeout     = (new Integer(recordTimeout)).intValue();
	   this.connectionTimeout = (new Integer(connectionTimeout)).intValue();
	   this.browser           = browser;
	   this.orgProfile        = orgProfile;
	  		 		
	   try
	   {
  	      osd = new PrintWriter( new FileWriter(outputFile));
	
	      osd.println("\"" + "recordCount"           + "\"," +
					  "\"" + "timeStamp"             + "\"," +
					  "\"" + "firstName"             + "\"," +
					  "\"" + "lastName"              + "\"," +
					  "\"" + "jobTitle"              + "\"," +
					  "\"" + "departmentTree"        + "\"," +
					  "\"" + "level1Dept"            + "\"," +
					  "\"" + "level2Dept"            + "\"," +
					  "\"" + "level3Dept"            + "\"," +
					  "\"" + "level4Dept"            + "\"," +
					  "\"" + "level5Dept"            + "\"," +
					  "\"" + "level6Dept"            + "\"," +
					  "\"" + "level7Dept"            + "\"," +
					  "\"" + "level8Dept"            + "\"," +
					  "\"" + "level9Dept"            + "\"," +
					  "\"" + "level10Dept"            + "\"," +
					  "\"" + "level11Dept"            + "\"," +
					  "\"" + "lowestDep"             + "\"," +
					  "\"" + "telephone"             + "\"," +
					  "\"" + "email"                 + "\"," +
					  "\"" + "fax"                   + "\"," +
					  "\"" + "address1"              + "\"," +
					  "\"" + "address2"              + "\"," +
					  "\"" + "city"                  + "\"," +
					  "\"" + "province"              + "\"," +
					  "\"" + "postalCode"           + "\"" );
	
	      osd.flush(); 
	      
	      osl = new PrintWriter( new FileWriter(outputListFile));
	   }
	   catch ( IOException e)
	   {
	       System.out.print("Error while createing export file: " + e );
	   } // end try
	
	   pageCount = 0;
	   recordCount = 0;
	   	   
	}
	
	public void run()
	{
		String sumLevel = "";
		
		BrowserVersion bv = null;
		
		if( browser.equals("CHROME"))
		{
		   bv = BrowserVersion.CHROME;
		}
		else if( browser.equals("EXPLORER"))
		{
		   bv = BrowserVersion.INTERNET_EXPLORER;
		}
		else if( browser.equals("FIREFOX"))
		{
		   bv = BrowserVersion.FIREFOX_45;
		}
		else if( browser.equals("EDGE"))
		{
		   bv = BrowserVersion.EDGE;
		}
		else
		{
			bv = BrowserVersion.CHROME; // default
		}
		
		System.out.println("==========================================================================================");
		System.out.println(bv.getUserAgent());
		System.out.println("==========================================================================================");
		
		// output list of top level organisations
		listOrganizations(bv);
		
		// process top level departments first
		
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        WebClient webClient = new WebClient(bv);  
                
        String url = baseUrl;

        System.out.println("run(): Loading top level departments page: " + url);

        HtmlPage page = null;
        
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        webClient.waitForBackgroundJavaScript(30 * 1000);
        String pageAsXml = page.asXml();
        //System.out.println(pageAsXml);  // page in XML format

        // JSoup imported objects 
        Document doc = Jsoup.parse(pageAsXml);
     
        Elements listings =  doc.getElementById("browseOrgs").select("ul.reduce-list-margin");

        System.out.println("Number of top level departments: " + listings.size()+ "\n"); //should be 9 as of March 5, 2017
        
        webClient.close();

        for (Element listing : listings){
        	String orgName = listing.select("ul > a").text();
            System.out.println("MainLevel Organization: " + orgName );
                     
            System.out.println("Sub Orginization(s):");
            
            recursionLevel = 1;
            String tabString = "";
            for( int k = 0; k < recursionLevel; k++)
            {
            	tabString += "\t";
            }
            	
            String branchCode = "";
            
            Elements branches = listing.select("a.small");
            for (Element branch : branches){
            	
            	// wait a bit...
            	try
				{
					Thread.sleep(treeTimeout);
				}
				catch (InterruptedException ex)
				{
					// something went wrong...
					ex.printStackTrace();
				}
            	
               	String branchName = branch.text();
            	branchCode = branch.attr("data-org-id");
            	
            	if( orgProfile.equals("0") || (!orgProfile.equals("0") && orgProfile.equals(branchCode)) )
            	{
            		// do processing if orgProfile is NOT specified or only for that department if specified
            		
            		webClient = new WebClient(bv); // create one web client per top level organisation and pass it recursively until the whole tree is exhausted...
            		
            	    System.out.println( tabString + branchName + "(code: " + branchCode + ")");    
	                
	                // process next level of organisations
	                String URL = "http://www.infogo.gov.on.ca/infogo/home.html#orgProfile/" + branchCode + "/en";
	                                                
	                sumLevel = branchName; // the very first organisation level should not be used
	                
	                processNextLevel(webClient, URL, sumLevel, "", "");   // add condition for testing
	                
	                System.out.println(tabString + "[" + Calendar.getInstance().getTime() + "] End of processing department, pages processed=" + pageCount + ", records processed=" + recordCount );
	             
	                webClient.close();
            	}
            }
            System.out.println();
           
        }
        
        Calendar now  = Calendar.getInstance();
 	     	   
        System.out.println("\n\n[" + now.getTime() + "] Program exiting, pages processed=" + pageCount + ", records processed=" + recordCount );
              
	}
	
	public void listOrganizations(BrowserVersion bv)
	{
		// output just list of top level organizations and correspondng codes
		// which could later be used to access separate departments
			
		// process top level departments first
		
		System.out.println("listOrganizations(): crating a list of  top level departments, the file is DbgOntList.txt, located at the same folder as output data.");
		
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
        WebClient webClient = new WebClient(bv);  
                
        String url = baseUrl;

        osl.println("listOrganizations(): Loading top level departments page: " + url);

        HtmlPage page = null;
        
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        webClient.waitForBackgroundJavaScript(30 * 1000);
        String pageAsXml = page.asXml();
     
        // JSoup imported objects 
        Document doc = Jsoup.parse(pageAsXml);
       
        Elements listings =  doc.getElementById("browseOrgs").select("ul.reduce-list-margin");

        osl.println("Number of top level departments: " + listings.size()+ "\n"); //should be 9 as of March 5, 2017

        for (Element listing : listings){
        	String orgName = listing.select("ul > a").text();
            osl.println("MainLevel Organization: " + orgName );
                       
            osl.println("Sub Orginization(s):");
            
            recursionLevel = 1;
            String tabString = "";
            for( int k = 0; k < recursionLevel; k++)
            {
            	tabString += "\t";
            }
            	
            String branchCode = "";
            
            Elements branches = listing.select("a.small");
            for (Element branch : branches){
               	String branchName = branch.text();
            	branchCode = branch.attr("data-org-id");
                osl.println( tabString + branchName + "(code: " + branchCode + ")");    
                
            }
            osl.println();
            
        }
                       
        webClient.close();
        
        osl.flush(); 
        osl.close();
	}
	
	private void processNextLevel(WebClient webClient, String URL, String sumLevel, String parentMailingAddress, String parentFax)
	{
		recursionLevel += 1;
		
		pageCount += 1;
		
		String tabString = "";
        for( int k = 0; k < recursionLevel; k++)
        {
        	tabString += "\t";
        }
        
		System.out.println(tabString + "processNextLevel(): recursionLevel=" + recursionLevel + " URL=" + URL );
		System.out.println(tabString + "processNextLevel(): sumLevel=" + sumLevel);
		
		HtmlPage page = null;
		
		try {
			page = webClient.getPage(URL);
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        webClient.waitForBackgroundJavaScript(30 * 1000);
        String pageAsXml = page.asXml();
        //System.out.println(pageAsXml);

        Document doc = Jsoup.parse(pageAsXml);
        //System.out.println(doc.outerHtml());
        
        //Get sub organisations and employees (if any) 
        
        try {
        	if( doc != null)
        	{
        		// mailing address
        		String mailingAddress = getMailAddr(doc);
        		System.out.println(tabString + "Mailing Address=" + mailingAddress);
        		
        		if(mailingAddress.length() > 0)
        		{
        			// do nothing
        		}
        		else
        		{
        			// use from parent level
        			mailingAddress = parentMailingAddress;
        		}
        		
        		// fax
        		String fax = getFax(doc);
        		System.out.println(tabString + "Fax=" + fax);
        		
        		if(fax.length() > 0)
        		{
        			// do nothing
        		}
        		else
        		{
        			// use from parent level
        			fax = parentFax;
        		}
        		
        		// list of sub organisations
				if(hasReportOrg(doc))
				{
					System.out.println(tabString + "================== Sub Organisations: ====================");
				    //System.out.println(getReportOrgs(doc));
					for (SubOrganisation item : getReportOrgs(doc)){
						System.out.println(tabString + "Sub Org.URL =" + item.getUrl());
						System.out.println(tabString + "Sub Org.Name=" + item.getName());
												
						// recursive call
						processNextLevel( webClient,item.getUrl(),sumLevel + "|" + item.getName(), mailingAddress, fax);  // comment out for testing
					}
				}
												
				// list of employees at this level
				getEmployees(doc, sumLevel, mailingAddress, fax, tabString);   // comment out for testing
				
        	}
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
        
		
		recursionLevel -= 1;
	}
	
	// prints all employee info from document (doc)
    void getEmployees(Document doc, String sumLevel, String mailingAddress, String faxNumber, String tabString) throws FailingHttpStatusCodeException, MalformedURLException, IOException {

    	System.out.println(tabString + "==================Employees: ====================");
        //Elements employees =  doc.getElementById("employees").children();
        Element e = doc.getElementById("employees");
        if( e != null )
        {
        	Elements employees = e.children();
         
	        System.out.println(tabString + "Num.Employees=" + employees.size());
	
	        for (Element employee : employees){
	            //System.out.println(employee.select("span.small"));
	
	            int i = 1;
	            for (Element info : employee.children()) {
	                if (i == 1) {
	                	String empData = info.text();
	                    System.out.println(tabString + empData);
	                    
	                    // extract useful employee data
	                    
	                    String[] empDataParts = empData.split(Pattern.quote("|"));
	                    int len = empDataParts.length;
	                    
	                    jobTitle = "";
	                    telephone = "";
	                    email = "";
	                    fax = faxNumber;
	                    
	                    String fullName = "";
	                    if(len >= 1) fullName = empDataParts[0].trim();
	                    if(len >= 2) jobTitle = empDataParts[1].trim();
	                    if(len >= 3) telephone = empDataParts[2].trim();
	                    if(len >= 4) email = empDataParts[3].trim();
	                    
	                    // break full name
	                    
	                    firstName = "";
	                    lastName = "";
	                    int k = fullName.lastIndexOf(" ");
	                    if( k > -1)
	                    {
	                    	lastName = fullName.substring(k).trim();
	                    	firstName = fullName.substring(0,k).trim();
	                    	
	                    	k = firstName.lastIndexOf(".");
	                    	if( k > -1 )
	                    	{
	                    		// remove titule from first name
	                    		firstName = firstName.substring(k+1).trim();
	                    	}
	                    }
	                    else
	                    {
	                    	lastName = fullName;
	                    }
	             
	                    // break organisation levels
	                  	
	                    String tmpSumLevel = sumLevel;
	                    String level1Dept = "";
	                    String level2Dept = "";
	                    String level3Dept = "";
	                    String level4Dept = "";
	                    String level5Dept = "";
	                    String level6Dept = "";
	                    String level7Dept = "";
	                    String level8Dept = "";
	                    String level9Dept = "";
	                    String level10Dept = "";
	                    String level11Dept = "";
	                    String lowestDept = "";
	                    
	                    String[] sumLevelParts = sumLevel.split(Pattern.quote("|"));
	                    int sumLevelLen = sumLevelParts.length;
	                    for( int j = 0; j < sumLevelLen; j++)
	                    {
	                       switch(j)
	                       {
		                       case 0: level1Dept = sumLevelParts[j];
				                       lowestDept = level1Dept;
				                       break;
		                       case 1: level2Dept = sumLevelParts[j];
				                       lowestDept = level2Dept;
				                       break;
		                       case 2: level3Dept = sumLevelParts[j];
				                       lowestDept = level3Dept;
				                       break;
		                       case 3: level4Dept = sumLevelParts[j];
				                       lowestDept = level4Dept;
				                       break;
		                       case 4: level5Dept = sumLevelParts[j];
				                       lowestDept = level5Dept;
				                       break;
		                       case 5: level6Dept = sumLevelParts[j];
				                       lowestDept = level6Dept;
				                       break;
		                       case 6: level7Dept = sumLevelParts[j];
				                       lowestDept = level7Dept;
				                       break;
		                       case 7: level8Dept = sumLevelParts[j];
				                       lowestDept = level8Dept;
				                       break;
		                       case 8: level9Dept = sumLevelParts[j];
				                       lowestDept = level9Dept;
				                       break;
		                       case 9: level10Dept = sumLevelParts[j];
				                       lowestDept = level10Dept;
				                       break;
		                       case 10:level11Dept = sumLevelParts[j];
				                       lowestDept = level11Dept;
				                       break;
		                    }
	                    }
	                    
	                    // break mailing address

	                    // addres examples:
                        // 159 Cedar St, Sudbury, ON P3E 6A5                                                // 3 fields address
                        // Main Legislative Bldg, Queen's Park, Toronto, ON M7A 1A1
                        // Main Legislative Bldg 3rd Flr Rm 387A, Queen's Park, Toronto, ON M7A 1A2
	                    // Suite 900, 2 Bloor St W, Toronto, ON M4W 3E2                                     // 4 fields address
                        // Bell Trinity Sq, South Tower 10th Flr, 483 Bay St, Toronto, ON M5G 2C9           // 5 fields address
	                    // 501 Pennsylvania Ave NW, Washington, DC 20001                                    // 3 fields American address

	                    String[] mailingAddressParts = mailingAddress.split(Pattern.quote(","));
	                    int mailingAddressLen = mailingAddressParts.length;
	                    
	                    if(mailingAddressLen > 0)
	                    {
	                    	// postal code is always last field
	                    	String rawPostalCode = mailingAddressParts[mailingAddressLen-1].trim();
	                       	postalCode = getPCode(rawPostalCode); // extract just postal code
	                       	int k1 = rawPostalCode.indexOf(postalCode);
	                       	if(k1 > -1)
	                       	{
	                       		province = rawPostalCode.substring(0,k1).trim();  // province (or state for USA) is before actual postal code
	                       	}
	                       	
	                       	if(mailingAddressLen > 1)
		                    {
		                       // city is always second to last
	                           city = mailingAddressParts[mailingAddressLen-2].trim();
		                    }
	                       	
	                       	if(mailingAddressLen == 3)
		                    {
	                       		address1 = mailingAddressParts[0].trim();
		                    }
	                       	else if(mailingAddressLen == 4)
		                    {
	                       		address1 = mailingAddressParts[0].trim();
	                       		address2 = mailingAddressParts[1].trim();
		                    }
	                       	else if(mailingAddressLen == 5)
		                    {
	                       		address1 = mailingAddressParts[0].trim() + ", " + mailingAddressParts[1].trim();
	                       		address2 = mailingAddressParts[2].trim();
		                    }
	     
	                    }
	                    
	                    // dump employee record
	                    
	                    recordCount++;
	                    String timeStamp = getTimeStamp();
	                    
	                    System.out.println(tabString + "recCount   =" + recordCount);
	                    System.out.println(tabString + "timeStamp  =" + timeStamp);
                        System.out.println(tabString + "fname      =" + firstName);
                        System.out.println(tabString + "lname      =" + lastName);
                        System.out.println(tabString + "jobTitle   =" + jobTitle);
                        System.out.println(tabString + "sumLevel   =" + tmpSumLevel);
                        System.out.println(tabString + "level1     =" + level1Dept);
                        System.out.println(tabString + "level2     =" + level2Dept);
                        System.out.println(tabString + "level3     =" + level3Dept);
                        System.out.println(tabString + "level4     =" + level4Dept);
                        System.out.println(tabString + "level5     =" + level5Dept);
                        System.out.println(tabString + "level6     =" + level6Dept);
                        System.out.println(tabString + "level7     =" + level7Dept);
                        System.out.println(tabString + "level8     =" + level8Dept);
                        System.out.println(tabString + "level9     =" + level9Dept);
                        System.out.println(tabString + "level10    =" + level10Dept);
                        System.out.println(tabString + "level11    =" + level11Dept);
                        System.out.println(tabString + "lowest     =" + lowestDept);
                        System.out.println(tabString + "phone      =" + telephone );
                        System.out.println(tabString + "fax        =" + fax );
                        System.out.println(tabString + "email      =" + email);
                        System.out.println(tabString + "address1   =" + address1 );
                        System.out.println(tabString + "address2   =" + address2 );
                        System.out.println(tabString + "city       =" + city );
                        System.out.println(tabString + "province   =" + province );
                        System.out.println(tabString + "postalCode =" + postalCode );
                        System.out.println(tabString + "------------------------------------------------------------");
	                

	                    osd.println("\"" + recordCount                   + "\"," +
	                                "\"" + timeStamp                     + "\"," +
	                                "\"" + firstName                     + "\"," +
	                                "\"" + lastName                      + "\"," +
	                                "\"" + jobTitle                      + "\"," +
	                                "\"" + tmpSumLevel                   + "\"," +
	                                "\"" + level1Dept                    + "\"," +
	                                "\"" + level2Dept                    + "\"," +
	                                "\"" + level3Dept                    + "\"," +
	                                "\"" + level4Dept                    + "\"," +
	                                "\"" + level5Dept                    + "\"," +
	                                "\"" + level6Dept                    + "\"," +
	                                "\"" + level7Dept                    + "\"," +
	                                "\"" + level8Dept                    + "\"," +
	                                "\"" + level9Dept                    + "\"," +
	                                "\"" + level10Dept                    + "\"," +
	                                "\"" + level11Dept                    + "\"," +
	                                "\"" + lowestDept                    + "\"," +
	                                "\"" + telephone                     + "\"," +
	                                "\"" + email                         + "\"," +
	                                "\"" + fax                           + "\"," +
	                                "\"" + address1                      + "\"," +
	                                "\"" + address2                      + "\"," +
	                                "\"" + city                          + "\"," +
	                                "\"" + province                      + "\"," +
	                                "\"" + postalCode                    + "\"" );

	                    osd.flush();
	                    
	                    
	                }
	                i = i + 1;
	            }
	        }
        }
        return;
    }

    // returns list of objects containing url/name for organisations under specified doc
    ArrayList<SubOrganisation> getReportOrgs(Document doc) throws FailingHttpStatusCodeException, MalformedURLException, IOException {

        ArrayList<SubOrganisation> listSubOrgs = new ArrayList<SubOrganisation>();

        //System.out.println(doc.getElementsByAttributeValue("data-i18n","orgprofile.reportingorgs").get(0).parent().parent().parent().outerHtml());
        Element root = doc.getElementsByAttributeValue("data-i18n","orgprofile.reportingorgs").get(0).parent().parent().parent();

        for(Element org : root.select("span > a")){
            //System.out.println(org.outerHtml());
            String url = "http://www.infogo.gov.on.ca/infogo/home.html#orgProfile/" + org.attr("data-org-id") + "/en";
            String name = org.text();
            //System.out.println(name);
            //System.out.println(url);
            listSubOrgs.add(new SubOrganisation(url, name));
        }

        return listSubOrgs;
    }


    // tells us whether there are more sub organisations below
    boolean hasReportOrg(Document doc) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        return doc.getElementsByAttributeValue("data-i18n", "orgprofile.reportingorgs").size() == 1;
    }
    
    ////////////////////////////////////////////////////////////////////
    // Functions that check if said attributes (fax and address) exist 
    ////////////////////////////////////////////////////////////////////

    Boolean hasFax(Document doc) throws FailingHttpStatusCodeException, IOException{                                    
                                                                                                                        
        // if found, the count returned by size() is actually 2 and NOT 1 because the html                               
        // accounts for various screen size cases ( 2 of them so 2 identical elements are found)                         
        return doc.getElementsByAttributeValue("aria-label", "Fax").size() == 2;                                        
    } 

    Boolean hasMailAddr(Document doc) throws FailingHttpStatusCodeException, IOException{                               
                                                                                                                        
        // if found, the count returned by size() is actually 2 and NOT 1 because the html                               
        // accounts for various screen size cases ( 2 of them so 2 identical elements are found)                         
        return doc.getElementsByAttributeValue("data-i18n", "profile.mailaddress").size() == 2;                         
    }  

    ////////////////////////////////////////////////////////////////
    // Functions that actually get the attributes (fax and address)  
    ////////////////////////////////////////////////////////////////
                                                                                                                        
    String getFax (Document doc) throws FailingHttpStatusCodeException, IOException{
    	
    	String fax = "";
    	
        if (hasFax(doc))
        {
            //System.out.println("\t-->" +doc.getElementsByAttributeValue("aria-label", "Fax").text());
            Element root = doc.getElementsByAttributeValue("aria-label", "Fax").parents().get(3);

            //text variable is all the text inside the final html block (could be one phone number, could be a paragraph)
            String text = root.select("div > div.row > div > span.small").text();

            //figure out if there are multiple fax numbers in block of text
            // 1 is OK
            // more than 1 is a problem - issue warning statement (multiple numbers found!)
            Pattern pattern = Pattern.compile("[\\d]{3}-[\\d]{3}-[\\d]{4}");
            Matcher  matcher = pattern.matcher(text);
            int phone_numbers = 0;
            while (matcher.find()){
                phone_numbers++;
            }

            if(phone_numbers == 1)
            {
            	// return fax only if ONE number is found
            	
                //System.out.println("Fax Found");
                //System.out.println(text);
                fax = text.trim();
            }
            else 
            {
                System.out.println("Warning: multiple fax numbers encountered – field will be left blank. Fax=" + text);
            }

        }
        else{
            //nothing to get
            //System.out.println("No Fax Found");
        }
        
        return fax;
    }
    

    String getMailAddr(Document doc) throws FailingHttpStatusCodeException, IOException{         
    	
       String address = "";
       
       if(hasMailAddr(doc)){                                                                                            
           //System.out.println("Has Mail Addr");                                                                         
           Element root = doc.getElementsByAttributeValue("data-i18n", "profile.mailaddress").parents().get(3);     
           address = root.select("div.row > div > span.small").text();
           //System.out.println("getMailAddr(): Addr.found=" + address );                                        
       }                                                                                                                
       else{  
    	   // nothing to get   
           //System.out.println("getMailAddr(): No MailAddr found");                                                               
       }     
       
       return address;
    }     
    
    // get today's date 
    
    private String getTimeStamp(){
          Calendar now  = Calendar.getInstance();

          int month     = (1+now.get(Calendar.MONTH));
          int day       = now.get(Calendar.DAY_OF_MONTH);

          String smonth = "";
          String sday   = "";

          smonth = ""+month;
          if(month < 10)  smonth = "0" + smonth;

          sday = "" + day;
          if(day < 10)    sday   = "0" + sday;

          return(now.get(Calendar.YEAR) + "-" +  smonth + "-" +  sday );
     }
    
    private String getPCode( String pcode )
    {
    	// ON M5G 2C9  - canadian pcode
    	// DC 20001    - american zip
        String tmp = "";
        int len = pcode.length();
        int i = 0;

        if( len < 8 ) return ("");

        // check if it is canadian pcode
        for( i = 0; i < len-6; i++ )
        {
            tmp = pcode.substring(i,i+7);
            if( !Character.isDigit(tmp.charAt(0)) &&
                Character.isDigit(tmp.charAt(1)) &&
                !Character.isDigit(tmp.charAt(2)) &&
                !Character.isDigit(tmp.charAt(3)) &&
                Character.isDigit(tmp.charAt(4)) &&
                !Character.isDigit(tmp.charAt(5)) &&
                Character.isDigit(tmp.charAt(6)) )
            {
                return tmp;
            }
        }
        
        // try american
        for( i = 0; i < len-4; i++ )
        {
            tmp = pcode.substring(i,i+5);
            if( Character.isDigit(tmp.charAt(0)) &&
                Character.isDigit(tmp.charAt(1)) &&
                Character.isDigit(tmp.charAt(2)) &&
                Character.isDigit(tmp.charAt(3)) &&
                Character.isDigit(tmp.charAt(4)) )
            {
                return tmp;
            }
        }

        return "";
    }
 
}














