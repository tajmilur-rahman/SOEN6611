package metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.ClassObject;
import ast.FieldInstructionObject;
import ast.FieldObject;
import ast.MethodObject;
import ast.SystemObject;

public class LCOMHenderson extends AbstractClassMetric {

	public LCOMHenderson(SystemObject system) {
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
		
		
		if(methods.size() < 2) {
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
		else if(classObject.getFieldList().size()==0){
			return methods.size()/(methods.size()-1);
		}
		else{
			return (methods.size()-(a/ classObject.getFieldList().size()))/(methods.size()-1);
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