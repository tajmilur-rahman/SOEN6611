import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {

	public static void main(String[] args) 
    {
	    System.out.println("Reading File from Java code");
	    //Name of the file
	    String fileName="C:\Users\Home\git\SOEN6611_Taj\oss_projects\ant";
	    try{
	
		    //Create object of FileReader
		    FileReader inputFile = new FileReader(fileName);
		
		    //Instantiate the BufferedReader Class
		    BufferedReader bufferReader = new BufferedReader(inputFile);
		
		    //Variable to hold the one line data
		    String line, revisionId;
		    String pattern = "^r\\d";
		    Pattern r;
		    Matcher m;
		
		    // Read file line by line and print on the console
		    while ((line = bufferReader.readLine()) != null)   {
		            System.out.println("Current line reading: "+line);
		            
		            // Create a Pattern object
		            r = Pattern.compile(pattern);
		            m = r.matcher(line);
		            
		            if(m.find()){
		            	// It is a line starting with rddddddd, so extract it farther
		            	System.out.println("\tThis line starts with with a revision number\n");
		            	
		            	// Change pattern and search farther in this line
		            	pattern = "^r(\\d)+";
		            	r = Pattern.compile(pattern);
		            	m = r.matcher(line);
		            	revisionId = m.group();
		            	System.out.println("\tRevision ID: "+revisionId+"\n");
		            	
		            	break;
		            }
		    }
		    
		    //Close the buffer reader
		    bufferReader.close();
	    }catch(Exception e){
	    	System.out.println("Error while reading file line by line:" + e.getMessage());                      
	    }

    }
}
