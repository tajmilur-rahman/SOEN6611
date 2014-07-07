package model;

import java.sql.Timestamp;
import java.util.Date;

public class Author {
	String name;
	Date firstCommit;
	Date lastCommit;

	public Author(String name, Timestamp firstCommit, Timestamp lastCommit) {
		this.name = name;
		this.firstCommit = firstCommit;
		this.lastCommit = lastCommit;
	}
	
	@Override
	public String toString() {
		return name + ", min: " + firstCommit + ", max: " + lastCommit;
	}

}
