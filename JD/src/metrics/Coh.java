package metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.ClassObject;
import ast.FieldInstructionObject;
import ast.MethodObject;
import ast.SystemObject;

public class Coh extends AbstractClassMetric {
	public Coh(SystemObject system){
		super(system);
	}
	
	@Override
	protected void calculateMetric() {
		
		Set<ClassObject> classes = system.getClassObjects();
		
		for(ClassObject classObject : classes) {
			float cohesion = computeCohesion(classObject);
			if(cohesion != -1) {
				metricValues.put(classObject.getName(), "" + cohesion);
			}
		}			
	}
	
	private float computeCohesion(ClassObject classObject) {
		
		List<MethodObject> methods = classObject.getMethodList();
		
		float a=0;
		
		if(methods.size() < 1 || classObject.getFieldList().size() < 1) {
			return -1;
		}
		
		for(int i=0; i<methods.size(); i++) {
			MethodObject mI = methods.get(i);
			List<FieldInstructionObject> attributesI = mI.getFieldInstructions();	
				Set<FieldInstructionObject> attributeAccess = instanceAttributes(attributesI,  classObject.getName());
				if(attributeAccess.isEmpty()) {
				} else {
					a=a+attributeAccess.size();
				}
		}
		
		if(methods.size()==0){
			return -1;
		}
		
		if(a==0){
			return 0;
		}
		else{
			return ( a / ( classObject.getFieldList().size() * methods.size() ) );
		}
	}

	private Set<FieldInstructionObject> instanceAttributes(List<FieldInstructionObject> attributesI,
			 String className) {
		
		Set<FieldInstructionObject> instanceAttributes = new HashSet<FieldInstructionObject>();
		for (FieldInstructionObject instructionI : attributesI) {
			if(instructionI.getOwnerClass().equals(className) && attributesI.contains(instructionI)) {
				instanceAttributes.add(instructionI);
			}
		}
		return instanceAttributes;		
	}
}
