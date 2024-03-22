package land;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import environment.EnvironmentalVariable;
import global.ModelConfig;
import raster.RasterItem;
import utils.LogWriter;

public class Cell implements RasterItem, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3330765479933210991L;

	// builder pattern for cell since it may be that some variables are not used for
	// different types of simulations
	private Map<HabitatType, Double> landCoverPercent;

	private final static double area = Math.pow(ModelConfig.CELLSIZE, 2) / 100; // assuming projected co-ordinate system
	// and square cells, in ha assuming cell
	// size is km2
	private double K; //double because effectively b DD parameter 
	private Double movementCost;
	private Double habitatMortality;
	private Map<EnvironmentalVariable, Double> environment; // this is maybe a heavy way to store this data
	// environment doesn't affect K but affects R?
	private List<Double> annualRainfall;
	private double[][] crossingProbabilities;
	private static LandscapeRaster landscapeRaster;

	public Cell(LandscapeRaster landscapeRaster) {
		landCoverPercent = new HashMap<HabitatType, Double>();

		if(Cell.landscapeRaster == null)
			Cell.landscapeRaster = landscapeRaster;
		//if (ModelConfig.TRANSFERTYPE.equals("SMS")) {
		//	crossingProbabilities = new double[3][3];
		//}
	}

	public void setLandCoverPercent(int habType, double percent) {

		HabitatType habitat = HabitatFactory.getHabitatType(habType);
		Double percentD = landCoverPercent.get(habitat);

		if (percentD == null) {
			landCoverPercent.put(habitat, percent);
		} else
			LogWriter.printlnWarning("Duplicate habitat cover for cell " + toString() + " habitat type " + habitat);
	
	setK();
	
	}

	private void setK() {
		double K = 0;
		for (Entry<HabitatType, Double> entry : landCoverPercent.entrySet()) {
			double habitatK = entry.getKey().getK();
			double coverPercentage = entry.getValue();
			K += coverPercentage * habitatK;
		}
		this.K= K;
	}

	//setting K from raster (quality raster)
	public void setK(int K) {
		this.K=K;
	}

	public double getK() {
		return K;
	}

	public double getArea() {
		return area;
	}

	public double getLandCoverPercent(HabitatType habitat) {

		double cover = 0.0;
		if (landCoverPercent.containsKey(habitat))
			cover = landCoverPercent.get(habitat);

		return cover;
	}

	//below is when habitat costs are different from habitat type, e.g. habitat suitability differs from cost of movement through

	public void updateHabitatCost(double movementCost) {

		this.movementCost = movementCost;
	}

	public double getHabitatCost() {
		if(movementCost == null) {
			movementCost=0.0;
			for (Entry<HabitatType, Double> entry : landCoverPercent.entrySet()) {
				movementCost += entry.getKey().getCost();
			}

			movementCost /= landCoverPercent.size();
		}
		return movementCost;
	}


	//need this in case landscape percents change in dynamic landscape sims 
	public void updateHabitatCost() {

		for (Entry<HabitatType, Double> entry : landCoverPercent.entrySet()) {
			movementCost += entry.getKey().getCost();
		}
		movementCost /= landCoverPercent.size();
	}


	public double getHabitatMortality() {
		if(habitatMortality != null) {
			habitatMortality= 0.0;
		for (Entry<HabitatType, Double> entry : landCoverPercent.entrySet()) {
			habitatMortality += entry.getKey().getMortality();
		}

		habitatMortality  /= landCoverPercent.size();
		
		}

		return habitatMortality;
	}
	
	public void updateHabitatMortality() {

		for (Entry<HabitatType, Double> entry : landCoverPercent.entrySet()) {
			habitatMortality += entry.getKey().getCost();
		}
		habitatMortality /= landCoverPercent.size();
	}
	
	public void updateHabitatMortality(double habitatMortality) {

		this.habitatMortality = habitatMortality;
	}
	
	

	public double[][] getCrossingProbabilities() {
		
		if(crossingProbabilities == null) {
			crossingProbabilities = new double[3][3];
			landscapeRaster.setSMSCosts(this);
		}
		
		return crossingProbabilities;
	}


	public void setEnvironmentVariable(EnvironmentalVariable variable, double value) {
		if (environment == null)
			environment = new HashMap<EnvironmentalVariable, Double>();

		environment.put(variable, value);

	}


	public void setAnnualRainfall(double[] rainfallArray) {

		Double[] doubleArray = ArrayUtils.toObject(rainfallArray); 
		annualRainfall = new ArrayList<>(Arrays.asList(doubleArray)); 
	}

	public void setHabitatCostSquare(int x, int y, double value) {
		crossingProbabilities[x][y] = value;
	}

}
