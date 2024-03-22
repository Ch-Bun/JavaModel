package raster;

import utils.LogWriter;

public class InterpolatingRasterSet<D extends InterpolatingRasterItem<D>> extends RasterSet<D> {

	private static final long serialVersionUID = 7372086730280503581L;

	public InterpolatingRasterSet(RasterHeaderDetails header) {
		super(header);
	}

	public void setup(RasterSet<D> fromSet, RasterSet<D> toSet, double fromValue, double toValue, double desiredValue) {
		clear();
		
		if ( !(isConstistentWithHeader(toSet.getHeaderDetails()) && isConstistentWithHeader(fromSet.getHeaderDetails())) ) {
			throw new RuntimeException("Raster headers inconsistent");
		}
		
		double factor = ((double)(desiredValue - fromValue)) / (toValue - fromValue);
		if (factor > 1 || factor < 0) {
			LogWriter.printlnError("InterpolatingRasterSet: Not interpolating, but extrapolating " + factor);
		}
		
		for (RasterKey key : fromSet.keySet()) {
			D fromItem = fromSet.get(key);
			D toItem = toSet.get(key);
			D newItem = createRasterData();

			if (fromItem == null || toItem == null)
				LogWriter.printlnError("InterpolatingRasterSet: Got nulls for key" + key);
			else {
				newItem.interpolateAll(fromItem, toItem, factor);
				put(key, newItem);
			}
		}
	}

}
