package metrics;

import java.util.List;
import java.util.Set;
import java.io.IOException;

import org.apache.commons.io.*;
import java.io.File;
import ast.ClassObject;
import ast.FieldObject;
import ast.SystemObject;

public class AHF extends AbstractClassMetric {
	
	private List<String> highVolatile = null;
	private List<String> medVolatile = null;
	private List<String> lowVolatile = null;
	
	public AHF(SystemObject system){
		super(system);
		/**
		 * Get the classes from the buckets
		 */
		try{
			highVolatile = FileUtils.readLines(new File(pathToHighFiles), "utf-8");
			medVolatile = FileUtils.readLines(new File(pathToMedFiles), "utf-8");
			lowVolatile = FileUtils.readLines(new File(pathToLowFiles), "utf-8");
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void calculateMetric() {
		
		Set<ClassObject> classes = system.getClassObjects();
		
		float ahfHigh = computeAHF(classes,"high");
		float ahfMed = computeAHF(classes,"medium");
		float ahfLow = computeAHF(classes,"low");
		
		if(ahfHigh >= 0){
			metricValues.put("AHF Heigh: ", "" + ahfHigh);
		}
		if(ahfMed >= 0){
			metricValues.put("AHF Med: ", "" + ahfMed);
		}
		if(ahfLow >= 0){
			metricValues.put("AHF Low: ", "" + ahfLow);
		}
	}
	
	private float computeAHF(Set<ClassObject> classes, String bucketType) {
		
		float ahf = 0;
		
		for(ClassObject co : classes){
			List<FieldObject> fields = co.getFieldList();
			for(FieldObject fieldObject : fields){
				ahf = ahf + (1 - visible(fieldObject, bucketType))/fields.size();
			}
		}
		
		return ahf;
		
	}
	
	private int visible(FieldObject fieldObject, String bucketType) {
		int cac = 0;
		int v = 0;
		
		Set<ClassObject> classes = system.getClassObjects();
		
		if(fieldObject.getAccess().name().equalsIgnoreCase("public")){
			v = 1;
		}else if(fieldObject.getAccess().name().equalsIgnoreCase("private")){
			v = 0;
		}else if(fieldObject.getAccess().name().equalsIgnoreCase("protected") || fieldObject.getAccess().name().isEmpty()){
			try {
				cac = countAccessibleClasses(fieldObject, bucketType);
			} catch (IOException e) {
				e.printStackTrace();
			}
			v = cac / (classes.size()-1);
		}
		
		return v;
		
	}
	
	private int countAccessibleClasses(FieldObject fieldObject, String bucketType) throws IOException{
		
		int firstTimeIn = 0;
		int countClass = 0;
		String curPackageName = "", newPackageName = "";
		
		Class<? extends FieldObject> classObject = fieldObject.getClass();
		String classProType = classObject.toString();
		
		classProType = classProType.substring(0, classProType.indexOf("extends"));
		if(classProType.contains(".")){
			curPackageName = "";
			int pos = classProType.indexOf(".")-1;
			while(classProType.charAt(pos) != ' '){
				curPackageName += classProType.charAt(pos);
				pos--;
			}
		}else{
			curPackageName = "default";
		}
		
		Set<ClassObject> classes = system.getClassObjects();
		
		int i = 0;
		for(ClassObject co : classes){
			//If class does not belong to given bucket then ignore
			if(bucketType.equals("high")){
				if(co.getName() != highVolatile.get(i)){
					continue;
				}
			}else if(bucketType.equals("medium")){
				if(co.getName() != medVolatile.get(i)){
					continue;
				}
			}else if(bucketType.equals("low")){
				if(co.getName() != lowVolatile.get(i)){
					continue;
				}
			}
			
			if(firstTimeIn == 0 ){
				newPackageName = curPackageName;
				firstTimeIn = 1;
			}else{
				classProType = co.toString();
				classProType = classProType.substring(0, classProType.indexOf("extends"));
				if(classProType.contains(".")){
					newPackageName = "";
					int pos = classProType.indexOf(".")-1;
					while(classProType.charAt(pos) != ' '){
						newPackageName += classProType.charAt(pos);
						pos--;
					}
				}else{
					newPackageName = "default";
				}
			}
			
			if(curPackageName.contains(newPackageName)){				
				countClass++;
			}
			i++;
		}
		
		return (countClass-1)/(classes.size()-1);
	}
	
}
