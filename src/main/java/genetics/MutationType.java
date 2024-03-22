package genetics;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import utils.LogWriter;


//basically a marker class for a string to hold the name of a mutation type 
public class MutationType {

	private final String mutationType;
	private static Map<String,MutationType> mutationTypes;
	
	
	public static MutationType newInstance(String name) {
		if(mutationTypes==null) 
			mutationTypes = new HashMap<String,MutationType>();
		
		MutationType mutationType = mutationTypes.get(name);
		
		if(mutationType == null) {
			mutationType = new MutationType(name);
			mutationTypes.put(name, mutationType);
		}
	
		return mutationType;
	}
	
	public MutationType(String mutationType) {
		this.mutationType=mutationType;
	}
	
	public static MutationType getMutationTypeForString(String name) {
		MutationType mutationType = mutationTypes.get(name);
		
		if(mutationType == null)
			LogWriter.printlnError("No mutation type for " + name);
		return mutationType;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mutationType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutationType other = (MutationType) obj;
		return Objects.equals(mutationType, other.mutationType);
	}

}
