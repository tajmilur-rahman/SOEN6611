package metrics;

import ast.SystemObject;

public class RFC extends AbstractClassMetric {

	public RFC(SystemObject system) {
		super(system);
	}

	@Override
	protected void calculateMetric() {
		metricValues.put("rfc_1", "1");
		metricValues.put("rfc_2", "2");
		metricValues.put("rfc_3", "3");
	}

}
