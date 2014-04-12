package metrics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.*;

import org.eclipse.core.internal.utils.Convert;

import ast.ClassObject;
import ast.FieldInstructionObject;
//import ast.FieldObject;
import ast.MethodObject;
import ast.SystemObject;

public class MHF extends AbstractClassMetric {

	public MHF(SystemObject system) {
		super(system);
	}
	
	@Override
	protected void calculateMetric() {
		
		//Set<ClassObject> classes = system.getClassObjects();
		
		
		float hidingFactor = computeHidingFactor();
		
		System.out.println(hidingFactor);
		metricValues.put("MHF", "" + hidingFactor);
		/**for(ClassObject classObject : classes) {
			float cohesion = computeCohesion(classObject);
			if (cohesion != -1) {
				metricValues.put(classObject.getName(), "" + cohesion);
			} else {
				metricValues.put(classObject.getName(), "Class contains less than 2 methods");
			}
		}	*/		
	}
	
	
	
	private float computeHidingFactor() {
		
		//List<MethodObject> methods = classObject.getMethodList();
		//List<MethodObject> methodsList = classObject.getMethodList();
		
		//float a=0;
		
		
		Set<ClassObject> classes = system.getClassObjects();
		
		String prePackageName="",curPackageName="",classProType="";
		String[][] antPackageDetails = new String[9500][4];
		int firstTimeIn=0,countPublic =0,countPrivate=0,countOther=0,countOtherTemp=0,count=0;
		float sumCalc=0,countClass=0;
		for(ClassObject classObjects : classes){
			List<MethodObject> methodsClass = classObjects.getMethodList();
			classProType= classObjects.toString();
			classProType= classProType.substring(0, classProType.indexOf("extends"));
			
			if(!classProType.contains("interface")){
				if(classProType.contains("."))
				{
					curPackageName="";
					int pos = classProType.lastIndexOf(".")-1;
					while(classProType.charAt(pos) != ' '){
						curPackageName += classProType.charAt(pos);
						pos--;
					}
					curPackageName = new StringBuffer(curPackageName).reverse().toString();
				}
				else
				{
					curPackageName ="default";
				}	
				if(firstTimeIn == 0 ){
				prePackageName = curPackageName;
				firstTimeIn = 1;
				}
				if(curPackageName.equals(prePackageName)== false){				
					if(countOtherTemp!=0)
					{
					sumCalc +=    countOtherTemp - (countOtherTemp * ((countClass-1)/(classes.size()-1)));
					}
					//antPackageDetails[count-1][4] = String.valueOf(countClass); 
					countClass=0;
					countOtherTemp=0;
					prePackageName = curPackageName;
				}
				
				countClass++;
				
				
				for(int i=0;i<methodsClass.size();i++)
				{
					MethodObject mI = methodsClass.get(i);
					//String modifier =mI.toString().substring(0, 3);
					//mI.getAccess().name().equalsIgnoreCase("public");
					
					if(mI.getAccess().name().equalsIgnoreCase("public")){
						countPublic++;
						antPackageDetails[count][1] = classProType.substring((classProType.lastIndexOf(".")+1));
						antPackageDetails[count][0] = curPackageName;
						antPackageDetails[count][2] = mI.getName();
						antPackageDetails[count][3] = "public";
						count++;
					}
					else if(mI.getAccess().name().equalsIgnoreCase("private")){
						countPrivate++;
						antPackageDetails[count][1] = classProType.substring((classProType.lastIndexOf(".")+1));
						antPackageDetails[count][0] = curPackageName;
						antPackageDetails[count][2] = mI.getName();
						antPackageDetails[count][3] = "private";
						count++;
					}
					else{
						countOther++;
						countOtherTemp++;
						if(mI.getAccess().name().equalsIgnoreCase("protected")){
							antPackageDetails[count][1] = classProType.substring((classProType.lastIndexOf(".")+1));
							antPackageDetails[count][0] = curPackageName;
							antPackageDetails[count][2] = mI.getName();
							antPackageDetails[count][3] = "protected";	
							count++;
						} else {
							antPackageDetails[count][1] = classProType.substring((classProType.lastIndexOf(".")+1));
							antPackageDetails[count][0] = curPackageName;
							antPackageDetails[count][2] = mI.getName();
							antPackageDetails[count][3] = "no_modifier";
							count++;
						}
						
					}			
				}
				//count++;
			}	
			}
			
		
		
		if(countOtherTemp!=0)
		{
		sumCalc +=    countOtherTemp - (countOtherTemp * ((countClass-1)/(classes.size()-1)));
		}
				
		//sumCalc = (sumCalc + countPrivate) / (countPublic+countPrivate +countOther);
		
		sumCalc = (sumCalc + countPrivate) / (countPublic+countPrivate+countOther);
		
		//Arrays.sort(antPackageDetails);
		
		PrintWriter writerAntPackageDetails;
		try {
			writerAntPackageDetails = new PrintWriter("F:\\antPackageDetails.txt", "UTF-8");
			for (int i = 0; i < antPackageDetails.length; i++) {
				for (int j = 0; j < 4; j++) {

					writerAntPackageDetails.print(antPackageDetails[i][j]);
					if (j != 3) {
						writerAntPackageDetails.print("|");
					}
				}
				writerAntPackageDetails.println();
			}
			writerAntPackageDetails.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated
			// catch block
			e.printStackTrace();
		}
		
		return sumCalc;
	}
}