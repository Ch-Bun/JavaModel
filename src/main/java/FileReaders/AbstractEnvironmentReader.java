package FileReaders;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import land.Cell;
import land.LandscapeRaster;
import land.Patch;
import raster.RasterKey;
import utils.LogWriter;

public abstract class AbstractEnvironmentReader {
	static ParameterValue<OverviewPolicy> policy;
	static ParameterValue<String> gridsize;
	static ParameterValue<Boolean> useJaiRead;

	GridCoverage2DReader reader;
	GridCoverage2D coverage;
	int numBands; //should be 365
	CoordinateReferenceSystem crs;

	public AbstractEnvironmentReader(){

		policy = AbstractGridFormat.OVERVIEW_POLICY.createValue();
		policy.setValue(OverviewPolicy.IGNORE);

		//this will basically read 4 tiles worth of data at once from the disk...
		gridsize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();

		//Setting read type: use JAI ImageRead (true) or ImageReaders read methods (false)
		useJaiRead = AbstractGridFormat.USE_JAI_IMAGEREAD.createValue();
		useJaiRead.setValue(true);

	}

	public void readAnnualPatchEnvironment(LandscapeRaster landscapeRaster, Map<Patch, List<RasterKey>> patchMap, int year){
		long startTime = System.currentTimeMillis();
		try {

			String filename = getFilePath(year);
			
			LogWriter.println("Read in " + filename);
			
			GridCoverage2DReader reader = new GeoTiffReader(filename);
			coverage = reader.read(new GeneralParameterValue[]{policy, gridsize, useJaiRead});
			crs=reader.getCoordinateReferenceSystem();

			numBands = 	coverage.getNumSampleDimensions();
			if(!(numBands ==365 || numBands == 366)) 
				LogWriter.printlnWarning("Missing daily enviromental data somewhere for year, " + year + " in file " + filename + ". Number of daily entries = " + numBands);

			//TODO maybe also a check here that crs matches user specified crs?


			GridGeometry2D geometry = coverage.getGridGeometry();
			for (Entry<Patch, List<RasterKey>> entry : patchMap.entrySet()) {
				LogWriter.println("Patch " + entry.getKey().getId());
				List<RasterKey> keys = entry.getValue();

				for(RasterKey key : keys) {

					double xcoord = landscapeRaster.getXCoordin(key);
					double ycoord = landscapeRaster.getYCoordin(key);

					DirectPosition2D location =new DirectPosition2D(crs,xcoord, ycoord);

					double[] annualRainfall = new double[numBands];

					//this is a workaround, should be able to use evaluate on DirectPosition2D but strange ambiguity errors
					GridCoordinates2D gridCo = geometry.worldToGrid(location);

					coverage.evaluate(gridCo, annualRainfall);

					Cell cell = landscapeRaster.get(key);
					
					setCellData(cell, annualRainfall);

					reader.dispose();
				}
			}
		}

		catch (Exception e) {
			LogWriter.printlnError("Error processing rainfall for year " + year);
		}
		LogWriter.println("Reading data for year " + year  + ", took " + (System.currentTimeMillis() - startTime) + " ms");

	}

	public abstract void setCellData(Cell cell, double[] rainfallArray);
	
	public abstract String getFilePath(int year);
	/*
	 * geometry = coverage.getGridGeometry();
	 * 
	 * GridCoverage2D coverageB;
	 * 
	 * GridEnvelope dimensions = reader.getOriginalGridRange(); GridCoordinates
	 * maxDimensions = dimensions.getHigh(); int w =
	 * maxDimensions.getCoordinateValue(0)+1; int h =
	 * maxDimensions.getCoordinateValue(1)+1;
	 * 
	 * try { numBands = reader.getGridCoverageCount();
	 * 
	 * 
	 * for (int i=0; i<w; i++) { for (int j=0; j<h; j++) {
	 * 
	 * org.geotools.geometry.Envelope2D pixelEnvelop = geometry.gridToWorld(new
	 * GridEnvelope2D(i, j, 1, 1));
	 * 
	 * double lat = pixelEnvelop.getCenterY(); double lon =
	 * pixelEnvelop.getCenterX();
	 * 
	 * double[] vals = new double[numBands]; coverage.evaluate(new
	 * GridCoordinates2D(i, j), vals);
	 * 
	 * coverageB.
	 * 
	 * 
	 * 
	 * } }
	 * 
	 * } catch (Exception e) { // TODO Auto-generated catch block
	 * LogWriter.println("problem updating rainfall"); }
	 * 
	 * }
	 */


}

