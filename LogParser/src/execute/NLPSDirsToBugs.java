package execute;

import db.DirectoryToJiraDBWriter;

public class NLPSDirsToBugs {

	/* Class to work with the jiras2 and user_jira_mapper tables and to 
	 * create an ouput like the following
	 * 
	 *   period  author  directory_count  assigned_jira  reported_jira  sum_jira  num_projects_in_jiras
	 *   -300    bob     4                2              3              5         2 (e.g. Genie, Nile)
	 *   -300    jim     2                1              0              1         1
	 * 
	 * 
	 */
	public static void main(String[] args) {
		DirectoryToJiraDBWriter dj = new DirectoryToJiraDBWriter();
		
		int period = 7; // bi-weekly
		dj.writeOutputTable(period);

	}

}
