package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ast.Access;
import ast.ClassObject;
import ast.MethodObject;
import ast.SystemObject;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class MHFStandard extends AbstractClassMetric {
	double totalNumberOfPublicMethods = 0;
	double totalNumberOfPrivateMethods = 0;
	double totalNumberOfProtectedMethods = 0;
	double totalNumberOfClasses = 0;
	ListMultimap<ClassObject, String> protectedMethodsPerClass = ArrayListMultimap.create();
	ListMultimap<String, ClassObject> packageToClasses = ArrayListMultimap.create();
	
	public MHFStandard(SystemObject system) {
		super(system);
	}

	@Override
	protected void calculateMetric() {
		Set<ClassObject> classes = system.getClassObjects();
		
		// output is 3 entries into the metricValues
		totalNumberOfClasses = classes.size(); 
		
		for(ClassObject classObject : classes) {
			String packageName = extractPackageName(classObject.getName());
			packageToClasses.put(packageName, classObject);
			
			List<MethodObject> methods = classObject.getMethodList();
			for(MethodObject m: methods) {
				if (m.getAccess().equals(Access.PUBLIC)) {
					totalNumberOfPublicMethods += 1;
					//System.out.println("Adding public: " + m.getClassName() + " " + m.getName());
				} else if (m.getAccess().equals(Access.PRIVATE)) {
					totalNumberOfPrivateMethods += 1;
					//System.out.println("Adding private: " + m.getClassName() + " " + m.getName());
				} else if (m.getAccess().equals(Access.PROTECTED) || m.getAccess().equals(Access.NONE)) {
					// handle protected separately?
					totalNumberOfProtectedMethods += 1;
					//System.out.println("Adding protected: " + m.getClassName() + " " + m.getName());
					protectedMethodsPerClass.put(classObject, m.getName());
				}
			}
		}

		// debug
		System.out.println("# of classes: " + totalNumberOfClasses);
		System.out.println("# of public methods: " + totalNumberOfPublicMethods);
		System.out.println("# of private methods: " + totalNumberOfPrivateMethods);
		System.out.println("# of protected methods: " + totalNumberOfProtectedMethods);
		
		double highMHF = sumOfVisibilities(highClasses) / (totalNumberOfPublicMethods + totalNumberOfPrivateMethods + totalNumberOfProtectedMethods);
		double medMHF = sumOfVisibilities(medClasses) / (totalNumberOfPublicMethods + totalNumberOfPrivateMethods + totalNumberOfProtectedMethods);
		double lowMHF= sumOfVisibilities(lowClasses) / (totalNumberOfPublicMethods + totalNumberOfPrivateMethods + totalNumberOfProtectedMethods);
		
		System.out.println("High MHF " + highMHF);
		System.out.println("Med MHF " + medMHF);
		System.out.println("Low MHF " + lowMHF);
		
		metricValues.put("HV", "" + highMHF);
		metricValues.put("MV", "" + medMHF);
		metricValues.put("LV", "" + lowMHF);

	}

	private String extractPackageName(String className) {
		ArrayList<String> path = new ArrayList<String>(Arrays.asList(className.split("\\.")));
		if (path.size () > 1) {
			path.remove(path.size() - 1);
		} else {
			System.out.println("What?: " + path);
		}

		return Joiner.on(".").join(path);
	}

	private double sumOfVisibilities(List<String> bucket) {
		double runningSumOfVisibilities = 0;
		int count = 0;
		for (ClassObject classObject : protectedMethodsPerClass.keySet()) {
			if (!bucket.contains(classObject.getName())) continue;
			List<String> protectedMethods = protectedMethodsPerClass.get(classObject);
			
			for(String method: protectedMethods) {
				List<ClassObject> classesInPackage = packageToClasses.get(extractPackageName(classObject.getName()));
				if (classesInPackage.size() == 0) {
					throw new IllegalStateException("Why are there 0 classes in this package?");
				}
				
				runningSumOfVisibilities += (1 - ((classesInPackage.size() - 1)) / (totalNumberOfClasses - 1)); 
				// System.out.println("Current running sum of visibilities: " + runningSumOfVisibilities);
				count++;
			}
		}
		
		System.out.println("Computed: " + count);
		return runningSumOfVisibilities + totalNumberOfPrivateMethods;
	}

}
