package execute;

import java.text.ParseException;
import java.util.Date;

import db.EventJaccardDBWriter;


/**
 * @author amish_gala
 * Uses the data after data extraction and data cleanup via SQL script. This code should be 
 * run only after the records have been cleaned. The output is a a table showing the 
 * Jaccard raw values of intersect and union for each of the dev/paths for periods before/after 
 * a given event date.  
 *
 */
public class NLPSExecutor {
	public static void main(String[] args) throws ParseException {
		/* 
		 * Get names for all developers, min and max dates
		 * For each dev, loop over each year and month and calculate union and intersection (store the raw) 
		 * so we can calculate the Jaccard
		 * 
		 * May think about weighted one
		 * 
		 * Calculating the intersect and union is done as per the following (i.e. how to read the jaccard_summary
		 * table). Here is an example row.
		 * 
		 * 
		 * select count(*) from (select dir from dev_file_mn where yr = '2012' and mn = '5' and author = 'nirh' 
		 *   union select dir from dev_file_mn where yr = '2012' and mn = '6' and author = 'nirh') as r
		 *  
		 */

		// Input there the date of the event, and the step in days (+/- from that date)
		Date eventDate = NLPSRunner.DATEFORMAT.parse("2014-02-16 16:23:11 -0400");
		int period = 30;

		EventJaccardDBWriter ec = new EventJaccardDBWriter();
		ec.writeOutputTable(eventDate, period);
	}
	

}
