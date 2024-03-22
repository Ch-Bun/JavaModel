package lifecycleevents;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import creature.Metapopulation;
import global.ModelConfig;
import land.LandscapeRaster;
import land.PatchMap;
import utils.LogWriter;

public class ReproductiveSeason implements LifeCycleEvent {

	private Map<String, LifeCycleEvent> reproductiveSeason = new HashMap<String,LifeCycleEvent>();

	private int timeTakenToMate = 0;
	private int gestationLength = 0;

 
	@Override
	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap,
			LocalDate date, boolean balancing) {
		
		LocalDate endOfSeason = ModelConfig.END_DATE_REP_SEASON.atYear(date.getYear());

		while (date.isBefore(endOfSeason)) {
			for(LifeCycleEvent event : reproductiveSeason.values()) {
				event.execute(metapopulation, landscape, patchMap, date, balancing);
			}
			date = date.plusDays(Math.max(1,timeTakenToMate+gestationLength));
		}
	}

	public void add (LifeCycleEvent event, String eventS) {
		
		String eventName=event.getClass().getName();

		try {
		if(reproductiveSeason.containsKey(eventName)) {
			throw new IOException("More than one " + eventS + " for reproductive cycle");
		}
		else {
			reproductiveSeason.put(eventName,event);
			LogWriter.println("Adding " + eventS + " to reproduction season ");
			if(event instanceof Mate) {
				timeTakenToMate = ModelConfig.TIMETAKENTOMATE;
			}
			if(event instanceof Birth) {
				gestationLength = ModelConfig.DAYSOFGESTATION;
			}
		}
		}
		catch(IOException e) {
			LogWriter.printlnError("Uh-oh there's been a problem setting up the reproductive season.");
			LogWriter.print(e);
		}
	}

}
