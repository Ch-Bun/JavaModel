package lifecycle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import creature.Sex;
import creature.Stage;
import dispersal.EmigrationType;
import dispersal.Settlement;
import dispersal.TransferType;
import utils.LogWriter;

public class LifeStageFactory {
	//these maps need to be made immutable 
	private static Map<Stage,Map<Sex, LifeStage>> lifeStages = new HashMap<>();
	private static Map<Sex, Set<Stage>> breedingStages = new HashMap<>();
	private static Map<Sex, Set<Stage>> dispersingStages = new HashMap<>();

	public static void setLifeStage(Sex sex, Stage stage, DemographyType demography, 
			EmigrationType emigration, TransferType transferType, Settlement settlementType) {
		Map<Sex, LifeStage> sexMap = lifeStages.get(stage);

		if (sexMap == null) {
			sexMap = new  HashMap<Sex, LifeStage>();
			lifeStages.put(stage,sexMap);
		}
		LifeStage lifestage = sexMap.get(sex);

		if (lifestage == null) {
			lifestage = new LifeStage(stage, demography, emigration, transferType, settlementType);
			
			sexMap.put(sex, lifestage);

			LogWriter.println(sex + " " + stage.toString() + " loaded");
	
		}
		else {
			LogWriter.printlnError("Entry already exists for stage " + stage + " and sex " + sex);
		}


		if(demography.getFecundity() > 0.0) {
			Set<Stage> breeder = breedingStages.get(sex);
			if (breeder==null) {
				breeder=new HashSet<Stage>();
				breedingStages.put(sex, breeder);
			}
			breeder.add(stage);
		} 
		
		if(demography.disperses()) {
			Set<Stage> disperser = dispersingStages.get(sex);
			if (disperser==null) {
				disperser=new HashSet<Stage>();
				dispersingStages.put(sex, disperser);
			}
			disperser.add(stage);
			
		}
	}
	
	public static void setLifeStage(Sex sex, Stage stage, DemographyType demography) {
		Map<Sex, LifeStage> sexMap = lifeStages.get(stage);

		if (sexMap == null) {
			sexMap = new  HashMap<Sex, LifeStage>();
			lifeStages.put(stage,sexMap);
		}
		LifeStage lifestage = sexMap.get(sex);

		if (lifestage == null) {
			lifestage = new LifeStage(stage, demography);
			
			sexMap.put(sex, lifestage);

			LogWriter.println(sex + " " + stage.toString() + " loaded");			
		}
		else {
			LogWriter.printlnError("Entry already exists for stage " + stage + " and sex " + sex);
		}


		if(demography.getFecundity() > 0.0) {
			Set<Stage> breeder = breedingStages.get(sex);
			if (breeder==null) {
				breeder=new HashSet<Stage>();
				breedingStages.put(sex, breeder);
			}
			breeder.add(stage);
		} 
	}
	
	

	public static LifeStage getLifeStage(Stage stage, Sex sex) {
		Map<Sex, LifeStage> result = lifeStages.get(stage);
		if (result == null) {
			LogWriter.printlnError("No life stage for stage" + stage.getStageName());
		}

		LifeStage sexSpecific = result.get(sex);
		if (sexSpecific == null) {
			LogWriter.printlnError("No life stage for stage " + stage.getStageName() + " and sex " + sex);
		}

		return sexSpecific;
	}


	public static int getNoOfStages(Sex sex) {
		return lifeStages.size();
	}

	public static Set<Stage> getBreedingStages(Sex sex){
		return breedingStages.get(sex);
	}
	
	public static Set<Stage> getDispersingStages(Sex sex){
		return dispersingStages.get(sex);
	}
	
	public static Set<Stage> getAllStages(Sex sex){
		return lifeStages.keySet();
	}

}
