package execute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Commit;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

public class VinodExtractor {
	
	private static final String DELIMITER = "«";

	public static void main(String[] args) throws IOException, ParseException {

		Date rev1_5 = Runner.DATEFORMAT.parse("2002-07-10 00:00:00 -0500");	
		Date rev1_6 = Runner.DATEFORMAT.parse("2003-12-18 00:00:00 -0500");
		Date rev1_7 = Runner.DATEFORMAT.parse("2006-12-19 00:00:00 -0500");
		Date rev1_8 = Runner.DATEFORMAT.parse("2010-02-08 00:00:00 -0500");
		Date rev1_9 = Runner.DATEFORMAT.parse("2013-03-07 00:00:00 -0500");

		
		//String antLog = Files.asCharSource(new File("small_history.txt"), Charset.defaultCharset()).read();
		String antLog = Files.asCharSource(new File("testlog.txt"), Charset.defaultCharset()).read();

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
		
		// allCommitObjects will be full of every commit
		System.out.println(allCommitObjects.size());
		
		List<Commit> from15to16 = new ArrayList<>();
		List<Commit> from16to17 = new ArrayList<>();
		List<Commit> from17to18 = new ArrayList<>();
		List<Commit> from18to19 = new ArrayList<>();
		List<Commit> from19toNow = new ArrayList<>();

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
		
		System.out.println("Between 1.5 and 1.6: " + from15to16.size());
		System.out.println("Between 1.6 and 1.7: " + from16to17.size());
		System.out.println("Between 1.7 and 1.8: " + from17to18.size());
		System.out.println("Between 1.8 and 1.9: " + from18to19.size());
		System.out.println("Between 1.9 and Now: " + from19toNow.size());

		
		
		createOutputfiles(allCommitObjects);
		
		
		/*
		
		String[][] Ant_infoS = new String[20000][5];
		String[][] Ant_pathS = new String[70000][4];
		// Variable to hold the one line data
		String line, previousLine="";
		int outer = 0,  outerP = 0,count;
		String concatLogMessage = "",revisionTemp = "",concatMessageTemp = "";
		boolean messageFlag = false, pathFlag = false;
		System.out.println("Reading File from Java code");
		// Name of the file
		// String fileName =
		// "C:\\Users\\Home\\git\\SOEN6611_Taj\\oss_projects\\ant\\ant-core-history.txt";
		String fileName = "ant-core-history.txt";
		try {

			// Create object of FileReader
			FileReader inputFile = new FileReader(fileName);

			// Instantiate the BufferedReader Class
			BufferedReader bufferReader = new BufferedReader(inputFile);

		
			// Read file line by line and print on the console
			while ((line = bufferReader.readLine()) != null) {
				count=0;
				// Partition the line contains revision id that start with
				// r0..r9
				if (line.startsWith("r1") || line.startsWith("r2")
						|| line.startsWith("r3") || line.startsWith("r4")
						|| line.startsWith("r5") || line.startsWith("r6")
						|| line.startsWith("r7") || line.startsWith("r8")
						|| line.startsWith("r9") || line.startsWith("r0")) {
					String temp = "";
					for (int i = 0; i < line.length(); i++) {
						char d = line.charAt(i);
						if (d == '|') {
							if (count == 2) {
								temp = temp.substring(0, 20);
								Ant_infoS[outer][2] = temp; // extract the
															// commit date
							} else if (count == 0) {
								Ant_infoS[outer][0] = temp; // extract the
															// revision id
								revisionTemp = temp;
							} else if (count == 1)

							{
								Ant_infoS[outer][1] = temp; // extract the user
															// id
							}

							temp = "";
							count++;

						} else {

							temp += d;

						}
						if (count == 3 && (i == line.length() - 1)) {
							//System.out.println("where is this " + temp);
							Ant_infoS[outer][4] = temp; // extract the LOC

						}

					}
					outer++;
				}

				else if ((previousLine.startsWith("   M")
						|| previousLine.startsWith("   A") || previousLine
							.startsWith("   D")) && line.isEmpty()) {
					messageFlag = true;
				}

				if (line.startsWith("--------------")) {
					messageFlag = false;
					Ant_infoS[outer][3] = concatLogMessage; // extract the log
															// message
					concatMessageTemp = concatLogMessage;
					concatLogMessage = "";
				}

				if (messageFlag == true && previousLine.isEmpty() == true) {
					concatLogMessage += line; // Concatenate the log message
												// which is mentioned in more
												// than one line

				}

				if (previousLine.startsWith("Changed paths:")) {

					pathFlag = true;
				}

				if (line.isEmpty() == false && pathFlag == true) {
					// System.out.println("the below are paths " + line);
					Ant_pathS[outerP][3] = line; // extract the path
					if (line.startsWith("   M")) {
						Ant_pathS[outerP][2] = "M"; // extract the value based
													// on change type
					} else if (line.startsWith("   A")) {
						Ant_pathS[outerP][2] = "A";
					} else if (line.startsWith("   D")) {
						Ant_pathS[outerP][2] = "D";
					}
					Ant_pathS[outerP][0] = revisionTemp;
					if (concatMessageTemp.contains("Bugzilla")
							|| concatMessageTemp.contains("Bug")
							|| concatMessageTemp.contains("bug")) {
						concatMessageTemp.matches("[0-9][0-9][0-9].*");
						String str = concatMessageTemp.replaceAll(
								"[^\\.0123456789]", "");
						if (str.isEmpty()) {
							Ant_pathS[outerP][1] = "999"; // the word bug is
															// mentioned but id
															// not found
						} else {
							Ant_pathS[outerP][1] = str; // extract bug id from
														// the log message
						}

					} else {
						Ant_pathS[outerP][1] = "0"; // the word "bug" is not
													// found
					}
					outerP++;
				} else {
					pathFlag = false;
				}

				previousLine = line;
			}

			// Close the buffer reader
			bufferReader.close();
		} catch (Exception e) {
			System.out.println("Error while reading file line by line:"
					+ e.getMessage());
		}

		PrintWriter writerAntInfo, writerAntPath;
		try {
			writerAntInfo = new PrintWriter("Ant_Info.txt", "UTF-8");
			for (int i = 0; i < Ant_infoS.length; i++) {
				for (int j = 0; j < 5; j++) {

					writerAntInfo.print(Ant_infoS[i][j]);
					if (j != 4) {
						writerAntInfo.print(DELIMIETER);
					}
				}
				writerAntInfo.println();
			}
			writerAntInfo.println();
			writerAntPath = new PrintWriter("Ant_Path.txt", "UTF-8");
			for (int i = 0; i < Ant_pathS.length; i++) {
				for (int j = 0; j < 4; j++) {
					writerAntPath.print(Ant_pathS[i][j]);
					if (j != 3) {
						writerAntPath.print(DELIMIETER);
					}
				}
				writerAntPath.println();
			}
			writerAntInfo.close();
			writerAntPath.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated
			// catch block
			e.printStackTrace();
		}
*/	}

	private static void createOutputfiles(List<Commit> allCommitObjects) throws IOException {
		// Create two files
		// One will have the ant_commits, the other will have ant_modifiedfiles
		
		StringBuilder commitLine;
		StringBuilder modifiedFiles;

		// delete both output files first
		File antCommits = new File("ant_commits");
		antCommits.delete();
		File antModifiedFiles = new File("ant_modified_files");
		antModifiedFiles.delete();
		
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
			
			System.out.println(c.toString());
			Files.asCharSink(new File("ant_commits.txt"), Charset.defaultCharset(), FileWriteMode.APPEND).writeLines(Arrays.asList(commitLine.toString()));
			
			for(String line: c.modifiedFiles) {
				modifiedFiles = new StringBuilder();
				modifiedFiles.append(c.revisionID);
				modifiedFiles.append(DELIMITER);
				modifiedFiles.append(line.trim());

				Files.asCharSink(new File("ant_modified_files.txt"), Charset.defaultCharset(), FileWriteMode.APPEND).writeLines(Arrays.asList(modifiedFiles.toString()));
			}
			
			
			
		}
		
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
