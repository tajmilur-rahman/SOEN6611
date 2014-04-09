package metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ast.SystemObject;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public abstract class AbstractClassMetric {
		
	// for windows 
	public static final String pathToHighFiles = "C:/Users/Amish_Gala/workspace_oss/SOEN6611_Taj/JD/volatility/1_high_volatility.txt";
	public static final String pathToMedFiles = "C:/Users/Amish_Gala/workspace_oss/SOEN6611_Taj/JD/volatility/2_med_volatility.txt";
	public static final String pathToLowFiles = "C:/Users/Amish_Gala/workspace_oss/SOEN6611_Taj/JD/volatility/3_low_volatility.txt";
	
	/** for linux
	private static String pathToHighFiles = "/home/rupak/Documents/git/SOEN6611_Taj/JD/volatility/1_high_volatility.txt";
	private static String pathToMedFiles = "/home/rupak/Documents/git/SOEN6611_Taj/JD/volatility/2_med_volatility.txt";
	private static String pathToLowFiles = "/home/rupak/Documents/git/SOEN6611_Taj/JD/volatility/3_low_volatility.txt"; */
	
	private List<String> highClasses;
	private List<String> medClasses;
	private List<String> lowClasses;	
	
	public static final String folderName = (new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")).format(new Date());
	public static final String outputPath = "C:\\metrics\\"; // for windows
	//public static final String outputPath = "/home/rupak/Documents/"; // for linux
	
	protected SystemObject system;
	protected Map<String, String> metricValues;
	SummaryMetricCollector smc;
	
	public AbstractClassMetric(SystemObject system) {
		this.system = system;
		metricValues = new TreeMap<>();
		loadVolatilityLists();
	}
	
	public void setSummaryMetricCollector(SummaryMetricCollector smc) {
		this.smc = smc;
	}
	
	private void loadVolatilityLists() {
		try {
			String high = Files.asCharSource(new File(pathToHighFiles), Charset.defaultCharset()).read();
			String med 	= Files.asCharSource(new File(pathToMedFiles), Charset.defaultCharset()).read();
			String low 	= Files.asCharSource(new File(pathToLowFiles), Charset.defaultCharset()).read();
			
			highClasses = Arrays.asList(high.split("\n"));
			medClasses 	= Arrays.asList(med.split("\n"));			
			lowClasses 	= Arrays.asList(low.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Template method
	public void executeMetric() {
		calculateMetric();
		saveResults();
	}
		
	protected abstract void calculateMetric();
	
	private void saveResults() {
		/* Write out 4 files per metric:
			1) The overall results from JD - for ALL files
			2) The 1_high_volatility results
			3) The 2_med_volatility results
			4) The 3_low_volatility results 
		
		*/
		File f = new File(outputPath + folderName);
		if (!f.isDirectory()) {
			f.mkdirs();			
		}
		
		File metricFile = new File(outputPath + folderName + "/" + this.getClass().getName() + ".txt");
		File encodedMetricFile = new File(outputPath + folderName + "/" + this.getClass().getName() + "_encoded.txt");
		File metricFileHigh = new File(outputPath + folderName + "/" + this.getClass().getName() + "_1_high.txt");
		File metricFileMed = new File(outputPath + folderName + "/" + this.getClass().getName() + "_2_med.txt");
		File metricFileLow = new File(outputPath + folderName + "/" + this.getClass().getName() + "_3_low.txt");
		
		// This code works now because we added "plugin.guava" to JD's plugin.xml.
		String className;
		for(Entry<String, String> e: metricValues.entrySet()) {
			try {
				className = e.getKey();
				Files.append(className + "," + e.getValue() + "\r\n", metricFile, Charsets.UTF_8);
				
				if(highClasses.contains(className)) {
					Files.append("3, " + e.getValue() + "\r\n", encodedMetricFile, Charsets.UTF_8);	
					//Files.append("3, " + e.getValue() + "\r\n", metricFileHigh, Charsets.UTF_8);
					smc.addMetricForClass(className, this.getClass().getName(), 3, e.getValue());
				} else if(medClasses.contains(className)) {
					Files.append("2, " + e.getValue() + "\r\n", encodedMetricFile, Charsets.UTF_8);	
					//Files.append("2, " + e.getValue() + "\r\n", metricFileMed, Charsets.UTF_8);
					smc.addMetricForClass(className, this.getClass().getName(), 2, e.getValue());
				} else if(lowClasses.contains(className)) {
					Files.append("1, " + e.getValue() + "\r\n", encodedMetricFile, Charsets.UTF_8);	
					//Files.append("1, " + e.getValue() + "\r\n", metricFileLow, Charsets.UTF_8);
					smc.addMetricForClass(className, this.getClass().getName(), 1, e.getValue());
				} else {
					//System.out.println("Class not in HV, MV, or LV: " + className);
				}	
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
