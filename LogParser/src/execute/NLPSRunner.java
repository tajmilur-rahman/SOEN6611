package execute;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import db.CommitDBWriter;

import model.Commit;

/**
 * @author amish_gala
 * Class to initialize the database with the records stored in the SVN commits logs for 
 * each of the investigated projects
 *
 */
public class NLPSRunner {
	static Logger logger = Logger.getLogger(NLPSRunner.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(NLPSRunner.class.getPackage().getName()));
	}
	
	public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.CANADA);

	public static void main(String[] args) throws IOException, ParseException {
		List<String> paths = getPathsOfFiles();
				
		LogProcessor p = new LogProcessor();
		Map<String, List<Commit>> commits = p.process(paths);
		
		// Creating the object drops and recreates the tables
		CommitDBWriter writer = new CommitDBWriter();
		writer.writeAll(commits);
	}

	private static List<String> getPathsOfFiles() {
		List<String> paths = new ArrayList<>();
		paths.add("input/nlps-configuration.txt");
		paths.add("input/nlps-dialog-runtime.txt");
		paths.add("input/nlps-integration-tests.txt");
		paths.add("input/nlps-nlu-bundle.txt");
		paths.add("input/nlps-profiles-cfg.txt");
		paths.add("input/nlps-server.txt");
		paths.add("input/nlps.txt");
		paths.add("input/solr-configuration.txt");
		paths.add("input/solr-stub-dss.txt");
		paths.add("input/stub-dss.txt");
		paths.add("input/udss.txt");
		
		return paths;
	}

}
