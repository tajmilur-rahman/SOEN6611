package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ast.ClassObject;
import ast.FieldObject;
import ast.MethodObject;
import ast.SystemObject;

public class CohStandard extends AbstractClassMetric {

	public CohStandard(SystemObject system) {
		super(system);
	}

	@Override
	protected void calculateMetric() {
		Set<ClassObject> classes = system.getClassObjects();
		
		for(ClassObject classObject : classes) {
			if ((classObject.getMethodList().size() < 1) || (classObject.getFieldList().size() < 1)) {
				continue;
			}
			double cohesion = computeCohesion(classObject);
			if (cohesion != -1) { 
				metricValues.put(classObject.getName(), "" + cohesion);
			}
		}			

	}

	private double computeCohesion(ClassObject classObject) {
		List<MethodObject> methods = classObject.getMethodList();
		List<FieldObject> classAttributes = classObject.getFieldList();
		List<FieldObject> nonStaticClassAttributes = new ArrayList<>();
		
		for(FieldObject f: classAttributes) {
			if (!f.isStatic()) {
				nonStaticClassAttributes.add(f);
			}
		}
		
		Map<FieldObject, Integer> numberOfMethodsWhichAccessField = new HashMap<>();
		int numberOfNonStaticMethods = 0;

		for(MethodObject m: methods) {
			if (!m.isStatic()) {
				Set<FieldObject> fieldsAccessedInMethod = classObject.getFieldsAccessedInsideMethod(m);
				for(FieldObject f: fieldsAccessedInMethod) {
					if (!f.isStatic()) {
						if (numberOfMethodsWhichAccessField.containsKey(f)) {
							numberOfMethodsWhichAccessField.put(f, numberOfMethodsWhichAccessField.get(f) + 1);
						} else {
							numberOfMethodsWhichAccessField.put(f, 1);
						}
					}
				}
				numberOfNonStaticMethods++;
			}
		}
		
		Integer sum = 0;
		for(Entry<FieldObject, Integer> e: numberOfMethodsWhichAccessField.entrySet()) {
			sum += e.getValue();
		}
		
		double denominator = (double)(numberOfNonStaticMethods * nonStaticClassAttributes.size());
		if (denominator == 0) {
			return -1;
		} else {
			return (double)(sum) / denominator;		
		}
	}
}
