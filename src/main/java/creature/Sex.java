package creature;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import utils.LogWriter;

public enum Sex  implements Serializable {

	MALE("male"),
	FEMALE("female"),
	BOTH("both");

	private String sexS;

	Sex(String sexS) {
		this.sexS = sexS;
	}

	public String getSex() {
		return sexS;
	}

	private static final Map<String, Sex> lowercaseCache = new HashMap<String, Sex>();
	static {
		for (Sex sex : values()) {
			lowercaseCache.put(sex.getSex(), sex);
		}
	}

	public static Sex getFromCache(String name) {
		Sex s = lowercaseCache.get(name);

		if (s == null)
			LogWriter.printlnError("Can't find sex type for " + name);

		return s;
	}
	@Override
	public String toString() {
		return sexS;
	}
}

