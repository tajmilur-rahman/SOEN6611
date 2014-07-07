package execute;

import java.text.ParseException;
import java.util.Date;

import model.EventJaccardCalculator;

public class NLPSExecutor {
	public static void main(String[] args) throws ParseException {
		/* 
		 * Get names for all developers, min and max dates
		 * For each dev, loop over each year and month and calculate union and intersection (store the raw) 
		 * so we can calculate the Jaccard
		 * 
		 * May think about weighted one
		 * 
		 * How to align based on the EVENT DATE, and then +30x and -30x
		 * 
		 * eventID eventDate  dev union intersection
		 * 
		 * event, dev, period (-30, +30 etc.), path
		 * 
		 * select count(*) from (select dir from dev_file_mn where yr = '2012' and mn = '5' and author = 'nirh' 
		 *   union select dir from dev_file_mn where yr = '2012' and mn = '6' and author = 'nirh') as r
		 *  
		 */

		// Input there the date of the event, and the step in days (+/- from that date)
		Date eventDate = NLPSRunner.DATEFORMAT.parse("2014-02-16 16:23:11 -0400");
		int period = 30;

		EventJaccardCalculator ec = new EventJaccardCalculator();
		ec.writeOutputTable(eventDate, period);
	}
	

}
