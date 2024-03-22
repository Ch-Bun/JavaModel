package land;

import global.ModelConfig;
import raster.AbstractRasterReader;
import raster.RasterSet;
import utils.LogWriter;
//this class reads in cells with singular habitat type but abstract tabular reader could be used for cells with multiple habitat types
//see parameter shock reader for flexible tabular reader
public class LandscapeReader extends AbstractRasterReader<Cell> {	

	public LandscapeReader (RasterSet<Cell> dataset) {
		super(dataset);
	}

	@Override
	public void setData(Cell item, String token) {

		if(ModelConfig.USING_HABITAT_CODES) {
		try {
			int habitatCode = Integer.parseInt(token);
			
			if(HabitatFactory.checkForHabitatType(habitatCode)){
				if (!"nan".equals(token)) {	
					item.setLandCoverPercent(habitatCode, 1.0);
				}
			}
		}
		catch (Exception e) {
			LogWriter.printlnError("No habitat carrying capacity data for habitat code " + token);
			LogWriter.print(e);
		}

		}
		else item.setK(Integer.parseInt(token));
	}
}
