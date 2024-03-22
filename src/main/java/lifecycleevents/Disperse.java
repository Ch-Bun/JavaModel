package lifecycleevents;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import creature.Creature;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import dispersal.EmigrationType;
import dispersal.Settlement;
import dispersal.TransferType;
import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import lifecycle.LifeStageFactory;
import raster.RasterKey;
import utils.LogWriter;
import utils.RandomNumberGenerator;


public class Disperse implements LifeCycleEvent {

	private int currentYear = ModelConfig.START_DATE.getYear()-1;


	//between years emigrants, that can move again (settleOnce=False) are shuffled, assumes over the course of the year individuals can roam
	//freely in a patch. Between dispersal events WITHIN a season emigrants are not shuffled. Individuals can emigrate during any dispersal event.
	//Emigration is classed as movement away from the natal patch. Once emigrated, individuals are life long disperser and can continue to move so 
	//long as settleOnce=False

	@Override
	public void execute(Metapopulation metapopulation, 
			LandscapeRaster landscape, PatchMap patchMap, LocalDate date, boolean balancing) {
		LogWriter.println("Dispersal started");
		//long startTime = System.nanoTime();

		Set<Creature> creatures = getPotentialDispersers(metapopulation);

		if(date.getYear() > currentYear) { //only emigrate once a year

			for(Creature creature : creatures) {

				EmigrationType emigrationType = LifeStageFactory.getLifeStage(creature.getStage(), creature.getSex()).getEmigrationType();

				if(!creature.isDisperser()) { //if not already a disperser then must still be in natal patch, give it the chance to become a disperser this year
					emigrationType.emigrate(creature, landscape, patchMap);
				}
				if(creature.isDisperser()) { 
					//if creature was already a disperser or has recently become one from above step and it's the first
					//dispersal event of the year then shuffle location 
					shuffleInPatch(creature, landscape, patchMap); 
				}

			}
		}

		//give settled dispersers the chance to move again if probability of unsettling > 0
		Set<Creature> settledDispersers = creatures.stream()
				.filter(u-> u.isDisperser())
				.filter(u -> u.isSettled())
				.collect(Collectors.toSet());

		for(Creature creature : settledDispersers) {
			Settlement settlementType = LifeStageFactory.getLifeStage(creature.getStage(), creature.getSex()).getSettlementType();

			if( RandomNumberGenerator.zeroToOne() < settlementType.getProbabilityOfUnsettling())
				creature.unsettle();
		}

		Set<Creature> transferringIndividuals = creatures.stream()
				.filter(u-> u.isDisperser()) //is marked as a disperser in life
				.filter(u -> !u.isSettled()) //is on the move (perhaps again)
				.collect(Collectors.toSet());



		

			for(int step = 0; step < ModelConfig.MAX_STEPS_PER_DISPERSAL_EVENT; step++) {
//LogWriter.println("step " + step + " number of dispersers left " + transferringIndividuals.size());
if(transferringIndividuals.size() < 1) break;
				//only settled creatures from previous (sms) step count towards DD settlement, calculate before
				//this changes
				Map<Patch, Integer> populationCounts = new HashMap<Patch , Integer>();		
				for(Patch patch : patchMap.getPatches()) {
					int popCount = patch.getSettledNt(Sex.MALE) + patch.getSettledNt(Sex.FEMALE); 		
					populationCounts.put(patch, popCount);
				}

				long startTime = System.nanoTime();

				//						for(Creature creature : dispersingIndividuals) {
				//							if(creature.isAlive() && !creature.isSettled()) //still need to check here as individuals can die between steps 
				//								transferType.transfer(creature, landscape, patchMap);	
				//						}

				transferringIndividuals.removeIf(c -> c.isSettled()); //could have settled if step >0

				transferringIndividuals.parallelStream()
				.forEach(c -> {
					LifeStageFactory.getLifeStage(c.getStage(), 
							c.getSex()).getTransferType().transfer(c, landscape, patchMap);
				});

				transferringIndividuals.removeIf(c -> !c.isAlive()); //could have died during transfer


				transferringIndividuals.parallelStream()
				.filter(c -> !c.isHome())
				.filter(c -> c.getCurrentPatch().getId() != ModelConfig.MATRIX_PATCH_ID)
				.forEach(c -> {
					LifeStageFactory.getLifeStage(c.getStage(), c.getSex()).getSettlementType()
					.settle(c, landscape, populationCounts.get(c.getCurrentPatch())); 
				});



				//								for(Creature creature : transferringIndividuals) {
				//								if(!creature.isHome() && creature.getCurrentPatch() !=null) {  //dispersing creature still 'at home' in natal patch or in matrix so cannot settle
				//										int nt = populationCounts.get(creature.getCurrentPatch());
				//										LifeStageFactory.getLifeStage(creature.getStage(), creature.getSex())
				//										.getSettlementType().
				//										settle(creature, landscape, nt); 
				//									}
				//								}



				long endTime = System.nanoTime();

				long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.

				//LogWriter.println("speed " + duration);



			}
		LogWriter.println("Dispersal ended");
	}

	private Set<Creature> getPotentialDispersers(Metapopulation metapopulation){
		Set<Creature> potentialDispersers = new HashSet<Creature>();

		for(Sex sex : Sex.values()) {
			Set<Stage> dispersingStages = LifeStageFactory.getDispersingStages(sex);
			if(dispersingStages != null) {
				for(Stage stage : LifeStageFactory.getDispersingStages(sex)) {

					potentialDispersers.addAll(metapopulation.get(stage).get(sex));

				}
			}
		}
		potentialDispersers.removeIf(c -> !c.isAlive()); //not sure this is really needed, depends on order of mating and dispersal and birth
		return potentialDispersers;
	}


	protected void shuffleInPatch(Creature creature, LandscapeRaster landscape, PatchMap patchMap) {

		Patch patch = creature.getCurrentPatch();
		if(patch.getId() != ModelConfig.MATRIX_PATCH_ID) {
			Set<RasterKey> keys = patchMap.get(patch);
			RasterKey[] arrayKeys = keys.toArray( new RasterKey[ keys.size() ] );
			RasterKey key = arrayKeys[RandomNumberGenerator.getRandomIndex(arrayKeys.length)];

			double newXLocation = landscape.getXCoordin(key) + RandomNumberGenerator.zeroToOne()*ModelConfig.CELLSIZE;
			double newYLocation = landscape.getYCoordin(key) + RandomNumberGenerator.zeroToOne()*ModelConfig.CELLSIZE;

			creature.move(newXLocation, newYLocation);
		} //else in matrix

	}
}


