
March 24, 2017 Version 1.1

General
=======
DbgOnt is a Java stand-alone application used to grab contact information data posted
on a specific Ontario government web URL by traversing the department tree containing employee information.
It's only input is a configuration XML file which is read immediately upon startup. The output is a
comma delimited text file containing retrieved data.
It requires Java 1.7 run time or later to be installed on the target machine. 
Some queries may not return all data fields specified below. This can happen in cases where data is not
existent in the targeted web page.
This application uses JavaScript engine which is required to process html data returned by targeted URL.
General syntax to run this application (assuming Java run time is already installed, jar files are in jar folder while class files are in mainpackage folder) is:
java -classpath .;./bin;jar/htmlunit-2.24-OSGi.jar;jar/jsoup-1.10.2.jar mainpackage.DbgOnt <input config XML file> 

Input Configuration XML file
============================
- Has to be specified as the only parameter to DbgOnt Java program
- Contains following configurable parameters:
  - TreeTimeout - timeout in miliseconds. Program will pause for this amount of time between processing new page (department) of information. Default is 50 ms.
  - RecordTimeout - timeout in miliseconds. Program will pause for this amount of time between processing employee records. Default is 50 ms.
  - ConnectionTimeout - is the amount of time, in milliseconds, the program will wait to read the data. Default is 3000 ms.
  - BaseUrl - top level department listing URL. Default is "http://www.infogo.gov.on.ca/infogo/home.html"
  - OutputPath - a complete path pointing to the existent directory where the results (OutputFile) will be written.
    Final '\' must not be specified. Default is '.'
  - OutputFile - the name of the comma separated text output file containing the results. Default is "DbgOnt_out.txt"
  - DirDelimiter - defines character used as delimiter between OutputPath and OutputFile. Default is '/'
  - Browser - Defines the type of browser this application will emulate when requesting data from target web server
              The possible values are: "CHROME", "EXPLORERv, "FIREFOX" and "EDGE"  ( default is "CHROME" )
  - OrgProfile - "0" (default)This is the organisation code from DbgOntList.txt file which is always generated on startup
  - NextRecord - "0" (default)If this is specified or other than “0” (zero), the new data will be APPENDED to existing output data file and the value will be the count of the first record (appended).
  - ContinueProcessing - "NO" (default)This is in effect only if OrgProfile (above) is specified and not “0” (zero): If set to “YES”, processing will start with OrgProfile and continue until end. If set to “NO”, ONLY OrgProfile will be processed.

- Has to conform to strict XML syntax
- The directory specified in OutputPath must be existent and have write permission.

NOTE: Although base URL can be configured, the whole application is heavily customized for this particular
      URL. Therefore, any change that data provider may introduce in the future may result in the partial
      or complete loss of functionality.

Output Results File
====================
Output results file is a simple comma delimited text ASCII file.
The following fields are written as a data record row for each contact retrieved from target URL:

"recordCount"  
"timeStamp"          
"firstName"            
"lastName"
"jobTitle"
"titule"
"departmentTree"       
"level1Dept"
"level2Dept"
"level3Dept"
"level4Dept"
"level5Dept"
"level6Dept"
"level7Dept"
"level8Dept"
"level9Dept"
"level10Dept"
"level11Dept"
"lowestDep"          
"telephone"           
"email"    
"fax"
"address1"
"address2"
"city"
"province"
"postalCode"
"country"       
"creationTime"         
"lastModified"                 

Dependencies
=============
Main Java class file is mainpackage.DbgOnt.class. All other .class files that comprise this Java executable and must be present in the mainpackage directory.
The following files must be present in the directory relative from which the DbgOnt is ran, or, CLASSPATH environment variable  has to be set
to their exact location:
- jar/htmlunit-2.24-OSGi.jar
- jar/jsoup-1.10.2.jar

runDbgOnt.bat is an example of a script to run DbgOnt Java application.

Recommended Usage
==================
Create a script to run the program runDbgOnt.bat (example shown below):

-------------------------------------- SCRIPT -----------------------------------------------------------------
java -version
java -classpath .;./bin;jar/htmlunit-2.24-OSGi.jar;jar/jsoup-1.10.2.jar mainpackage.DbgOnt DbgOnt.xml > DbgOnt.log
---------------------------------------------------------------------------------------------------------------

Make sure that the location from which this script is run contains the following folders/files:

input parameter file: ./DbgOnt.xml
Java class files:     mainpackage/DbgOnt.class
                      mainpackage/DbgOntProcessor.class
Java jar files:       jar/htmlunit-2.24-OSGi.jar
                      jar/jsoup-1.10.2.jar

As can be seen from the script, an extensive output is redirected to DgbOnt.log log file which could be used for debugging if problems
are encountered.

Please note that this application may run for a long time, depending on internet connection speed, target URL traffic and amount of data 
at target URL: Tests have shown that for this site about 6,000 records per hour can be achieved. The site may contain 40,000+ records.










