package FileReaders;

import java.util.Map;

import creature.Sex;
import creature.Stage;

public interface DispersalFileReader<T> {
	
	abstract Map<Stage, Map<Sex, T>> readDispersalFile(String filename);

}
