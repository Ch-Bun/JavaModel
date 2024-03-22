package FileReaders;

import land.Cell;
import raster.AbstractRasterReader;
import raster.RasterSet;


public class HabitatMortalityReader extends AbstractRasterReader<Cell> {	

	public HabitatMortalityReader (RasterSet<Cell> dataset) {
		super(dataset);
	}

	@Override
	public void setData(Cell item, String token) {
		
		item.updateHabitatMortality(Double.parseDouble(token));

	}
}
