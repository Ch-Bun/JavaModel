package environment;

import utils.LogWriter;

public enum EnvironmentalVariable {

	TEMPERATURE("temperature"),
	RAINFALL("rainfall");
	
	private String name;
	
	EnvironmentalVariable(String name) {
		this.name = name;
	}
	
	public static EnvironmentalVariable findByName(String variable){
	    for(EnvironmentalVariable v : values()){
	        if( v.name.equals(variable)){
	            return v;
	        }
	    }
	    
	    LogWriter.printlnError("No environmental variable found for " + variable);
	    return null;
	}
	
	
	
}
