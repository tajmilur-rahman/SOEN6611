package execute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Commit;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class VinodExtractor {
	
	private static final String DELIMITER = "«";
	private static final String OUTPUT_PATH = "out\\"; 
	private static final String ANT_COMMIT_FILE_PATH = OUTPUT_PATH + "ant_commits.txt";
	private static final String ANT_MODIFIED_FILES_PATH = OUTPUT_PATH + "ant_modified_files.txt";

	private static final List<Commit> from15to16 = new ArrayList<>();
	private static final List<Commit> from16to17 = new ArrayList<>();
	private static final List<Commit> from17to18 = new ArrayList<>();
	private static final List<Commit> from18to19 = new ArrayList<>();
	private static final List<Commit> from19toNow = new ArrayList<>();
	
	public static void main(String[] args) throws IOException, ParseException {

		Date rev1_5 = Runner.DATEFORMAT.parse("2002-07-10 00:00:00 -0500");	
		Date rev1_6 = Runner.DATEFORMAT.parse("2003-12-18 00:00:00 -0500");
		Date rev1_7 = Runner.DATEFORMAT.parse("2006-12-19 00:00:00 -0500");
		Date rev1_8 = Runner.DATEFORMAT.parse("2010-02-08 00:00:00 -0500");
		Date rev1_9 = Runner.DATEFORMAT.parse("2013-03-07 00:00:00 -0500");
		
		String antLog = Files.asCharSource(new File("small_history.txt"), Charset.defaultCharset()).read();
		//String antLog = Files.asCharSource(new File("testlog.txt"), Charset.defaultCharset()).read();

		String adjusted = antLog.replaceAll("(?m)^[ \t]*\r?\n", ""); // remove all empty lines
		
		List<String> splitCommits = Arrays.asList(adjusted.split("------------------------------------------------------------------------"));
		Commit commitObject;
		List<Commit> allCommitObjects = new ArrayList<>();
		for(String commit: splitCommits) {
			commitObject = new Commit();
			
			Boolean modifiedFileFound = false;
			for(String line: commit.split("\r\n")) {
				if (line.trim().isEmpty()) continue;
				
				if (isHeaderLine(line)) {
					// r1561742 | cduffy | 2014-01-27 11:51:30 -0500 (Mon, 27 Jan 2014) | 1 line
					String[] header = line.split("\\|");
					commitObject.revisionID = header[0].trim();
					commitObject.userID = header[1].trim();
					commitObject.commitDate = Runner.DATEFORMAT.parse(header[2].trim().split("\\(")[0].trim());
					commitObject.linesChanged = header[3].trim();
					
				} else if (isModifiedFileLine(line)) {
					commitObject.modifiedFiles.add(line.trim());
					modifiedFileFound = true;
				} else {
					if (modifiedFileFound) {
						commitObject.commitLogs.add(line.trim());						
					}
				}
			}
			
			if (commitObject.revisionID != null) {
				allCommitObjects.add(commitObject);
			}

		}
		
		// Sort the commits into different buckets
		for(Commit c: allCommitObjects) {
			if (c.revisionID.isEmpty()) continue;			
			if(c.getCommitDate().before(rev1_6)) {
				from15to16.add(c);
			} else if (c.getCommitDate().before(rev1_7)) {
				from16to17.add(c);
			} else if (c.getCommitDate().before(rev1_8)) {
				from17to18.add(c);
			} else if (c.getCommitDate().before(rev1_9)) {
				from18to19.add(c);
			} else {
				from19toNow.add(c);
			}
		}
		
		printStatistics();		
		createOutputfiles(allCommitObjects);
		
	}

	private static void printStatistics() {
		System.out.println("Between 1.5 and 1.6: " + from15to16.size());
		System.out.println("Between 1.6 and 1.7: " + from16to17.size());
		System.out.println("Between 1.7 and 1.8: " + from17to18.size());
		System.out.println("Between 1.8 and 1.9: " + from18to19.size());
		System.out.println("Between 1.9 and Now: " + from19toNow.size());
	}

	private static void createOutputfiles(List<Commit> allCommitObjects) throws IOException {
		// Create two files
		// One will have the ant_commits, the other will have ant_modifiedfiles
		
		StringBuilder commitLine;
		StringBuilder modifiedFiles;

		// delete both output files first
		FileUtils.deleteDirectory(new File(OUTPUT_PATH));
		
		// create both files in UTF_8
		File path = new File(OUTPUT_PATH);
		path.mkdir();
				
		for(Commit c: allCommitObjects) {
			commitLine = new StringBuilder();
			commitLine.append(c.revisionID);
			commitLine.append(DELIMITER);
			commitLine.append(c.userID);
			commitLine.append(DELIMITER);
			commitLine.append(Runner.DATEFORMAT.format(c.getCommitDate()));
			commitLine.append(DELIMITER);
			commitLine.append(c.commitLogs.toString());
			commitLine.append(DELIMITER);
			commitLine.append(c.linesChanged);
			
			//FileUtils.writeStringToFile(new File(ANT_COMMIT_FILE_PATH), commitLine.toString(), Charsets.UTF_8, true);
			Files.append(commitLine.toString(), new File(ANT_COMMIT_FILE_PATH), Charsets.UTF_8);
			Files.append("\r\n", new File(ANT_COMMIT_FILE_PATH), Charsets.UTF_8);
			
			for(String line: c.modifiedFiles) {
				modifiedFiles = new StringBuilder();
				modifiedFiles.append(c.revisionID);
				modifiedFiles.append(DELIMITER);
				modifiedFiles.append(line.trim());

				//FileUtils.writeStringToFile(new File(ANT_MODIFIED_FILES_PATH), modifiedFiles.toString(), Charsets.UTF_8, true);
				Files.append(commitLine.toString(), new File(ANT_MODIFIED_FILES_PATH), Charsets.UTF_8);
				Files.append("\r\n", new File(ANT_MODIFIED_FILES_PATH), Charsets.UTF_8);
			}
		}
		System.out.println("Done!");
		
	}

	private static boolean isModifiedFileLine(String line) {
		StringBuilder regex = new StringBuilder() ;
		//   M /ant/core/trunk/WHATSNEW

		regex.append("\\s\\s\\s([ADM].*?)");
		
		Pattern headerPattern = Pattern.compile(regex.toString());
		Matcher headerMatcher = headerPattern.matcher(line);
		
		return headerMatcher.find();
	}

	private static boolean isHeaderLine(String line) {
		StringBuilder regex = new StringBuilder() ;
		regex.append("(r\\d+)\\s\\|"); // 1 revID
		regex.append("\\s(.*?)\\s\\|"); // 2 userID
		regex.append("\\s(.*?)\\s\\|"); // 3 date
		
		Pattern headerPattern = Pattern.compile(regex.toString());
		Matcher headerMatcher = headerPattern.matcher(line);
		
		return headerMatcher.find();
		
	}

}
