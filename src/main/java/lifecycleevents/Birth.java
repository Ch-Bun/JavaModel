package lifecycleevents;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import creature.ButterflyFactory;
import creature.Creature;
import creature.CreatureFactory;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import genetics.Genome;
import global.ModelConfig;

import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import lifecycle.LifeStageFactory;
import raster.RasterKey;
import utils.LogWriter;
import utils.RandomNumberGenerator;

public class Birth implements LifeCycleEvent{

	CreatureFactory factory = new ButterflyFactory();


	@Override
	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap, LocalDate date, boolean balancing) {
		//this method ignores females that are in the matrix, they can deposit eggs/offspring after next dispersal event, if this is only chance then eggs are cleared when aging 
		LogWriter.println("starting generation of new offspring, year" + date.getYear());

		for(Entry<Patch, Set<RasterKey>> entry : patchMap.entrySet()){
			
			Patch patch = entry.getKey();
			if(patch.getId() != ModelConfig.MATRIX_PATCH_ID) {

			int patchNt = patch.getSettledNt(Sex.MALE) + patch.getSettledNt(Sex.FEMALE); //only settled individuals contribute to DD
			double patchK = patch.getK();

			Set<Creature> fertilisedFemales = new HashSet<Creature>();

			Set<Stage> femaleBreedingStages=LifeStageFactory.getBreedingStages(Sex.FEMALE);

			for(Stage stage : femaleBreedingStages) {
				fertilisedFemales.addAll(patch.getStage(Sex.FEMALE, stage).stream()
						.filter(u -> u.getMates() != null)
						.filter(u -> u.getMates().size() > 0 )
						.collect(Collectors.toSet()));
			}
			for(Creature female : fertilisedFemales) {

				List<Creature> fathers=female.getMates();

				double maxFecundity = LifeStageFactory.getLifeStage(female.getStage(), Sex.FEMALE).getDemography().getFecundity();
				double fecundity  = (ModelConfig.DENSITY_DEPENDENT_FECUNDITY) ? maxFecundity*Math.exp(-(patchNt)/patchK) : maxFecundity;
                                // mortality due to disturbances are here included as reduced fecundity
                                if(patch.toBeDisturbed && !balancing && (patch.undisturbedGenerations == ModelConfig.GENERAIONS_BETWEEN_DISTURBANCES))
                                    fecundity *= (1 - ModelConfig.FRACTION_KILLED_BY_DISTURBANCE);
				int numberOfBirths = RandomNumberGenerator.samplePoisson(fecundity);

				for(int birth=0; birth < numberOfBirths; birth++) {
					double xLocation = female.getXLocation();
					double yLocation = female.getYLocation();
					Creature mate = fathers.get(RandomNumberGenerator.getRandomIndex(fathers.size()));
					Creature egg = (!Genome.isEmpty) ? 
							factory.createOffspring(female.inheritance(),mate.inheritance(), date,patch,xLocation, yLocation, female.getId()) :
								factory.createOffspring(date,patch,xLocation, yLocation, female.getId());
					patch.addCreature(egg);	
					metapopulation.addCreature(egg);
				}
			}
		}
		}
		LogWriter.println("finished generation of new offspring");
	}
		
}
