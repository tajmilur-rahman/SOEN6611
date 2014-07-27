package execute;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class RegexTests {


	@Test
	public void thisIsNotAFile() {
		String line = "blah/blah/blah blah";
		assertFalse(pathIsFile(line));		
	}

	@Test
	public void thisIsAFile() {
		String line = "blah/blah/blahblah.txt";
		assertTrue(pathIsFile(line));		
	}
	
	@Test
	public void thisIsNotAFileBecauseItsABranch() {
		String line = "blah/branches/something/hello-5.3.2";
		assertFalse(pathIsFile(line));		
	}
	
	@Test
	public void thisIsNotAFileBecauseItsATag() {
		String line = "blah/tags/something/hello-5.3.2";
		assertFalse(pathIsFile(line));		
	}
	
	@Test
	public void thisIsAFileInABranch() {
		String line = "blah/branches/hello/myfile.txt";
		assertTrue(pathIsFile(line));
	}
	
	@Test
	public void thisIsOnlyASlash() {
		String line = "/";
		assertFalse(pathIsFile(line));
	}
	
	
	private boolean pathIsFile(String line) {
		if (line.trim().equals("/")) return false;
		
		Pattern lastPartPattern = Pattern.compile("/([^/]+)\\s*$");
		Matcher lastPartMatcher = lastPartPattern.matcher(line);
		
		if(lastPartMatcher.find()) {
			String lastPart = lastPartMatcher.group(1);
			//System.out.println(lastPart);
			
			if (lastPart.contains(".")) {
				//System.out.println(lastPart + " has " + StringUtils.countMatches(lastPart, ".") + " dot(s)");
				if ( StringUtils.countMatches(lastPart, ".") > 1 && (line.contains("/tags/") || line.contains("/branches/")) ) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
			
		}
		
		return false;
	}

}
