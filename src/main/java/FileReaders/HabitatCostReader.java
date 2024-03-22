package FileReaders;

import land.Cell;
import raster.AbstractRasterReader;
import raster.RasterSet;


public class HabitatCostReader extends AbstractRasterReader<Cell> {	

	public HabitatCostReader (RasterSet<Cell> dataset) {
		super(dataset);
	}

	@Override
	public void setData(Cell item, String token) {
		
		item.updateHabitatCost(Double.parseDouble(token));

	}
}
