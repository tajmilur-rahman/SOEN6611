package metrics;

import java.util.Set;

import ast.ClassObject;
import ast.SystemObject;

public class LCOM2 extends AbstractClassMetric {
	
	public LCOM2(SystemObject system) {
		super(system);
	}

	@Override
	public void calculateMetric() {
		
		Set<ClassObject> classes = system.getClassObjects();
		System.out.println(classes.size());
		
		metricValues.put("file 1", "1");
		metricValues.put("file 2", "2.0");
		metricValues.put("file 4", "4.01");
		metricValues.put("file 3", "3.999");
	}
}