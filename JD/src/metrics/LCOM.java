package metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.ClassObject;
import ast.FieldInstructionObject;
import ast.MethodObject;
import ast.SystemObject;

public class LCOM extends AbstractClassMetric {

	public LCOM(SystemObject system) {
		super(system);
	}
	
	@Override
	protected void calculateMetric() {
		
		Set<ClassObject> classes = system.getClassObjects();
		
		for(ClassObject classObject : classes) {
			int cohesion = computeCohesion(classObject);
			if(cohesion != -1) {
				metricValues.put(classObject.getName(), "" + cohesion);
			} else {
				// metricValues.put(classObject.getName(), "Class contains less than 2 methods");
			}
		}			
	}
	
	private int computeCohesion(ClassObject classObject) {
		
		List<MethodObject> methods = classObject.getMethodList();
		int p = 0;
		int q = 0;
		
		if(methods.size() < 2) {
			return -1;
		}
		
		for(int i=0; i<methods.size()-1; i++) {
			MethodObject mI = methods.get(i);
			List<FieldInstructionObject> attributesI = mI.getFieldInstructions();
			for(int j=i+1; j<methods.size(); j++) {
				MethodObject mJ = methods.get(j);
				List<FieldInstructionObject> attributesJ = mJ.getFieldInstructions();
				
				Set<FieldInstructionObject> intersection = commonAttributes(attributesI, attributesJ, classObject.getName());
				if(intersection.isEmpty()) {
					p++;
				} else {
					q++;
				}
				
			}
		}
		
		if (p > q) {
			return p - q;
		} else {
			return 0;
		}
	}
	
	private Set<FieldInstructionObject> commonAttributes(List<FieldInstructionObject> attributesI,
			List<FieldInstructionObject> attributesJ, String className) {
		
		Set<FieldInstructionObject> commonAttributes = new HashSet<FieldInstructionObject>();
		for (FieldInstructionObject instructionI : attributesI) {
			if(instructionI.getOwnerClass().equals(className) && attributesJ.contains(instructionI)) {
				commonAttributes.add(instructionI);
			}
		}
		return commonAttributes;
		
	}

}