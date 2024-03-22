package land;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import utils.LogWriter;

public class HabitatFactory {

    static Map<Integer, HabitatType> habitatTypes = new HashMap<>();

    public static HabitatType setHabitatType(HabitatType habitatType) {
    	int id = habitatType.getId();
    	HabitatType result = habitatTypes.get(id);
        if (result == null) {
            habitatTypes.put(id, habitatType);
            LogWriter.println(id + " (" + habitatType.getHabitat() + ") " + 
            " K set at " + habitatType.getK());
        }
        return result;
    }

	public static HabitatType getHabitatType(int habType) {
		HabitatType result = habitatTypes.get(habType);
		 if (result == null) {
			 LogWriter.printlnError("No habitat type for code " + habType);
		 }
		return result;
	}
	
	public static boolean checkForHabitatType(int habitatType) {
		return habitatTypes.containsKey(habitatType);
	}

	public static int getNoOfHabitats() {
		return habitatTypes.size();
	}
	
	public static Collection<HabitatType> getHabitats(){
		
		return habitatTypes.values();
	
	}

}
