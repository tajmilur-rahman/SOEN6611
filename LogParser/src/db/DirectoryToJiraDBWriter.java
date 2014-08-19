package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Logger;

import model.Author;

public class DirectoryToJiraDBWriter {
	static Logger logger = Logger.getLogger(DirectoryToJiraDBWriter.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(DirectoryToJiraDBWriter.class.getPackage().getName()));
	}
	
	String dbPath;
	String dbUser;
	String dbPassword;

	Connection connection = null;
	ResultSet rs = null;
	PreparedStatement ps = null;
	
	String dirToUse;
	
	public DirectoryToJiraDBWriter() {
		loadDBProperties();
	}

	private void loadDBProperties() {
		Properties p = new Properties();
		try (FileInputStream input = new FileInputStream("src/config.properties")) {
			p.load(input);
			dbPath = p.getProperty("db.path");
			dbUser = p.getProperty("db.user");
			dbPassword = p.getProperty("db.password");
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
	}	
	
	public void writeOutputTable(int period) {
		dropTable();
		createTableIfNotExists();
		
		Date firstCommit = getFirstCommitDate();
		Date lastCommit = getLastCommitDate();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(firstCommit.getTime()));
		Timestamp current = new Timestamp(cal.getTime().getTime());
				
		cal.add(Calendar.DAY_OF_WEEK, period);
		Timestamp next = new Timestamp(cal.getTime().getTime());

		int periodToStore = 0;

		Connection connection = null;
		Statement st = null;
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			st = connection.createStatement();
			connection.setAutoCommit(false);
			
			do {
				st.addBatch("insert into period_author_dir " +
						"select '" + periodToStore + "', s.author, dir_2_file, count(dir_2_file) " +
						"from svn_commit s, committed_files f where s.revision_id = f.revision_id " +
						"and commit_date >= '" + current + "' " +
						"and commit_date < '" + next + "' " +
						"group by s.author, dir_2_file");

				String query = "insert into period_author_jiras " +
						"select '" + periodToStore + "', u.author, " +
								"(select count(*) from user_jira_mapper v, jiras2 w where v.jira_user = w.assignee and resolved >= '" + current + "' and resolved < '" + next + "' and v.author = u.author), " +
								"(select count(*) from user_jira_mapper x, jiras2 z where x.jira_user = z.reporter and resolved >= '" + current + "' and resolved < '" + next + "' and x.author = u.author), " +
								"count(*)," +
								"sum(num_apps) " +
						"from user_jira_mapper u, jiras2 j where (u.jira_user = j.assignee or u.jira_user = j.reporter) " +
						"and resolved >= '" + current + "' " +
						"and resolved < '" + next + "' " +
						"group by u.author";

				System.out.println(query);
				
				st.addBatch("insert into period_author_jiras " +
						"select '" + periodToStore + "', u.author, " +
								"(select count(*) from user_jira_mapper v, jiras2 w where v.jira_user = w.assignee and resolved >= '" + current + "' and resolved < '" + next + "' and v.author = u.author), " +
								"(select count(*) from user_jira_mapper x, jiras2 z where x.jira_user = z.reporter and resolved >= '" + current + "' and resolved < '" + next + "' and x.author = u.author), " +
								"count(*), " +
								"sum(num_apps) " +
						"from user_jira_mapper u, jiras2 j where (u.jira_user = j.assignee or u.jira_user = j.reporter) " +
						"and resolved >= '" + current + "' " +
						"and resolved < '" + next + "' " +
						"group by u.author");
				
				current = increment(current, period);
				next = increment(next, period);
				periodToStore += period;
				
			} while (current.before(lastCommit));

			int counts [] = st.executeBatch();
			connection.commit();

		} catch (SQLException ex) {
			System.out.println("Insert failed!");
			ex.printStackTrace();
			ex.getNextException().printStackTrace();
		} finally {
			try {
				st.close();
			} catch (SQLException ex2) {
				ex2.printStackTrace();
			}			
		}
		
		System.out.println("Finished!");
		
		writeDirectoryJiraTable(period);
		
	}
	
	private void writeDirectoryJiraTable(int period) {
		Connection connection = null;
		Statement st = null;
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			st = connection.createStatement();
			connection.setAutoCommit(false);
			
			st.addBatch("insert into directory_jira " +
					"select period, author, coalesce(count, 0), coalesce(sum, 0), coalesce(assigned_jiras, 0), coalesce(reported_jiras, 0), coalesce(sum_jiras, 0), coalesce(num_apps, 0) " +
					"from (select period, author, count(directory), sum(count) from period_author_dir group by period, author order by author, period) p " +
						"full join period_author_jiras d using(period, author) order by author, period;");

			int counts [] = st.executeBatch();
			connection.commit();

		} catch (SQLException ex) {
			System.out.println("Insert failed!");
			ex.printStackTrace();
			ex.getNextException().printStackTrace();
		} finally {
			try {
				st.close();
			} catch (SQLException ex2) {
				ex2.printStackTrace();
			}			
		}
		
		System.out.println("Finished!");
		
	}

	private Timestamp increment(Timestamp ts, int period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(ts.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, period);
		return new Timestamp(cal.getTime().getTime());
	}

	private Date getFirstCommitDate() {
		return getCommitDate("min");
	}
	
	private Date getLastCommitDate() {
		return getCommitDate("max");
	}
	
	private Date getCommitDate(String extreme) {
		Date result = null;

		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			Connection connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);

			ps = connection.prepareStatement("select " + extreme + "(commit_date) from svn_commit");

			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getDate(1);
			}
			ps.close();

		} catch (SQLException e) {
			System.out.println("Something went wrong");
			e.printStackTrace();

		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}		
		
		return result;
		
	}

	private void writeDirectoryJiraForAuthor(Entry<String, Author> authorMinMaxEntry, int period) {

	}

	private void dropTable() {
		Connection connection = null;
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			PreparedStatement ps = connection.prepareStatement("drop table if exists directory_jira");
			ps.executeUpdate();

			ps = connection.prepareStatement("drop table if exists period_author_dir");
			ps.executeUpdate();			

			ps = connection.prepareStatement("drop table if exists period_author_jiras");
			ps.executeUpdate();		
			
			ps.close();
			
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}		
	}
	
	private void createTableIfNotExists() {
		connection = null;
		
		//period  author  directory_count  assigned_jira  reported_jira  sum_jira  num_projects_in_jiras
		
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);

			ps = connection.prepareStatement("create table if not exists period_author_jiras (" +
					"period numeric NOT NULL," +
					"author text NOT NULL, " +
					"assigned_jiras numeric NOT NULL," +
					"reported_jiras numeric NOT NULL," +
					"sum_jiras numeric NOT NULL," +
					"num_apps numeric NOT NULL," +
					"" +
					"primary key(period, author))");
			ps.executeUpdate();			
			
			ps = connection.prepareStatement("create table if not exists period_author_dir (" +
					"period numeric NOT NULL," +
					"author text NOT NULL, " +
					"directory text NOT NULL," +
					"count numeric," +
					"" +
					"primary key(period, author, directory))");
			ps.executeUpdate();
			
			ps = connection.prepareStatement("create table if not exists directory_jira (" +
					"period numeric NOT NULL," +
					"author text NOT NULL, " +
					"directory_count numeric, " +
					"files_count numeric, " +
					"assigned_jira numeric, " +
					"reported_jira numeric, " +
					"sum_jira numeric, " +
					"num_projects_in_jiras numeric, " +
					"" +
					"primary key(period, author))");
			ps.executeUpdate();
			
			ps.close();

		} catch (SQLException e) {
			System.out.println("Create directory_jira failed!");
			e.printStackTrace();
			System.exit(1);
		}
		
	}

	
}
