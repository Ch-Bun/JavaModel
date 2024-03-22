package dispersal;
import java.util.Set;

import creature.Creature;
import global.ModelConfig;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import raster.RasterKey;
import utils.RandomNumberGenerator;

//marker interface 
public abstract class TransferType {
	protected TransferParameters transferParameters;

	protected TransferType(TransferParameters transferParameters) {
		this.transferParameters=transferParameters;
	}

	public abstract void transfer(Creature creature, LandscapeRaster landscape, PatchMap patchMap);


	public int getMaxStepsPerDispersalEvent() {
		return transferParameters.getMaxStepsPerDispersalEvent();
	}
}
