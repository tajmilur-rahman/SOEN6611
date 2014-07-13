package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import model.Commit;

public class CommitDBWriter {
	static Logger logger = Logger.getLogger(CommitDBWriter.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(CommitDBWriter.class.getPackage().getName()));
	}
	
	private final String dbPath = "jdbc:postgresql://127.0.0.1:5432/SOEN6951";
	private final String dbUser = "postgres";
	private final String dbPassword = "testit";
	
	public CommitDBWriter() {
		checkConnection();
		dropTables();
		initializeTables();
	}

	
	public void writeAll(Map<String, List<Commit>> commitsPerProject) {

		Connection connection = null;
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			
			PreparedStatement ps = null;
			
			for(Entry<String, List<Commit>> e: commitsPerProject.entrySet()) {
				// is this risky?
				String project = e.getKey().split("/")[1].split("\\.")[0];
				logger.info("[INFO] Beginning storing records for project " + project);
				
				for (Commit c: e.getValue()) {
					String revisionID = c.revisionID;
					String author = c.userID;
					long timestamp = c.commitDate.getTime();
					String subject = c.commitLogs.toString().replace("'", "\"");
					
					ps = connection.prepareStatement("insert into svn_commit (revision_ID, author, commit_date, subject, log, project)" +
							"values('" + revisionID + "','" + author + "','" + new Timestamp(timestamp)  + "','" + subject + "','" + subject + "','" + project + "');");
				    //logger.warning(ps.toString());
					ps.executeUpdate();
					
					for (Entry<String, String> commitTypeAndPath: c.commitLogsByType.entries()) {
						String modifyType = commitTypeAndPath.getKey();
						String path = commitTypeAndPath.getValue().replace("'", "\"");
	
						ps = connection.prepareStatement("insert into committed_files (revision_ID, modify_type, path, project)" +
								"values('" + revisionID + "','" + modifyType + "','" + path  + "','" + project + "');");
						//logger.warning(ps.toString());
						ps.executeUpdate();
					} 
				}
			}
			
			ps.close();

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}	
		
		
	}
	

	private void dropTables() {
		Connection connection = null;
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			PreparedStatement ps = connection.prepareStatement("drop table if exists svn_commit");
			ps.executeUpdate();
			
			ps = connection.prepareStatement("drop table if exists committed_files");
			ps.executeUpdate();
			
			ps.close();

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}		
	}



	private void initializeTables() {
		Connection connection = null;
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			PreparedStatement ps = connection.prepareStatement("create table svn_commit (" +
					"revision_ID text NOT NULL, " +
					"author text NOT NULL," +
					"commit_date timestamp with time zone NOT NULL," +
					"subject text default ''," +
					"log text default ''," +
					"project text NOT NULL," +
					"" +
					"primary key(revision_ID, project))");
			ps.executeUpdate();

			ps = connection.prepareStatement("create table committed_files (" +
					"revision_ID text NOT NULL, " +
					"modify_type character(1) NOT NULL," +
					"path text NOT NULL," +
					"project text NOT NULL," +
					"" +
					"primary key(revision_ID, modify_type, project, path))");
			ps.executeUpdate();
			
			ps.close();

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		

		
	}



	private void checkConnection() {
		System.out.println("-------- PostgreSQL JDBC Connection Testing ------------");
		 
		try { 
			Class.forName("org.postgresql.Driver"); 
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? Include in your library path!");
			e.printStackTrace();
			return; 
		}
 
		System.out.println("PostgreSQL JDBC Driver Registered!");
		Connection connection = null;
 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
	}




}
