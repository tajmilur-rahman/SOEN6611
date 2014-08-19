package model;

public class AuthorPeriod {

	public int period;
	public String author;
	public String jiraUser;
	public int directoryCount;
	public int assignedJirasCount;
	public int reportedJirasCount;
	public int numberOfProjectsInJiras;
	
	public AuthorPeriod(String author, String jiraUser, int directoryCount,	int assignedJirasCount, int reportedJirasCount,	int numberOfProjectsInJiras) {
		this.author = author;
		this.jiraUser = jiraUser;
		this.directoryCount = directoryCount;
		this.assignedJirasCount = assignedJirasCount;
		this.reportedJirasCount = reportedJirasCount;
		this.numberOfProjectsInJiras = numberOfProjectsInJiras;
	}

	public int getTotalJiras() {
		return assignedJirasCount + reportedJirasCount;
	}

}
