package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lifecycleevents.LifeCycleEvent;
import lifecycleevents.ReproductiveSeason;
import utils.AbstractFileReader;
import utils.LogWriter;

public class LifecycleReader extends AbstractFileReader {

	private int eventColumnNo;
	private int repSeasonColumnNo;

	public LifecycleReader() {
		super(2);
		// OrderOfEvents,InReproductiveSeason
	}

	public void readLifeCycleFile(String filename, List<LifeCycleEvent> lifecycle) {

		LogWriter.println("reading life cycle file: " + filename);

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));
			Set<String> events = new HashSet<String>();

			ReproductiveSeason reproduction = new ReproductiveSeason();

			try {
				eventColumnNo = colNameMap.get("Event");
				repSeasonColumnNo=colNameMap.get("InReproductiveSeason");
			} catch(Exception e) {
				throw new IOException("Life cycle file, error in column names. Hint: Check column spelling and lower/upper case");
			}


			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					throw new IOException("Too few columns in tabular file " + filename + " line " + line);
				}

				String pack = "lifecycleevents.";
				String event = tokens[eventColumnNo];
				String eventAd= pack + event;
				Boolean inRepSeason = Boolean.parseBoolean(tokens[repSeasonColumnNo]);

				String age = "Age";

				if(event.equals(age) && inRepSeason) {
					throw new IOException("Aging process cannot take place within reproductive season");
				}
				
				
				LifeCycleEvent instance = null;
				String className = null;
				
				try {

					Class<?> clazz = Class.forName(eventAd);
					Constructor<?> constructor = clazz.getConstructor();
					instance = (LifeCycleEvent) constructor.newInstance();
					className = clazz.getName();
					}catch(Exception e) {
						throw new IOException("Problem generating life cycle event. "
								+ "Hint: check spelling/case of " + event + " event in file. Options"
										+ "are: Mate, Disperse, Birth, Age");
					}

				if(!inRepSeason) {
					if( events.contains(className)) {
						throw new IOException("More than one " + event + " for life cycle event ");
					}
					else {
						lifecycle.add(instance);
						events.add(className);
						LogWriter.println("Adding " + event + " to life cycle ");
					}
				}
				else {
					if(!lifecycle.stream().anyMatch(c -> c instanceof ReproductiveSeason)) {
						lifecycle.add(reproduction);
						LogWriter.println("Adding reproduction season to life cycle ");
					}
					reproduction.add(instance, event);
				}
			}
		}
		catch (Exception e) {
			LogWriter.printlnError("Uh-oh, failed reading file " + filename);
			LogWriter.print(e);
		}

	}
}
