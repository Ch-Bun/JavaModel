package dispersal;

import creature.Creature;
import genetics.Trait;
import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import raster.RasterKey;
import utils.RandomNumberGenerator;

public class DispersalKernelTransfer extends TransferType {

	DispersalKernelParameters kernelTransferParameters;

	public DispersalKernelTransfer(TransferParameters transferParameters) {
		super(transferParameters);
		kernelTransferParameters = (DispersalKernelParameters) transferParameters;
	}

	@Override
	public void transfer(Creature creature, LandscapeRaster landscape, PatchMap patchMap) {

		
		
		double distanceMoved;
		if(kernelTransferParameters.getEvolving()) {
			double r1 = 0.0000001 + RandomNumberGenerator.zeroToOne()*(1.0-0.0000001);
			distanceMoved= (-1.0*creature.getQTLValueFromGenome(Trait.MEANDISTANCE))*Math.log(r1);
		}else 
			distanceMoved = kernelTransferParameters.sample();


		double dispMortProbability;

		if(kernelTransferParameters.isDistanceDependentMortality())
			dispMortProbability = 1.0 / (1.0 + Math.exp(-(distanceMoved - kernelTransferParameters.getMortalityInflection())*kernelTransferParameters.getMortalitySlope()));  
		else dispMortProbability = kernelTransferParameters.getMortalityRate();

		if(RandomNumberGenerator.zeroToOne() > dispMortProbability) {


			double angle = RandomNumberGenerator.zeroToOne()*2*Math.PI;
			double newX = creature.getXLocation() + distanceMoved * Math.cos(angle);
			double newY = creature.getYLocation() + distanceMoved * Math.sin(angle);

			RasterKey key  = landscape.getKeyFromCoordinates(newX, newY);

			Patch newPatch =patchMap.getPatchForLocation(key);

			creature.move(newX, newY, newPatch);


		}else creature.killCreature();
	}




}
