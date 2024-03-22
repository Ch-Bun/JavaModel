package lifecycleevents;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import creature.Creature;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import lifecycle.LifeStage;
import lifecycle.LifeStageFactory;
import utils.LogWriter;
import utils.RandomNumberGenerator;


public class Age implements LifeCycleEvent {

	@Override
	public void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap,
			LocalDate date, boolean balancing) {
		// TODO Auto-generated method stub
		LogWriter.println("Starting aging ");
		Set<Creature> allCreatures= metapopulation.setView();
		Map<Patch, Integer> populationCounts = new HashMap<Patch , Integer>();

		for(Patch patch : patchMap.getPatches()) {
			int popCount = patch.getNt(Sex.MALE) + patch.getNt(Sex.FEMALE); //transient individuals count towards DD
			populationCounts.put(patch, popCount);
		}


		for(Creature creature : allCreatures) {
			if(creature.isAlive()) {
				int ageNow = creature.getAge();
				Sex sex = creature.getSex();
				Patch currentPatch =creature.getCurrentPatch();
				Stage stageNow = creature.getStage();
				LifeStage currentLifeHistory = LifeStageFactory.getLifeStage(stageNow, sex);
				//see nemoAge for explanation
		
				int Nt = populationCounts.get(currentPatch);
				double K = currentPatch.getK();// all inds should be in a patch even if it's matrix 
				
				//note if K = 0 here (for overlapping generations), e.g. artificial landscape matrix then survival probability could be higher in matrix than in patches
				//because exp(0)=	1 -> survivalProb * 1 = survivalProb 

				double survivalProbability = (ModelConfig.DENSITY_DEPENDENT_SURVIVAL) ? currentLifeHistory.getDemography().getSurvival()
						*Math.exp(-(Nt)/K) : currentLifeHistory.getDemography().getSurvival();
				double	transitionProbability = (ModelConfig.DENSITY_DEPENDENT_DEVELOPMENT) ? currentLifeHistory.getDemography().getTransition()
						*Math.exp(-(Nt)/K) : currentLifeHistory.getDemography().getTransition();
				Double sigma = survivalProbability + transitionProbability;

				Double lambda = transitionProbability/sigma;

				Double doesSurvive = (ageNow+1 > currentLifeHistory.getDemography().getMaxAge()) ? transitionProbability : sigma;

				if(RandomNumberGenerator.zeroToOne() < doesSurvive) {
					int ageNew =creature.age(); //age increments by 1 year 
					Double doesTransition = (ageNew > currentLifeHistory.getDemography().getMaxAge()) ? 1.0 : lambda;

					if(RandomNumberGenerator.zeroToOne() < doesTransition) {

						Stage stageNew = Stage.getNextStage(stageNow);

						if(!metapopulation.removeCreature(creature))
							LogWriter.printlnError("Trying to remove creature " + creature.getId() + " age, " + ageNow + " " + stageNow.toString() + "from "
									+ "metapopulation container when it doesn't exist in there");

						creature.age(stageNew);
						metapopulation.addCreature(creature);
						//LogWriter.println("Creature " + creature.getId() + " age " + creature.getAge() + " is in patch " + creature.getCurrentPatch().getId());
					}
				}
				else { //individual dies
					if(!metapopulation.removeCreature(creature))
						LogWriter.printlnError("Trying to remove creature " + creature.getId() + " age, " + ageNow + " " + stageNow.toString() + "from "
								+ "metapopulation container when it doesn't exist in there");
					creature.remove();
				}
			}
			else
			{
				if(!metapopulation.removeCreature(creature))
					LogWriter.printlnError("Trying to remove creature " + creature.getId()  + "from "
							+ "metapopulation container when it doesn't exist in there");

				creature.remove();
			}
		}


		//rather than lots of removes etc, set alive = false and then do one big clean up of inds

		LogWriter.println("Finished aging");
	}




}
