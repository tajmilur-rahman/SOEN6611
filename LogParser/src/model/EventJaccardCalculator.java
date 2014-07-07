package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class EventJaccardCalculator {
	static Logger logger = Logger.getLogger(EventJaccardCalculator.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(EventJaccardCalculator.class.getPackage().getName()));
	}
	
	String dbPath;
	String dbUser;
	String dbPassword;

	Connection connection = null;
	ResultSet rs = null;
	PreparedStatement ps = null;
	
	public EventJaccardCalculator() {
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
	
	public void writeOutputTable(Date eventDate, int period) {
		
		// Drop table - only do this once!!! Comment out for future runs
		dropTable();
		
		// Initialize Table (for the first time)
		createTableIfNotExists();
		
		// get authors
		Map<String, Author> authorMinMax = getAuthorsMinMax();
		
		for(Entry<String, Author> e: authorMinMax.entrySet()) {
			writeEventAuthor(eventDate, e, period);
		}
		
		
	}

	private void writeEventAuthor(Date eventDate, Entry<String, Author> e, int period) {
		// Fill in table first, then perhaps we can calculate after
		connection = null;
		Statement st = null;
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			st = connection.createStatement();
			connection.setAutoCommit(false);
			
			// from event date to first commit stepping back by period
			int fromFirstToEvent = Days.daysBetween(new DateTime(e.getValue().firstCommit), new DateTime(eventDate)).getDays();
			int fromEventToLast = Days.daysBetween(new DateTime(eventDate), new DateTime(e.getValue().lastCommit)).getDays();
			
			if (e.getValue().firstCommit.after(eventDate)) {
				System.out.println("[Skipped] " + e.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
			} else {
				int multiplier = -1;
				do {
					System.out.println(e.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
					st.addBatch("insert into event_author(event_date, author, period) values ('" + 
							new Timestamp(eventDate.getTime()) + "', '" + e.getKey() + "','" + (period * multiplier) + "')");
					fromFirstToEvent -= period;
					multiplier -= 1;
				} while (fromFirstToEvent >= 0);
			}

			if (e.getValue().lastCommit.before(eventDate)) {
				System.out.println("[Skipped] " + e.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
			} else {
				int multiplier = 1;
				do {
					System.out.println(e.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
					st.addBatch("insert into event_author(event_date, author, period) values ('" + 
							new Timestamp(eventDate.getTime()) + "', '" + e.getKey() + "','" + (period * multiplier) + "')");
					fromEventToLast -= period;
					multiplier += 1;
				} while (fromEventToLast >= 0);
			}
			
			int counts [] = st.executeBatch();
		
			System.out.println(counts.length);
			connection.commit();

		} catch (SQLException ex) {
			System.out.println("Insert failed!");
			ex.printStackTrace();
		} finally {
			try {
				st.close();
			} catch (SQLException ex2) {
				ex2.printStackTrace();
			}			
		}
		
		
	}

	private Map<String, Author> getAuthorsMinMax() {
		Map<String, Author> result = new TreeMap<>();
		
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			ps = connection.prepareStatement("select author, min(commit_date), max(commit_date) from svn_commit group by author");
			rs = ps.executeQuery();
			while (rs.next()) {
				Author author = new Author(rs.getString(1), rs.getTimestamp(2), rs.getTimestamp(3));
				result.put(rs.getString(1), author);
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

	private void dropTable() {
		Connection connection = null;
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			PreparedStatement ps = connection.prepareStatement("drop table if exists event_author");
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
		 
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			ps = connection.prepareStatement("create table if not exists event_author (" +
					"event_date timestamp with time zone NOT NULL," +
					"author text NOT NULL, " +
					"period numeric NOT NULL," +
					"jac_num numeric," +
					"jac_den numeric," +
					"" +
					"primary key(event_date, author, period))");
			ps.executeUpdate();
			
			ps.close();

		} catch (SQLException e) {
			System.out.println("Create event_author failed!");
			e.printStackTrace();
			return;
		}
		
	}

}
