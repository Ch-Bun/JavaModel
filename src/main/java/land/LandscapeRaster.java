package land;


import java.util.Collections;
import java.util.List;

import global.ModelConfig;
import raster.RasterHeaderDetails;
import raster.RasterKey;
import raster.RasterSet;
import utils.LogWriter;
import utils.RandomNumberGenerator;

public class LandscapeRaster extends RasterSet<Cell> {

	private static final long serialVersionUID = -4282129788267999900L;

	public LandscapeRaster(RasterHeaderDetails header) {
		super(header);
	}

	protected Cell createRasterData() {
		return new Cell(this);
	}

	public void generate() {

		int habitat; 

		if(ModelConfig.FRACTAL) {

			FractalLandscapeRaster fractalLandscape = new FractalLandscapeRaster(this.getHeaderDetails(),
					ModelConfig.HURST_EXPONENT);

			List<Double> fractalValues=fractalLandscape.getMapValues();

			double proportionValue = percentile(fractalValues);


			for (int c = 0; c<this.getHeaderDetails().getNcolumns(); c++) {
				for (int r = 0; r<this.getHeaderDetails().getNrows(); r++) {
					habitat = 0; //default is matrix 
					Cell cell = get(c, r);// JAA-Sandbjerg : getFromCoordinates(c, r) always returns (0,4)
					RasterKey key = new RasterKey(c, r); // JAA_Sandbjerg: getKeyFromCoordinates(c, r) always returns (0,4)

					double cellFractalValue = fractalLandscape.get(key).getDouble();

					if(cellFractalValue < proportionValue)
						habitat=1; //suitable

					cell.setLandCoverPercent(habitat, ModelConfig.BINARY_FRACTAL ? 1.0: cellFractalValue); //BUG: cellFractalValue can be -ve, range needs to be normalised before doing this alternative here is to set 2nd argument to fractal value and then * by habitat file value to get K over fractal land gradient
				}


			}
		}
		else {

			for (int c = 0; c<this.getHeaderDetails().getNcolumns(); c++) {
				for (int r = 0; r<this.getHeaderDetails().getNrows(); r++) {
					habitat = 0; //default is matrix 
					Cell cell = get(c, r);
					if(RandomNumberGenerator.zeroToOne() < ModelConfig.PROPORTION_SUITABLE) 
						habitat = 1;

					cell.setLandCoverPercent(habitat, 1.0);
				}
			}


		}
	}
	private double percentile(List<Double> fractalValues) {

		double index =  ModelConfig.PROPORTION_SUITABLE*(fractalValues.size()-1);

		int low = (int) Math.floor(index);
		int high = (int) Math.ceil(index);

		Collections.sort(fractalValues);

		double pValue = fractalValues.get(low);

		if (index > low) {
			double h = index - low;
			pValue = (1 - h) * pValue + h *  fractalValues.get(high);
		}
		return pValue;

	}


	public void setSMSCosts(Cell cell) {


		//LogWriter.println("Setting up SMS cost matrices...");
		int pr = (int) ModelConfig.PERCEPTUALRANGE;

		RasterKey rasterKey = this.keys(cell);

			int focalXLocation = rasterKey.getCol();
			int focalYLocation = rasterKey.getRow();

			// NW and SE corners of effective cost array relative to the current cell (x,y):
			int xmin = 0,ymin = 0,xmax = 0,ymax = 0;

			for (int x2=-1; x2<2; x2++) {   // index of relative move in x direction
				for (int y2=-1; y2<2; y2++) { // index of relative move in x direction

					if(!(x2==0 && y2 == 0)) {
						if (x2==0 || y2==0) { // not diagonal (rook move)
							if (x2==0){ // vertical (N-S) move
								if(pr%2==0) { xmin=-pr/2; xmax=pr/2; ymin=y2; ymax=y2*pr; } // PR even
								else { xmin=-(pr-1)/2; xmax=(pr-1)/2; ymin=y2; ymax=y2*pr; } // PR odd
							}
							if (y2==0) { // horizontal (E-W) move
								if(pr%2==0) { xmin=x2; xmax=x2*pr; ymin=-pr/2; ymax=pr/2; } // PR even
								else { xmin=x2; xmax=x2*pr; ymin=-(pr-1)/2; ymax=(pr-1)/2; } // PR odd
							}
						}
						else { // diagonal (bishop move)
							xmin=x2; xmax=x2*pr; ymin=y2; ymax=y2*pr;
						}
						if (xmin > xmax) { int z=xmax; xmax=xmin; xmin=z; } // swap xmin and xmax

						RasterKey neighbouringCellKey = new RasterKey(focalXLocation +x2, focalYLocation - y2); //-y2 here because y of rasterset is inverted 

					//	LogWriter.println("focal cell " + focalXLocation + " , " + focalYLocation);
					//	LogWriter.println("neighboring cell " + (focalXLocation +x2) + " , " + (focalYLocation - y2));


						if(this.containsKey(neighbouringCellKey)) { //this means out of bounds or no data patches have probability of crossing of 0.0 so avoid
							//add number of no data cells to total *100000
							int fXMin = focalXLocation + xmin;
							int fXMax = focalXLocation + xmax;
					
							int fYMin;
							int fYMax;
							
							if ((focalYLocation - ymin) > (focalYLocation - ymax)){ 
								 fYMin = focalYLocation - ymax; //- because y of raster set is inverted
								 fYMax = focalYLocation - ymin; 
							} // swap ymin and ymax
							else {
								 fYMin = focalYLocation - ymin; //- because y of raster set is inverted
								 fYMax = focalYLocation - ymax;
							}

//							List<RasterKey> keys = this.keySet().stream()
//									.filter(p->p.getCol() >= fXMin)
//									.filter(p->p.getCol() <= fXMax)
//									.filter(p->p.getRow() >= fYMin)
//									.filter(p->p.getRow() <= fYMax)
//									.collect(Collectors.toList());
//
//							List<Cell> surroundingCells = new ArrayList<Cell>();

							double neighbourCost =0.0;
							
							for(int fx= fXMin; fx <= fXMax; fx++) {
								for(int fy = fYMin; fy <= fYMax; fy++) {
									//torus for purpose of having a cost value 
									int x = (fx < 0) ? fx+ this.getNcolumns()+1 : (fx > this.getNcolumns()) ? fx-this.getNcolumns()-1 : fx ;
									int y = (fy < 0) ? fy+ this.getNrows()+1 : (fy > this.getNrows()) ? fy-this.getNrows()-1 : fy ;
									
									
									RasterKey key = new RasterKey(x,y);
									
									Cell cellA = get(key);
									if(cellA != null)
										neighbourCost += cellA.getHabitatCost();
									else
										
										neighbourCost += ModelConfig.HABITAT_NO_DATA_COST;
									
								}
							}
							
					
							
							 neighbourCost /= pr*pr; //arithmetic mean

							cell.setHabitatCostSquare(x2+1, y2+1, neighbourCost);
						}
					}


				}

			}
		
	}

}


