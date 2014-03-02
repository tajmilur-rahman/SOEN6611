package model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import execute.Runner;

public class Commit {
	public String revisionID;
	public String userID;
	public Date commitDate;
	public List<String> modifiedFiles;
	public List<String> commitLogs;
	public String commitLog;
	public String linesChanged;
	
	public Commit() {
		modifiedFiles = new ArrayList<>();
		commitLogs = new ArrayList<>();
	}
	
	public Commit(String rid, String uid, String cd, List<String> mf, String cl) {
		this.revisionID = rid;
		this.userID = uid;
		
		try {
			this.commitDate = Runner.DATEFORMAT.parse(cd.split("\\(")[0].trim());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		System.out.println(mf);
		this.modifiedFiles = mf;
		this.commitLog = cl;
	}
	
	public Date getCommitDate() {
		return (Date) commitDate.clone();
	}
	
	@Override
	public String toString() {
		return revisionID + " - " + userID + "\n";
	}
}
