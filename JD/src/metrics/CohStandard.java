package metrics;

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
			if (classObject.getMethodList().size() < 2) continue;
			Double cohesion = computeCohesion(classObject);
			if(cohesion != -1) {
				metricValues.put(classObject.getName(), "" + cohesion);
			}
		}			

	}

	private Double computeCohesion(ClassObject classObject) {
		List<MethodObject> methods = classObject.getMethodList();
		List<FieldObject> classAttributes = classObject.getFieldList();
		Map<FieldObject, Integer> numberOfMethodsWhichAccessField = new HashMap<>();

		for(MethodObject m: methods) {
			Set<FieldObject> fieldsAccessedInClass = classObject.getFieldsAccessedInsideMethod(m);
			for(FieldObject f: fieldsAccessedInClass) {
				if (numberOfMethodsWhichAccessField.containsKey(f)) {
					numberOfMethodsWhichAccessField.put(f, numberOfMethodsWhichAccessField.get(f) + 1);
				} else {
					numberOfMethodsWhichAccessField.put(f, 1);
				}
			}
		}
		
		Integer sum = 0;
		for(Entry<FieldObject, Integer> e: numberOfMethodsWhichAccessField.entrySet()) {
			sum += e.getValue();
		}
		
		return (Double.valueOf(sum) / (Double.valueOf(methods.size()) * Double.valueOf(classAttributes.size())));
		
		}
}
