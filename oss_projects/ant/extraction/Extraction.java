import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;

public class Extraction {

	public static void main(String[] args) {
		String[][] Ant_infoS = new String[20000][5];
		String[][] Ant_pathS = new String[70000][4];
		// Variable to hold the one line data
		String line, previousLine="";
		int outer = 0,  outerP = 0,count;
		String concatLogMessage = "",revisionTemp = "",concatMessageTemp = "";
		boolean messageFlag = false, pathFlag = false;
		System.out.println("Reading File from Java code");
		// Name of the file
		// String fileName =
		// "C:\\Users\\Home\\git\\SOEN6611_Taj\\oss_projects\\ant\\ant-core-history.txt";
		String fileName = "E:\\ant-core-history.txt";
		try {

			// Create object of FileReader
			FileReader inputFile = new FileReader(fileName);

			// Instantiate the BufferedReader Class
			BufferedReader bufferReader = new BufferedReader(inputFile);

		
			// Read file line by line and print on the console
			while ((line = bufferReader.readLine()) != null) {
				count=0;
				// Partition the line contains revision id that start with
				// r0..r9
				if (line.startsWith("r1") || line.startsWith("r2")
						|| line.startsWith("r3") || line.startsWith("r4")
						|| line.startsWith("r5") || line.startsWith("r6")
						|| line.startsWith("r7") || line.startsWith("r8")
						|| line.startsWith("r9") || line.startsWith("r0")) {
					String temp = "";
					for (int i = 0; i < line.length(); i++) {
						char d = line.charAt(i);
						if (d == '|') {
							if (count == 2) {
								temp = temp.substring(0, 20);
								Ant_infoS[outer][2] = temp; // extract the
															// commit date
							} else if (count == 0) {
								Ant_infoS[outer][0] = temp; // extract the
															// revision id
								revisionTemp = temp;
							} else if (count == 1)

							{
								Ant_infoS[outer][1] = temp; // extract the user
															// id
							}

							temp = "";
							count++;

						} else {

							temp += d;

						}
						if (count == 3 && (i == line.length() - 1)) {
							//System.out.println("where is this " + temp);
							Ant_infoS[outer][4] = temp; // extract the LOC

						}

					}
					outer++;
				}

				else if ((previousLine.startsWith("   M")
						|| previousLine.startsWith("   A") || previousLine
							.startsWith("   D")) && line.isEmpty()) {
					messageFlag = true;
				}

				if (line.startsWith("--------------")) {
					messageFlag = false;
					Ant_infoS[outer][3] = concatLogMessage; // extract the log
															// message
					concatMessageTemp = concatLogMessage;
					concatLogMessage = "";
				}

				if (messageFlag == true && previousLine.isEmpty() == true) {
					concatLogMessage += line; // Concatenate the log message
												// which is mentioned in more
												// than one line

				}

				if (previousLine.startsWith("Changed paths:")) {

					pathFlag = true;
				}

				if (line.isEmpty() == false && pathFlag == true) {
					// System.out.println("the below are paths " + line);
					Ant_pathS[outerP][3] = line; // extract the path
					if (line.startsWith("   M")) {
						Ant_pathS[outerP][2] = "M"; // extract the value based
													// on change type
					} else if (line.startsWith("   A")) {
						Ant_pathS[outerP][2] = "A";
					} else if (line.startsWith("   D")) {
						Ant_pathS[outerP][2] = "D";
					}
					Ant_pathS[outerP][0] = revisionTemp;
					if (concatMessageTemp.contains("Bugzilla")
							|| concatMessageTemp.contains("Bug")
							|| concatMessageTemp.contains("bug")) {
						concatMessageTemp.matches("[0-9][0-9][0-9].*");
						String str = concatMessageTemp.replaceAll(
								"[^\\.0123456789]", "");
						if (str.isEmpty()) {
							Ant_pathS[outerP][1] = "999"; // the word bug is
															// mentioned but id
															// not found
						} else {
							Ant_pathS[outerP][1] = str; // extract bug id from
														// the log message
						}

					} else {
						Ant_pathS[outerP][1] = "0"; // the word "bug" is not
													// found
					}
					outerP++;
				} else {
					pathFlag = false;
				}

				previousLine = line;
			}

			// Close the buffer reader
			bufferReader.close();
		} catch (Exception e) {
			System.out.println("Error while reading file line by line:"
					+ e.getMessage());
		}

		PrintWriter writerAntInfo, writerAntPath;
		try {
			writerAntInfo = new PrintWriter("F:\\Ant_Info.txt", "UTF-8");
			for (int i = 0; i < Ant_infoS.length; i++) {
				for (int j = 0; j < 5; j++) {

					writerAntInfo.print(Ant_infoS[i][j]);
					if (j != 4) {
						writerAntInfo.print("|");
					}
				}
				writerAntInfo.println();
			}
			writerAntInfo.println();
			writerAntPath = new PrintWriter("F:\\Ant_Path.txt", "UTF-8");
			for (int i = 0; i < Ant_pathS.length; i++) {
				for (int j = 0; j < 4; j++) {
					writerAntPath.print(Ant_pathS[i][j]);
					if (j != 3) {
						writerAntPath.print("|");
					}
				}
				writerAntPath.println();
			}
			writerAntInfo.close();
			writerAntPath.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated
			// catch block
			e.printStackTrace();
		}
	}

}
