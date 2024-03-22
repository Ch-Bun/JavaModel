package lifecycleevents;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import creature.Creature;
import creature.CreatureFactory;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import global.ModelConfig;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import lifecycle.LifeStageFactory;
import raster.RasterKey;
import utils.RandomNumberGenerator;

public class Initialise implements LifeCycleEvent {

	@Override
	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap, LocalDate date, boolean balancing) {

		CreatureFactory creatureFactory = metapopulation.getFactory(); 
		//Initial population is always adult and stage, must make sure INITIALUSERPOPULATIONSTAGEID matches 'adult'
		//currently initialised at K 
		//sex is assigned randomly with binomial probability 

		Map<Patch, Set<RasterKey>> patchesToInitialise = patchMap.getInitialPatches();

		for(Entry<Patch, Set<RasterKey>> entry : patchesToInitialise.entrySet()){
			Patch patch = entry.getKey();
			if(patch.getId() != ModelConfig.MATRIX_PATCH_ID) {
			Set<RasterKey> keys = entry.getValue();
			RasterKey[] arrayKeys = keys.toArray( new RasterKey[ keys.size() ] );

			double maxK = patch.getK()*ModelConfig.INITIALISE_K_PROPORTION;

			List<Double> kValues = ModelConfig.INITIAL_STAGE_PROPORTIONS.stream().map(e -> e*maxK).collect(Collectors.toList());
			int stageIndex=0;
			for(double kStage : kValues) {

				Stage stage =Stage.getFromCache(stageIndex);
				int minAge = LifeStageFactory.getLifeStage(stage, Sex.MALE).getDemography().getMinAge();
				//could add max age here and have age randomly chosen

				for(int k=0; k< (int) kStage ;k++){
					RasterKey key = arrayKeys[RandomNumberGenerator.getRandomIndex(arrayKeys.length)];
					//randomise position in cell/patch
					double xLocation = landscape.getXCoordin(key) + RandomNumberGenerator.zeroToOne()*ModelConfig.CELLSIZE;
					double yLocation = landscape.getYCoordin(key) + RandomNumberGenerator.zeroToOne()*ModelConfig.CELLSIZE;
					Creature creature = creatureFactory.createAdult(date,patch,stage,minAge, xLocation,yLocation);
					patch.addCreature(creature);
					metapopulation.addCreature(creature);
				}
				stageIndex++;	
			}
		}
		}
	}

	//	@Override
	/*	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap, LocalDate date) {

		CreatureFactory creatureFactory = metapopulation.getFactory(); 
		//Initial population is always adult and stage, must make sure INITIALUSERPOPULATIONSTAGEID matches 'adult'
		//currently initialised at K 
		//sex is assigned randomly with binomial probability 
		Stage stage = new Stage(ModelConfig.INITIALPOPULATIONSTAGEID,"adult");
		int age = ModelConfig.INITIALPOPULATIONAGE;

		for(Entry<Patch, Set<RasterKey>> entry : patchMap.entrySet()){
			Patch patch = entry.getKey();
			Set<RasterKey> keys = entry.getValue();
			RasterSet<Cell> cells = landscape.createSubsetForKeys(keys);

			for(Entry<RasterKey,Cell> landscapeEntry : cells.entrySet()){
				Cell cell =  landscapeEntry.getValue();
				int carryingCapacity = cell.getK();

				for(int i=0; i < carryingCapacity; i++)
				{
					Creature creature = creatureFactory.createAdult(date,patch,stage,age,cell);
					cell.addCreature(creature);
					metapopulation.addCreature(creature);
				}
			}
			patch.setNt(Sex.FEMALE);
			patch.setNt(Sex.MALE);
		}
	}*/


}
