package land;

import raster.IntegerRasterItem;
import raster.RasterHeaderDetails;
import raster.RasterSet;

public class PatchRaster extends RasterSet<IntegerRasterItem> {
	private static final long serialVersionUID = 3112137541459361502L;

	public PatchRaster(RasterHeaderDetails header) {
		super(header);
		// TODO Auto-generated constructor stub
	}

	protected IntegerRasterItem createRasterData() {
		return new IntegerRasterItem(-999);
	}
	
}
