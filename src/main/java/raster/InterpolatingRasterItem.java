package raster;

public interface InterpolatingRasterItem<I> extends RasterItem {

	void interpolateAll(I fromItem, I toItem, double factor);
}
