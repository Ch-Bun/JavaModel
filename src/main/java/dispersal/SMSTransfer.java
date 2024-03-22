package dispersal;

import java.util.Set;

import creature.Creature;
import genetics.MutationType;
import genetics.Trait;
import global.ModelConfig;
import land.Cell;
import land.LandscapeRaster;
import land.Patch;
import land.PatchMap;
import raster.RasterKey;
import utils.LogWriter;
import utils.RandomNumberGenerator;


public class SMSTransfer extends TransferType {

	public SMSTransfer(SMSTransferParameters smsTransferParameters) {
		super(smsTransferParameters);
	}

	@Override
	public void transfer(Creature creature, LandscapeRaster landscape,PatchMap patchMap) {

		SMSTransferParameters parameters = (SMSTransferParameters) this.transferParameters;

		double directionalPersistence = (transferParameters.evolving) ? creature.getQTLValueFromGenome(Trait.DP) : parameters.getDp();

		if(creature.isSettled())
			LogWriter.printlnError("Settled creature " + creature.getId() + " is trying to move");

		//in a patch from last timestep and didn't settle so get out
		if(creature.getCurrentPatch() != null)	
			directionalPersistence *= directionalPersistence*10;
		//in matrix, keep DP inflated until far from previous patch
		else {  
			if(creature.getStepsFromNearestPatch() <= (ModelConfig.PERCEPTUALRANGE+1)) {
				directionalPersistence *= directionalPersistence*10;

			}


		}
		double currentX = creature.getXLocation();
		double currentY = creature.getYLocation();
		Patch currentPatch = creature.getCurrentPatch();

			LogWriter.println("creature " + creature.getId() + " x " + currentX + " y " + currentY + " patch " + creature.getCurrentPatch());

		Cell cell = landscape.getFromCoordinates(currentX, currentY);

		double[][] habitatWeights = cell.getCrossingProbabilities();
		double[][] dpWeights = calcDPWeightings(directionalPersistence, creature.getDirectionOfTravel());
		double[][]nbrCells = calcProbabilityMatrix(dpWeights, habitatWeights);

		double[] newLocation = selectNewLocation(nbrCells, creature);
		// select direction at random based on cell selection probabilities
		// landscape boundaries and no-data cells are reflective

		double newX = newLocation[0];
		double newY = newLocation[1];

		RasterKey newKey =landscape.getKeyFromCoordinates(newX, newY);
		Patch newPatch = patchMap.getPatchForLocation(newKey);

		if(RandomNumberGenerator.zeroToOne() < parameters.getStepMortality()) {
			creature.killCreature();
		LogWriter.println("creature " + creature.getId() + " dies during transfer");
		}
		else {
			double theta = Math.atan2((newX-currentX),(newY-currentY));
			creature.setDirectionOfTravel(theta);
			creature.move(newX, newY, newPatch);
		

		if(newPatch.getId() == ModelConfig.MATRIX_PATCH_ID) { //stepped into matrix or still in matrix
			creature.incrementSteps();
		}
		else {
			//still in same patch as before so do nothing to step count
			if(!newPatch.equals(currentPatch)) //moved into a new patch, either from matrix or old patch
				creature.resetStepCount();

		}
		LogWriter.println("creature " + creature.getId() + " new x " + newX + " new y " + newY + " patch " + newPatch
				+ " step count " + creature.getStepsFromNearestPatch());
		}
		
		
	}


	private double[] selectNewLocation(final double[][] nbrCells, Creature creature){

		double[] cumulative = new double[9];
		int j = 0;
		cumulative[0] = nbrCells[0][0];
		for (int y2 = 0; y2 < 3; y2++) {
			for (int x2 = 0; x2 < 3; x2++) {
				if (j != 0) cumulative[j] = cumulative[j-1] + nbrCells[x2][y2];
				j++;
			}
		}
		double rnd = RandomNumberGenerator.zeroToOne();
		double[] newLocation = {creature.getXLocation(), creature.getYLocation()};
		j = 0;
		for (int y2 = 0; y2 < 3; y2++) {
			for (int x2 = 0; x2 < 3; x2++) {
				if (rnd < cumulative[j]) {
					if(nbrCells[x2][y2] > 0 ) {   
						newLocation[0] +=  (x2 - 1)*ModelConfig.CELLSIZE; 
						newLocation[1] += (y2 - 1)*ModelConfig.CELLSIZE; 
						return newLocation;
					}
				}
				j++;
			}
		}
		return newLocation;
	}

