package metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ast.SystemObject;

public abstract class AbstractClassMetric {
	
	static String folderName = (new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")).format(new Date());
	private static final String outputPath = "C:\\metrics\\";
	
	protected SystemObject system;
	protected Map<String, String> metricValues;
	
	public AbstractClassMetric(SystemObject system) {
		this.system = system;
		metricValues = new TreeMap<>();
	}
	
	// Template method
	public void executeMetric() {
		calculateMetric();
		saveResults();
	}
		
	protected abstract void calculateMetric();
	
	private void saveResults() {
		File f = new File(outputPath + folderName);
		if (!f.isDirectory()) {
			f.mkdirs();			
		}
		
		File metricFile = new File(outputPath + folderName + "\\" + this.getClass().getName() + ".txt");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(metricFile))) {
			for(Entry<String, String> e: metricValues.entrySet()) {
				writer.write(e.getKey() + "," + e.getValue() + "\r\n");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// TODO: Ask Dr. Tsantalis
		//for(Entry<String, String> e: metricValues.entrySet()) {

			// this doesn't work sadly. JD throws this exception:
			// Caused by: java.lang.ClassNotFoundException: com.google.common.io.Files cannot be found by jd_1.0.0.qualifier
			/*			try {
				Files.append(e.getKey() + "," + e.getValue() + "\r\n", metricFile, Charsets.UTF_8);
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
		//}
	}
}
