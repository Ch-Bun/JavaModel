package dispersal;

import java.util.Map;

import creature.Creature;
import creature.Sex;
import genetics.Trait;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import utils.LogWriter;
import utils.RandomNumberGenerator;

public class  Settlement {
	protected SettlementParameters settlementParameters;

	public Settlement(SettlementParameters settlementParameters) {
		this.settlementParameters=settlementParameters;
	}

	public void settle(Creature creature, LandscapeRaster landscape, int patchResidentPopSize) {


		double probabilityOfSettling;
		Sex oppositeSex = (creature.getSex().equals(Sex.MALE)) ? Sex.FEMALE : Sex.MALE;

		Patch patch = creature.getCurrentPatch();

		double sO = settlementParameters.isEvolving() ? creature.getQTLValueFromGenome(Trait.SO) : settlementParameters.getsO();

		if(patch != null && !creature.isHome()) { //new cell is suitable and not natal, double check

			if(settlementParameters.isDensDep()) {
				double beta = settlementParameters.isEvolving() ? creature.getQTLValueFromGenome(Trait.SETTLEMENTBETA) : settlementParameters.getBeta();
				double alpha = settlementParameters.isEvolving() ? creature.getQTLValueFromGenome(Trait.SETTLEMENTALPHA) : settlementParameters.getAlpha();

				probabilityOfSettling = sO/ 
						1 + Math.exp(-(settlementParameters.getDensityBValue()*patchResidentPopSize - beta*alpha));
			}
			else probabilityOfSettling = sO;

			if(settlementParameters.isFindMate() ) { //find mate can include transient individuals
				if (patch.getFecundNt(oppositeSex) > 0){
					if(probabilityOfSettling > RandomNumberGenerator.zeroToOne())
						creature.settle();
				}
			}else {
				if(probabilityOfSettling > RandomNumberGenerator.zeroToOne())
					creature.settle();
			}
		}else
			LogWriter.printlnError("Creature " + creature.getId() + " trying to settle in matrix or natal patch");
	}

	public double getProbabilityOfUnsettling() {
		return settlementParameters.getProbabilityOfUnsettling();
	}

}
