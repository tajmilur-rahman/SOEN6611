package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ast.Access;
import ast.ClassObject;
import ast.FieldObject;
import ast.MethodObject;
import ast.SystemObject;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class AHFStandard extends AbstractClassMetric {
	double totalNumberOfPublicFields = 0;
	double totalNumberOfPrivateFields = 0;
	double totalNumberOfProtectedFields = 0;
	double totalNumberOfClasses = 0;
	ListMultimap<ClassObject, String> protectedFieldsPerClass = ArrayListMultimap.create();
	ListMultimap<String, ClassObject> packageToClasses = ArrayListMultimap.create();
	
	public AHFStandard(SystemObject system) {
		super(system);
	}

	@Override
	protected void calculateMetric() {
		Set<ClassObject> classes = system.getClassObjects();
		
		// output is 3 entries into the metricValues
		totalNumberOfClasses = 0; //classes.size(); 
		
		for(ClassObject classObject : classes) {
			if (classObject.isInterface()) continue;
			totalNumberOfClasses += 1;
			String packageName = extractPackageName(classObject.getName());
			packageToClasses.put(packageName, classObject);
			
			List<FieldObject> fields = classObject.getFieldList();
			for(FieldObject f: fields) {
				if (f.getAccess().equals(Access.PUBLIC)) {
					totalNumberOfPublicFields += 1;
					//System.out.println("Adding public: " + f.getClassName() + " " + f.getName());
				} else if (f.getAccess().equals(Access.PRIVATE)) {
					totalNumberOfPrivateFields += 1;
					//System.out.println("Adding private: " + f.getClassName() + " " + f.getName());
				} else if (f.getAccess().equals(Access.PROTECTED) || f.getAccess().equals(Access.NONE)) {
					// handle protected separately?
					totalNumberOfProtectedFields += 1;
					//System.out.println("Adding protected: " + f.getClassName() + " " + f.getName());
					protectedFieldsPerClass.put(classObject, f.getName());
				}
			}
		}

		// debug
		System.out.println("# of classes: " + totalNumberOfClasses);
		System.out.println("# of public fields: " + totalNumberOfPublicFields);
		System.out.println("# of private fields: " + totalNumberOfPrivateFields);
		System.out.println("# of protected fields: " + totalNumberOfProtectedFields);
		
		double highAHF = sumOfVisibilities(highClasses) / (totalNumberOfPublicFields + totalNumberOfPrivateFields + totalNumberOfProtectedFields);
		double medAHF = sumOfVisibilities(medClasses) / (totalNumberOfPublicFields + totalNumberOfPrivateFields + totalNumberOfProtectedFields);
		double lowAHF= sumOfVisibilities(lowClasses) / (totalNumberOfPublicFields + totalNumberOfPrivateFields + totalNumberOfProtectedFields);
		
		System.out.println("High AHF " + highAHF);
		System.out.println("Med AHF " + medAHF);
		System.out.println("Low AHF " + lowAHF);
		
		metricValues.put("HV", "" + highAHF);
		metricValues.put("MV", "" + medAHF);
		metricValues.put("LV", "" + lowAHF);

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
		for (ClassObject classObject : protectedFieldsPerClass.keySet()) {
			if (!bucket.contains(classObject.getName())) continue;
			List<String> protectedFields = protectedFieldsPerClass.get(classObject);
			
			for(String field: protectedFields) {
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
		return runningSumOfVisibilities + totalNumberOfPrivateFields;
	}

}
