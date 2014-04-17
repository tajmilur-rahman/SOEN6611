package metrics;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ast.ClassObject;
import ast.MethodInvocationObject;
import ast.MethodObject;
import ast.SystemObject;
import ast.decomposition.cfg.AbstractVariable;

public class RFC extends AbstractClassMetric {

	Set<MethodInvocationObject> calledMethods;
	
	public RFC(SystemObject system) {
		super(system);
		calledMethods = new HashSet<>();
	}

	@Override
	protected void calculateMetric() {
		
		Set<ClassObject> classes = system.getClassObjects();
		
		for(ClassObject classObject : classes) {
			int rfc = calculateRFC(classObject);
			metricValues.put(classObject.getName(), "" + rfc);
		}	
		
	}

	private int calculateRFC(ClassObject classObject) {
		// need set of R and set of M. It's the count of all that
		// for each R, have to look at
		
		List<MethodObject> methods = classObject.getMethodList();
		for(MethodObject method: methods) {
			Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> imtf = method.getInvokedMethodsThroughFields();
			Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> imtlv = method.getInvokedMethodsThroughLocalVariables();
			Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> imtp = method.getInvokedMethodsThroughParameters();
			//Set<MethodInvocationObject> imttr = method.getInvokedMethodsThroughThisReference();
			
			processInvokedMethods(imtf, classObject.getName());
			processInvokedMethods(imtlv, classObject.getName());
			processInvokedMethods(imtp, classObject.getName());
		}
		
		// TODO: is this a good idea?
		int rfc = calledMethods.size() + classObject.getNumberOfMethods();
		calledMethods = new HashSet<>();
		return rfc;
	}

	private void processInvokedMethods(
			Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> im, String className) {
		
		for(Entry<AbstractVariable, LinkedHashSet<MethodInvocationObject>> e: im.entrySet()) {
			//System.out.println("Class name: " + className);
			//System.out.println("Variable: " + e.getKey());
			for(MethodInvocationObject m: e.getValue()) {
				
				if (system.getPositionInClassList(m.getOriginClassName()) != -1) {
					if (m.getOriginClassName() != className) {
						calledMethods.add(m);
						//System.out.println("Adding " + m.getOriginClassName() + " " + m.getMethodName() + ". Size: " + calledMethods.size());
					} else {
						//System.out.println("Skipping " + m.getOriginClassName() + " " + m.getMethodName() + ". Size: " + calledMethods.size());
					}
				} else {
					//System.out.println("Skipping " + m.getOriginClassName() + " because it is not our code!");
				}
				
			}			
		}		
	}

}
