package model;

import java.util.ArrayList;
import java.util.List;

public class CommitFactory {
	String logSnippet;
	String revisionID;
	String userID;
	String commitDate;
	List<String> modifiedFiles;
	String commitLog;
	
	public CommitFactory() {
		modifiedFiles = new ArrayList<>();
	}
	
	public CommitFactory withRevisionID(String revisionID) {
		this.revisionID = revisionID;
		return this;
	}
	
	public CommitFactory withUserID(String userID) {
		this.userID = userID;
		return this;
	}
	
	public CommitFactory withCommitDate(String commitDate) {
		this.commitDate = commitDate;
		return this;
	}
	
	public CommitFactory withModifiedFiles(String mf) {
		for(String file: mf.split("\\r\\n")) {
			modifiedFiles.add(file.trim());
		}
		return this;
	}

	public CommitFactory withCommitLog(String commitLog) {
		this.commitLog = commitLog;
		return this;
	}
	
	public Commit build() {
		return new Commit(revisionID, userID, commitDate, modifiedFiles, commitLog);
	}

}
