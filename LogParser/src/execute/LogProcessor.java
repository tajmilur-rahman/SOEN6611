package execute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Commit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

public class LogProcessor {
	
	static Logger logger = Logger.getLogger(LogProcessor.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(LogProcessor.class.getPackage().getName()));
	}
	

	public Map<String, List<Commit>> process(List<String> paths) throws IOException, ParseException {
		Map<String, List<Commit>> commitsPerProject = new HashMap<>();
		
		for(String file: paths) {
			commitsPerProject.put(file, processFile(file));
		}
		
		return commitsPerProject;
	}

	private List<Commit> processFile(String file) throws IOException, ParseException {
		
		String fileContents = Files.asCharSource(new File(file), Charset.defaultCharset()).read();

		String adjusted = fileContents.replaceAll("(?m)^[ \t]*\r?\n", ""); // remove all empty lines
		
		List<String> splitCommits = Arrays.asList(adjusted.split("------------------------------------------------------------------------"));
		Commit commitObject;
		List<Commit> allCommitObjects = new ArrayList<>();
		
		Multimap<String, String> problematicCommitsFoundInFiles = ArrayListMultimap.create();
		
		Map<String, Commit> revisionIDToCommit = new HashMap<>();
		for(String commit: splitCommits) {
			commitObject = new Commit();
			Boolean modifiedFileFound = false;
			for(String line: commit.split("\r\n")) {
				if (line.trim().isEmpty()) continue;
				
				//logger.info(line);
				
				if (isHeaderLine(line)) {
					// r1561742 | cduffy | 2014-01-27 11:51:30 -0500 (Mon, 27 Jan 2014) | 1 line
					String[] header = line.split("\\|");
					commitObject.revisionID = header[0].trim();
					commitObject.userID = header[1].trim();
					commitObject.commitDate = NLPSRunner.DATEFORMAT.parse(header[2].trim().split("\\(")[0].trim());
					commitObject.linesChanged = header[3].trim();
					
				} else if (isModifiedFileLine(line)) {
					// For this project, we'll take all files, not just Java files.
					//if (!line.contains(".java")) continue; //only takes java files
					commitObject.modifiedFiles.add(line.trim());
					commitObject.commitLogsByType.put(line.trim().split("\\s")[0].trim(), line.trim().split("\\s", 2)[1].trim());
					modifiedFileFound = true;
				} else {
					if (modifiedFileFound) {
						commitObject.commitLogs.add(line.trim());
						Commit existingCommit = revisionIDToCommit.put(commitObject.revisionID, commitObject);
						if (null != existingCommit) {
							problematicCommitsFoundInFiles.put(file, commitObject.revisionID);
							// logger.warning("Found duplicate commit: " + file + ", " + commitObject.revisionID);
						}
					}
				}
			}
			
			if (commitObject.revisionID != null) {
				//logger.info(commitObject.toString());
				allCommitObjects.add(commitObject);
			}

		}

		//logger.warning("Size of all commits: " + allCommitObjects.size() + ". Size of unique commits: " + revisionIDToCommit.size());
		//logger.warning(problematicCommitsFoundInFiles.toString());
		
		return allCommitObjects;
	}
	
	private boolean isModifiedFileLine(String line) {
		StringBuilder regex = new StringBuilder() ;
		//   M /ant/core/trunk/WHATSNEW

		regex.append("\\s\\s\\s([ADM]\\s.*?)");
		
		Pattern headerPattern = Pattern.compile(regex.toString());
		Matcher headerMatcher = headerPattern.matcher(line);
		
		return headerMatcher.find();
	}

	private boolean isHeaderLine(String line) {
		StringBuilder regex = new StringBuilder() ;
		regex.append("(r\\d+)\\s\\|"); // 1 revID
		regex.append("\\s(.*?)\\s\\|"); // 2 userID
		regex.append("\\s(.*?)\\s\\|"); // 3 date
		
		Pattern headerPattern = Pattern.compile(regex.toString());
		Matcher headerMatcher = headerPattern.matcher(line);
		
		return headerMatcher.find();		
	}	

}
