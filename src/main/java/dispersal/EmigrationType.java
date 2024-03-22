package dispersal;

import java.util.Map;
import java.util.Set;

import creature.Creature;
import creature.Sex;
import genetics.Trait;

import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import raster.RasterKey;
import utils.RandomNumberGenerator;

public abstract class EmigrationType {

	protected final EmigrationParameters emigrationParameters;
//	protected final Map<Trait,TraitParameters> emigrationTraitParameters; //this doesn't differ between stages so can be static
	protected final Boolean isEvolving;
	
	protected EmigrationType(EmigrationParameters emigrationParameters) {
		this.emigrationParameters=emigrationParameters;
		this.isEvolving=emigrationParameters.getEvolving();
	}
	
	public Boolean isEvolving() {
		return isEvolving;
	}

	public void emigrate(Creature creature, LandscapeRaster landscape, PatchMap patchMap) {		

			Patch currentPatch = creature.getCurrentPatch();
			//Set<RasterKey> patchKeys = patchMap.get(currentPatch);
				boolean doesEmigrate = doesEmigrate(creature, currentPatch.getNt(Sex.FEMALE) + currentPatch.getNt(Sex.MALE), currentPatch.getK());
				if(doesEmigrate) { //individual hasn't emigrated yet 
					//double angle = Math.random() * 2 *Math.PI;
					//RasterKey newLocation = findPatchEdge(patchKeys,(int)creature.getXLocation(),(int)creature.getYLocation(), angle);
					//Patch newPatch = patchMap.getPatchForLocation(newLocation);
					//creature.move(newLocation.getCol(), newLocation.getRow(), newPatch); //sets individual at the very edge of the patch 
					creature.emigrate();
				}
	}
		


	private boolean doesEmigrate(Creature creature, int Nt, double b) {

		double dO;
		double alpha;
		double beta;
		double emigrationProbability;
		
		if(isEvolving()) {

			dO = creature.getQTLValueFromGenome(Trait.DO);
			alpha = creature.getQTLValueFromGenome(Trait.EMIGRATIONALPHA);
			beta = creature.getQTLValueFromGenome(Trait.EMIGRATIONBETA);
			
			emigrationProbability = calculateEmigrationProbability(dO, alpha, beta, Nt, b); 
		}
		else {
			emigrationProbability = calculateEmigrationProbability(Nt, b); 
			
		}
		return RandomNumberGenerator.zeroToOne() < emigrationProbability;
	}

	private RasterKey findPatchEdge(Set<RasterKey> patchKeys, int currentXLocation, int currentYLocation,Double angle) {

		RasterKey newLocation = new RasterKey(currentXLocation, currentYLocation);
		int newX = currentXLocation;
		int newY = currentYLocation;
		double amount = ModelConfig.CELLSIZE;
		boolean reverseXDirection=false;
		boolean reverseYDirection=false;

		//if location is in patch or out of bounds then resample 
		while(patchKeys.contains(newLocation) || isOutOfBounds(newLocation)){
			if(newX > ModelConfig.NCOLS || newX < 0) //send individual back in opposite direction, only record steps of successful direction
				reverseXDirection=true;
			if(newY > ModelConfig.NROWS || newY < 0) //send individual back in opposite direction, only record steps of successful direction
				reverseYDirection=true;

			newX = (reverseXDirection) ?  newX - (int)(amount * Math.cos(angle)) : 
				newX + (int)(amount * Math.cos(angle));
			newY = (reverseYDirection) ? newY - (int)(amount * Math.sin(angle)) :
				newY + (int)(amount * Math.sin(angle));

			newLocation.createShifted(newX, newY);
		}

		return newLocation;
	}

	private static boolean isOutOfBounds(RasterKey location) {
		if(location.getCol() >= ModelConfig.NCOLS || location.getCol() < 0 || 
				location.getRow() >= ModelConfig.NROWS || location.getRow() < 0) //send individual back in opposite direction, only record steps of successful direction
			return true;
		return false;
	}

	
	public abstract double calculateEmigrationProbability(int Nt, double b);

	//for genetics
	public abstract double calculateEmigrationProbability(double dO, double alpha, double beta, int Nt, double b);

	
}
