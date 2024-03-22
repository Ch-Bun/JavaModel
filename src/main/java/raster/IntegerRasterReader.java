package raster;

public class IntegerRasterReader extends AbstractRasterReader<IntegerRasterItem> {	
	
	public IntegerRasterReader (RasterSet<IntegerRasterItem> dataset) {
		super(dataset);
	}

	@Override
	public void setData(IntegerRasterItem item, String token) {
		if (!"nan".equals(token)) {	
			int i = Integer.parseInt(token);
			item.setInt(i);
		}
	}
}
