package metrics;

import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Joiner;

public class SummaryMetric {

	String className;
	int volatility;
	int size;
	Map<String, String> metrics;
	
	public SummaryMetric(String name, int volatility, int size) {
		this.className = name;
		this.volatility = volatility;
		this.size = size;
		
		// Use a treemap to preserve ordering
		this.metrics = new TreeMap<>();
	}
	
	public void addMetric(String metricName, String value) {
		if (metrics.containsKey(metricName)) { 
			throw new IllegalStateException("Added the same metric more than once: " + metricName);
		}
		metrics.put(metricName, value);
	}
	
	public boolean isComplete() {
		return metrics.size() == size;
	}
	
	@Override
	public String toString() {
		Joiner joiner = Joiner.on(",");
		return className + "," + joiner.join(metrics.values());
	}
	
}
