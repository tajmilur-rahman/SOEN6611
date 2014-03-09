package model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class VolatilityResult {

	public Multimap<Integer, String> classVolatilityMap;
	
	public VolatilityResult() {
		classVolatilityMap = ArrayListMultimap.create();
	}
	
	
	
}
