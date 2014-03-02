package execute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Commit;
import model.CommitFactory;

import com.google.common.io.Files;

public class Runner {
	public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.CANADA);

	public static void main(String[] args) throws IOException, ParseException {
		Date rev1_5 = DATEFORMAT.parse("2002-07-10 00:00:00 -0500");	
		Date rev1_6 = DATEFORMAT.parse("2003-12-18 00:00:00 -0500");
		Date rev1_7 = DATEFORMAT.parse("2006-12-19 00:00:00 -0500");
		Date rev1_8 = DATEFORMAT.parse("2010-02-08 00:00:00 -0500");
		Date rev1_9 = DATEFORMAT.parse("2013-03-07 00:00:00 -0500");
		// dates taken from https://ant.apache.org/antnews.html
		
		String antLog = Files.asCharSource(new File("small_history.txt"), Charset.defaultCharset()).read();
		//String antLog = Files.asCharSource(new File("testmassive.txt"), Charset.defaultCharset()).read();
		//String antLog = Files.asCharSource(new File("testlog.txt"), Charset.defaultCharset()).read();
		List<Commit> allCommits = parseCommits(antLog);
		
		List<Commit> from15to16 = new ArrayList<>();
		List<Commit> from16to17 = new ArrayList<>();
		List<Commit> from17to18 = new ArrayList<>();
		List<Commit> from18to19 = new ArrayList<>();
		List<Commit> from19toNow = new ArrayList<>();

		for(Commit c: allCommits) {
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
		
		System.out.println(from15to16.size());
		System.out.println(from16to17.size());
		System.out.println(from17to18.size());
		System.out.println(from18to19.size());
		System.out.println(from19toNow.size());

	}

	private static List<Commit> parseCommits(String antLog) {
		String adjusted = antLog.replaceAll("(?m)^[ \t]*\r?\n", ""); // remove all empty lines
		List<String> splitCommits = Arrays.asList(adjusted.split("------------------------------------------------------------------------"));
		System.out.println("A total of: " + splitCommits.size() + " commits.");
		List<Commit> allCommits = new ArrayList<>();

		// I AM THE VICTIM OF CATASTROPHIC BACKTRACKING. CHANGE THIS CODE, ONLY APPLY REGEX ON A GIVEN LINE
		
		StringBuilder regex = new StringBuilder();
		regex.append("(r\\d+)\\s\\|"); // 1 revID
		regex.append("\\s(.*?)\\s\\|"); // 2 userID
		regex.append("\\s(.*?)\\s\\|"); // 3 date
		regex.append("(.*?)[\\r\\n]+"); // 4 end of string and new lines
		regex.append("(Changed paths:[\\r\\n]+)"); // 5 changed paths
		regex.append("((\\s+(.)\\s(.+)[\\r\\n]+)+)"); // 6, 7, 8, 9 all the modified added or deleted files
		regex.append("((.*\\r\\n)+?)"); // 10 commit log
//		regex.append("------------------------------------------------------------------------");

		Pattern commitPattern = Pattern.compile(regex.toString());

		for(String part: splitCommits) {
			Matcher commitMatcher = commitPattern.matcher(part);
			CommitFactory cf;
			while(commitMatcher.find()) {
				cf = new CommitFactory();
				Commit commit = cf.withRevisionID(commitMatcher.group(1))
						.withUserID(commitMatcher.group(2))
						.withCommitDate(commitMatcher.group(3))
						.withModifiedFiles(commitMatcher.group(6))
						.withCommitLog(commitMatcher.group(10)).build();
				
				allCommits.add(commit);
				System.out.println("Adding: " + commit);
			}			
		}
		
		return allCommits;
	}

}
