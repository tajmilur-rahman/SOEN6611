package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ModifiedCommitWriter {
	// The output of this is a file which looks like this:
	// only want JAVA files
	// commitID1, file1, file2, file3
	// commitID2, file1, file4, file5
	// ...
	
	private static final String OUTPUT_PATH = "processed/"; 
	
	List<Commit> commits;
	String fileName;
	
	public ModifiedCommitWriter(List<Commit> commits, String fileName) {
		this.fileName = fileName;
		this.commits = commits;
	}
	
	public void writeOutput() throws IOException {
		// create both files in UTF_8
		File path = new File(OUTPUT_PATH);
		path.mkdir();
		FileWriter fw = new FileWriter(OUTPUT_PATH + fileName, false);
		
		String outputLine = "";
				
		for (Commit c: commits) {
			// skip commits which have less than 2 modified java files
			if (c.commitLogsByType.get("M").size() < 2) continue;
			
			outputLine = (c.revisionID + ", " + c.commitLogsByType.get("M").toString()).replace("[", "").replace("]", "");
			fw.write(outputLine + "\r\n");

		}
		
		System.out.println("Writing to transaction file done!");
		fw.close();
		
	}
	
}
