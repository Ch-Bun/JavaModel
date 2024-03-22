package creature;

import java.time.LocalDate;
import genetics.Chromosome;
import land.Patch;

public interface CreatureFactory  {
	
	Creature createOffspring(Chromosome maternalChromosome, Chromosome paternalChromosome, LocalDate localDate,
			Patch patch, double xLocation, double yLocation, int maternalId);
	
	Creature createAdult(LocalDate localDate, Patch patch, Stage stage, int age, double xLocation,
			double yLocation);

	Creature createOffspring(LocalDate date, Patch patch, double xLocation, double yLocation, int id);
	
}
