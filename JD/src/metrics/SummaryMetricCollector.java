package metrics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SummaryMetricCollector {

	Map<String, SummaryMetric> allMetrics;
	int size;
		
	public SummaryMetricCollector() {
		this.allMetrics = new HashMap<>();
		size = 5;
	}
	
	public void addMetricForClass(String className, String metricName, int volatility, String metricValue) {
		if (allMetrics.containsKey(className)) {
			SummaryMetric sm = allMetrics.get(className);
			sm.addMetric(metricName, metricValue);
 		} else {
  			// How to get the size?
 			SummaryMetric sm = new SummaryMetric(className, volatility, size);
 			sm.addMetric(metricName, metricValue);
 			allMetrics.put(className, sm);
 		}
	}
	
	public void writeMetricsToFiles() throws IOException {
		long start = System.currentTimeMillis();
		
		File summaryMetricFileHigh = new File(AbstractClassMetric.outputPath + AbstractClassMetric.folderName + "/" + this.getClass().getName() + "_1_high.txt");
		File summaryMetricFileMed = new File(AbstractClassMetric.outputPath + AbstractClassMetric.folderName + "/" + this.getClass().getName() + "_2_med.txt");
		File summaryMetricFileLow = new File(AbstractClassMetric.outputPath + AbstractClassMetric.folderName + "/" + this.getClass().getName() + "_3_low.txt");
		boolean runonce = true;
		for(Entry<String, SummaryMetric> e: allMetrics.entrySet()) {
			SummaryMetric summaryMetric = e.getValue();
			if(summaryMetric.isComplete()) {
				if (runonce) {
					System.out.println(summaryMetric.metrics.keySet());
					runonce = false;
				}
				if(summaryMetric.volatility == 3) {
					Files.append(summaryMetric.toString() + "\r\n", summaryMetricFileHigh, Charsets.UTF_8);	
				} else if(summaryMetric.volatility == 2) {
					Files.append(summaryMetric.toString() + "\r\n", summaryMetricFileMed, Charsets.UTF_8);	
				} else if(summaryMetric.volatility == 1) {
					Files.append(summaryMetric.toString() + "\r\n", summaryMetricFileLow, Charsets.UTF_8);	
				} else {
					throw new RuntimeException("How is the volatility not 1, 2, or 3? It's: " + summaryMetric.volatility);
				}
			}
		}
		long end = System.currentTimeMillis();
		long elapsed = end - start;
		System.out.println("Time to write out metrics: " +  elapsed);
	}

	public void setSize(int size) {
		this.size = size;
	}

}
