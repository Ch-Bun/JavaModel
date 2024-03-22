package creature;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import creature.Butterfly.ButterflyBuilder;
import genetics.Chromosome;
import genetics.Genome;
import land.Patch;

import utils.RandomNumberGenerator;

public class ButterflyFactory implements CreatureFactory, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1954928698779299550L;
	//need to reset this with every replicate 
	private static final AtomicInteger butterflyCounter = new AtomicInteger(0);


		//this should only be called for initialisation, passing in model config parameter of stage to initialise with 
	@Override
	public Butterfly createAdult(LocalDate localDate, Patch patch, Stage stage, int age, double xLocation, double yLocation) {
		Sex sex = (RandomNumberGenerator.getZeroOrOne()== 0) ? Sex.MALE : Sex.FEMALE;
		ButterflyBuilder butterflyBuilder = new Butterfly.ButterflyBuilder(butterflyCounter.incrementAndGet(),stage, age, sex, localDate, patch, xLocation, yLocation);
		butterflyBuilder.maternalId();
		if(!Genome.isEmpty)
			butterflyBuilder.genome();			
		return butterflyBuilder.build();	
	}

	@Override
	public Butterfly createOffspring(Chromosome maternalChromosome, Chromosome paternalChromosome,LocalDate localDate, Patch patch, double xLocation, double yLocation, int maternalId) {
		Sex sex = (RandomNumberGenerator.getZeroOrOne()== 0) ? Sex.MALE : Sex.FEMALE;
		Stage stage = Stage.EGG;
		
		ButterflyBuilder butterflyBuilder = new Butterfly.ButterflyBuilder(butterflyCounter.incrementAndGet(),stage, 0, sex, localDate, patch, xLocation, yLocation);
		butterflyBuilder.maternalId(maternalId);
		butterflyBuilder.genome(maternalChromosome, paternalChromosome);
			
		return butterflyBuilder.build();	
		
	}
	
	public Butterfly createOffspring(LocalDate localDate, Patch patch, double xLocation, double yLocation, int maternalId) {
		Sex sex = (RandomNumberGenerator.getZeroOrOne()== 0) ? Sex.MALE : Sex.FEMALE;
		Stage stage = Stage.EGG;
		
		ButterflyBuilder butterflyBuilder = new Butterfly.ButterflyBuilder(butterflyCounter.incrementAndGet(),stage, 0, sex, localDate, patch, xLocation, yLocation);
		butterflyBuilder.maternalId(maternalId);

		return butterflyBuilder.build();	
		
	}
	
}
