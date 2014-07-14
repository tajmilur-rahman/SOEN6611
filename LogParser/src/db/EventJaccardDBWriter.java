package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import model.Author;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class EventJaccardDBWriter {
	static Logger logger = Logger.getLogger(EventJaccardDBWriter.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(EventJaccardDBWriter.class.getPackage().getName()));
	}
	
	String dbPath;
	String dbUser;
	String dbPassword;

	Connection connection = null;
	ResultSet rs = null;
	PreparedStatement ps = null;
	
	public EventJaccardDBWriter() {
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
	
	public void writeOutputTable(Date eventDate, int period) {
		
		// Drop table - only do this once!!! Comment out for future runs
		dropTable();
		
		// Initialize Table (for the first time)
		createTableIfNotExists();
		
		// get authors
		Map<String, Author> authorMinMax = getAuthorsMinMax();
		
		for(Entry<String, Author> authorMinMaxEntry: authorMinMax.entrySet()) {
			writeEventAuthor(eventDate, authorMinMaxEntry, period);
		}
		
		
	}

	private void writeEventAuthor(Date eventDate, Entry<String, Author> authorMinMaxEntry, int period) {
		// from event date to first commit stepping back by period
		int fromFirstToEvent = Days.daysBetween(new DateTime(authorMinMaxEntry.getValue().firstCommit), new DateTime(eventDate)).getDays();
		int fromEventToLast = Days.daysBetween(new DateTime(eventDate), new DateTime(authorMinMaxEntry.getValue().lastCommit)).getDays();
		
		Connection connection = null;
		Statement st = null;
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			st = connection.createStatement();
			connection.setAutoCommit(false);
			
			if (authorMinMaxEntry.getValue().firstCommit.after(eventDate)) {
				System.out.println("[Skipped] " + authorMinMaxEntry.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
			} else {
				int multiplier = -1;
				do {
					System.out.println(authorMinMaxEntry.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Timestamp(eventDate.getTime()));
					cal.add(Calendar.DAY_OF_WEEK, (period * (multiplier + 1)));
					Timestamp current = new Timestamp(cal.getTime().getTime());
					
					cal.setTime(new Timestamp(eventDate.getTime()));
					cal.add(Calendar.DAY_OF_WEEK, (period * multiplier));
					Timestamp previous = new Timestamp(cal.getTime().getTime());
					
					st.addBatch("insert into event_author " +
							"select '" + new Timestamp(eventDate.getTime()) + "', author, '" + (period * multiplier) + "', " +
									"dir, count(*) from committed_files f, svn_commit s	where f.revision_id = s.revision_id and " +
									"commit_date <= '" + current + "' and " +
									"commit_date > '" + previous + "' and " +
									"author = '" + authorMinMaxEntry.getKey() + "' " +
									"group by author, dir");
					
					fromFirstToEvent -= period;
					multiplier -= 1;
				} while (fromFirstToEvent >= 0);
				
				authorMinMaxEntry.getValue().lowerPeriod = period * (multiplier + 1);
			}

			// from event date to last commit stepping forward by period
			if (authorMinMaxEntry.getValue().lastCommit.before(eventDate)) {
				System.out.println("[Skipped] " + authorMinMaxEntry.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);
			} else {
				int multiplier = 1;
				do {
					System.out.println(authorMinMaxEntry.getKey() + ": " + fromFirstToEvent + ", " + fromEventToLast);

					Calendar cal = Calendar.getInstance();
					cal.setTime(new Timestamp(eventDate.getTime()));
					cal.add(Calendar.DAY_OF_WEEK, (period * (multiplier - 1)));
					Timestamp current = new Timestamp(cal.getTime().getTime());
					
					cal.setTime(new Timestamp(eventDate.getTime()));
					cal.add(Calendar.DAY_OF_WEEK, (period * multiplier));
					Timestamp next = new Timestamp(cal.getTime().getTime());
					
					st.addBatch("insert into event_author " +
							"select '" + new Timestamp(eventDate.getTime()) + "', author, '" + (period * multiplier) + "', " +
									"dir, count(*) from committed_files f, svn_commit s	where f.revision_id = s.revision_id and " +
									"commit_date >= '" + current + "' and " +
									"commit_date < '" + next + "' and " +
									"author = '" + authorMinMaxEntry.getKey() + "' " +
									"group by author, dir");					
					
					
					fromEventToLast -= period;
					multiplier += 1;
				} while (fromEventToLast >= 0);
				
				authorMinMaxEntry.getValue().upperPeriod = period * (multiplier - 1);
			}
			
			int counts [] = st.executeBatch();
			System.out.println(counts.length);
			System.out.println("   -----> " + authorMinMaxEntry.getKey() + " " + 
					authorMinMaxEntry.getValue().lowerPeriod + " " + authorMinMaxEntry.getValue().upperPeriod);

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

		if (authorMinMaxEntry.getValue().lowerPeriod != 0 || authorMinMaxEntry.getValue().upperPeriod != 0) {
			writeJaccardSummary(eventDate, authorMinMaxEntry.getKey(), period);	
		}
	}

	private void writeJaccardSummary(Date eventDate, String author, int period) {
		Connection connection = null;
		Statement st = null;
		try {
			connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			st = connection.createStatement();
			connection.setAutoCommit(false);
			
			Map<String, Integer> minMax = getActualMinMax(eventDate, author);
			int min = minMax.get("min");
			int max = minMax.get("max");
			
			if (Math.abs(max - min) >= (period * 2) ) {
				int start = min;
				int incrementor = period;
				while(start < (max - period)) {
					
					// stupid handling due to data organization :(
					if (start == -period) {
						incrementor *= 2;
					} else {
						incrementor = period;
					}
					
					st.addBatch("insert into jaccard_summary " +
							"select '" + new Timestamp(eventDate.getTime()) + "', '" + author + "', '" + start + "', " +
									"(select count(*) from (select dir from event_author where period = '" + start + "' and author = '" + author + "' " + 
									"intersect select dir from event_author where period = '" + (start + incrementor) + "' and author = '" + author + "'" +
									") as r), " +
									"" +
									"(select count(*) from (select dir from event_author where period = '" + start + "' and author = '" + author + "' " + 
									"union select dir from event_author where period = '" + (start + incrementor) + "' and author = '" + author + "'" +
									") as u)");
					
					start += incrementor;
				}
				
				int counts [] = st.executeBatch();
				System.out.println(" --> Executing write to jaccard_summary: " + counts.length);
				connection.commit();
			}
			
			
			
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
	}


	private Map<String, Author> getAuthorsMinMax() {
		Map<String, Author> result = new TreeMap<>();
		
		try {
			Connection connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
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
	
	private Map<String, Integer> getActualMinMax(Date eventDate, String author) {
		Map<String, Integer> result = new HashMap<>();
		
		try {
			Connection connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);
			
			ps = connection.prepareStatement("select min(period), max(period) from event_author where event_date = '" +
					 new Timestamp(eventDate.getTime()) + "' and author = '" + author + "'");
			rs = ps.executeQuery();
			if (rs.next()) {
				result.put("min", rs.getInt(1));
				result.put("max", rs.getInt(2));
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
			
			ps = connection.prepareStatement("drop table if exists jaccard_summary");
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
					"dir text NOT NULL," +
					"commits numeric," +
					"" +
					"primary key(event_date, author, period, dir))");
			ps.executeUpdate();

			ps = connection.prepareStatement("create table if not exists jaccard_summary (" +
					"event_date timestamp with time zone NOT NULL," +
					"author text NOT NULL, " +
					"period numeric NOT NULL," +
					"jac_intersect numeric," +
					"jac_union numeric," +
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
