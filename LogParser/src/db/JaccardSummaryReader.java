package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import model.JaccardSummary;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class JaccardSummaryReader {
	static Logger logger = Logger.getLogger(JaccardSummaryReader.class.getName());
	
	static {
		logger.setParent(Logger.getLogger(JaccardSummaryReader.class.getPackage().getName()));
	}	

	String dbPath;
	String dbUser;
	String dbPassword;

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
	
	public JaccardSummaryReader() {
		loadDBProperties();
	}
	
	public Multimap<String, JaccardSummary> getJaccardSummaryForEventDate(Date eventDate) {
		Multimap<String, JaccardSummary> result = ArrayListMultimap.create();
		
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			Connection connection = DriverManager.getConnection(dbPath, dbUser, dbPassword);

			ps = connection.prepareStatement("select author, period, jac_intersect, jac_union, " +
					"case when jac_union = 0 then 1 " +
					"else jac_intersect / jac_union " +
					"end," +
					"jac_int_weighted, jac_union_weighted," +
					"case when jac_union_weighted = 0 then 1 " +
					"else jac_int_weighted / jac_union_weighted " +
					"end " +
					"from jaccard_summary where event_date = '" +
					 new Timestamp(eventDate.getTime()) + "' order by author, period");

			rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getString(1), new JaccardSummary(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getDouble(5), rs.getInt(6), rs.getInt(7), rs.getDouble(8)));
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
	
}