	private double[][] calcProbabilityMatrix(final double[][] dpWeights, final double[][] habitatWeights){

		double[][] nbrCells = new double[3][3];
		double sumOfValues = 0.0;
		// determine weighted effective cost for the 8 neighbours
		// multiply directional persistence and habitat-dependent weights
		for (int y2 = 2; y2 > -1; y2--) {
			for (int x2 = 0; x2 < 3; x2++) {
				if(x2 == 1 && y2 == 1) nbrCells[x2][y2] = 0.0;
				else {
					if(x2 == 1 || y2 == 1) //not diagonal
						nbrCells[x2][y2] = dpWeights[x2][y2]*habitatWeights[x2][y2];
					else // diagonal
						nbrCells[x2][y2] =  Math.sqrt(2)*dpWeights[x2][y2]*habitatWeights[x2][y2];
				}
				if(nbrCells[x2][y2] > 0.0) {
					nbrCells[x2][y2] = 1.0/nbrCells[x2][y2]; //take the reciprocal
					sumOfValues += nbrCells[x2][y2];
				}
			}
		}

		//weighted probabilities, sum to 1
		for (int y2 = 2; y2 > -1; y2--) {
			for (int x2 = 0; x2 < 3; x2++) {
				if(nbrCells[x2][y2] >0.0)
					nbrCells[x2][y2] = nbrCells[x2][y2]/sumOfValues;
			}
		}

		return nbrCells;
	}

	private double[][] calcDPWeightings( final double base, final double theta) {

		double[][] d = new double[3][3]; // 3x3 array indexed from SW corner by xx and yy



		if(theta == -9999.9) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					d[i][j] = 1.0;
				}
			}
		}
		else {
			int dx,dy,xx,yy;
			double i0 = 1.0; 					// direction of theta - lowest cost bias
			double i1 = base;
			double i2 = base * base;
			double i3 = i2 * base;
			double i4 = i3 * base;		// opposite to theta - highest cost bias

			if (Math.abs(theta) > 7.0 * Math.PI / 8.0) { dx = 0; dy = -1; }
			else {
				if (Math.abs(theta) > 5.0 * Math.PI / 8.0) { dy = -1; if (theta > 0) dx = 1; else dx = -1; }
				else {
					if (Math.abs(theta) > 3.0 * Math.PI / 8.0) { dy = 0; if (theta > 0) dx = 1; else dx = -1; }
					else {
						if (Math.abs(theta) > Math.PI / 8.0) { dy = 1; if (theta > 0) dx = 1; else dx = -1; }
						else { dy = 1; dx = 0; }
					}
				}
			}

			d[1][1] = 0; // central cell has zero weighting
			d[dx+1][dy+1] = (float)i0;
			d[-dx+1][-dy+1] = (float)i4;
			if (dx == 0 || dy ==0) { // theta points to a cardinal direction
				d[dy+1][dx+1] = (float)i2; d[-dy+1][-dx+1] = (float)i2;
				if (dx == 0) { // theta points N or S
					xx = dx+1; if (xx > 1) dx -= 2; yy = dy;
					d[xx+1][yy+1] = (float)i1; d[-xx+1][yy+1] = (float)i1;
					d[xx+1][-yy+1] = (float)i3; d[-xx+1][-yy+1] = (float)i3;
				}
				else { // theta points W or E
					yy = dy+1; if (yy > 1) dy -= 2; xx = dx;
					d[xx+1][yy+1] = (float)i1; d[xx+1][-yy+1] = (float)i1;
					d[-xx+1][yy+1] = (float)i3; d[-xx+1][-yy+1] = (float)i3;
				}
			}
			else { // theta points to an ordinal direction
				d[dx+1][-dy+1] = (float)i2; d[-dx+1][dy+1] = (float)i2;          
				xx = dx+1; if (xx > 1) xx -= 2; d[xx+1][dy+1] = (float)i1;
				yy = dy+1; if (yy > 1) yy -= 2; d[dx+1][yy+1] = (float)i1;
				d[-xx+1][-dy+1] = (float)i3; d[-dx+1][-yy+1] = (float)i3;
			}
		}
		return d;
	}



}
