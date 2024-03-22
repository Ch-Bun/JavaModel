package lifecycleevents;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import creature.Creature;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import lifecycle.LifeStageFactory;
import mating.MatingSystem;
import mating.MatingSystemFactory;
import raster.RasterKey;
import utils.LogWriter;
import java.util.stream.Collectors;

//this loops through the patches as opposed to the metapopulation as assumption is mating only happens within patches 
//in cell based landscape this wouldn't make a difference 
public class Mate implements LifeCycleEvent {

	MatingSystem matingSystem;

	public Mate() {
		this.matingSystem = MatingSystemFactory.getMatingSystem();
	}

	@Override
	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap, LocalDate date, boolean balancing) {
		LogWriter.println("Mating started");
		//mating only happens in patches therefore we iterate through patch containers not
		//metapopulation, means females in metapopulation are ignored for mating 
		for(Entry<Patch, Set<RasterKey>> entry : patchMap.entrySet()){
			Patch patch = entry.getKey();
			if(patch.getId() != ModelConfig.MATRIX_PATCH_ID ) {
				patch.resetProportionOfMatedFemales();
				List<Creature> femalePool = new ArrayList<Creature>();
				List<Creature> malePool = new ArrayList<Creature>();

				Set<Stage> femaleBreedingStages=LifeStageFactory.getBreedingStages(Sex.FEMALE);

				for(Stage stage : femaleBreedingStages) {
					femalePool.addAll(patch.getStage(Sex.FEMALE, stage));
				}

				Set<Stage> maleBreedingStages=LifeStageFactory.getBreedingStages(Sex.MALE);

				for(Stage stage : maleBreedingStages) {
					malePool.addAll(patch.getStage(Sex.MALE, stage));
				}
				matingSystem.mating(femalePool, malePool);

				if(femalePool.size() > 0) {
					double numberOfMatedFemales  = (femalePool.stream()
							.filter(c -> c.getMates() !=null)
							.filter(c -> c.getMates().size() > 0 )
							.collect(Collectors.toList()).size());

					double proportionOfMatedFemales =  numberOfMatedFemales / femalePool.size();

					patch.setProportionOfMatedFemales(proportionOfMatedFemales);
				}
			}
		}
		LogWriter.println("Mating ended");
	}
}
