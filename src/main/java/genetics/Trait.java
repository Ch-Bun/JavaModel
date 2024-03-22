package genetics;

import java.util.HashMap;
import java.util.Map;

import utils.LogWriter;

public enum Trait {

	EMIGRATIONALPHA("alpha"), //EMIGRATION, SETTLMENT
	EMIGRATIONBETA("beta"),  //EMIGRATION, SETTLEMENT
	DO("dO"), //EMIGRATION
	STEPLENGTH("stepLength"), //CRW
	STEPCORRELATION("stepCorrelation"), //CRW
	MEANDISTANCE("distance"), //KERNEL
	SO("sO"),
	SETTLEMENTALPHA("alphaS"),
	SETTLEMENTBETA("betaS"),
	DP("directionalPersistence"); //SETTLEMENT
	
	private String name;
	
	private Trait(String name) {
		this.name=name;
	}
	
	private String getName() {
		return name;
	}
	
	private static final Map<String, Trait> nameCache = new HashMap<String, Trait>();

    static {
        for (Trait t : values()) {
        	nameCache.put(t.getName(), t);

       }
    }
    
	private static Trait getFromCache(Map<String, Trait> cache, String name) {
		Trait type = cache.get(name);
		
		if (type == null)
			LogWriter.printlnError("Can't find Item for " + name + " check spelling of traits in files");
		
		return type;
	}

	public static Trait getForName(String name) {
		Trait trait = getFromCache(nameCache, name);
		if (trait == null) {
			throw new RuntimeException("Can't find trait for name: " + name);
		}
		return trait;
	}
}
