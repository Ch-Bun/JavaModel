package raster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import global.ModelConfig;
import utils.LogWriter;

/* Class which holds raster data.  The generics used to defines the type of raster data held.
 */
public class RasterSet<D extends RasterItem> extends DualHashBidiMap<RasterKey, D> {

	private static final long serialVersionUID = 4180188703734898215L;
	private RasterHeaderDetails header;

	public RasterSet(RasterHeaderDetails header) {
		this.header = header;
	}

	/** Returns the raster item for the row and column. If an source header is provided these are assumed to be that format system */
	public Collection<D> getCollection(int col, int row, RasterHeaderDetails source) {

		Collection<D> dataPoints = new ArrayList<D>();
		int c, r;

		if(source.getXCellSize() != header.getXCellSize()) {
			if (source.getXCellSize() % header.getXCellSize() != 0)
				throw new RuntimeException("Cell sizes must be the same or souce an integer multiple of destination");

			double cellRatio = source.getXCellSize() / header.getXCellSize();

			for (int x = 0; x<cellRatio; x++) {
				for (int y = 0; y<cellRatio; y++) {
					c = (int)(col*cellRatio) + x;
					r = (int)(row*cellRatio) + y;

					D d = get(c, r);
					if (d != null)
						dataPoints.add(d);
				}
			}
		}	
		else {
			D d = get(col, row);
			if (d != null)
				dataPoints.add(d);
		}

		return dataPoints;
	}

	/** Method to get raster item for coordinates */
	public D getFromCoordinates(double source_x, double source_y) {
		RasterKey key = getKeyFromCoordinates(source_x, source_y);
		return get(key.getCol(), key.getRow());   // can't just call get(key) as may need to create the RasterItem
	}

	public RasterKey getKeyFromCoordinates(double source_x, double source_y) {
		int col = (int)((source_x - header.getXllCorner())/header.getXCellSize()); // +0.01 to avoid floating point issues in truncation to int?
		// header.getNrows() - 1, lower left basis.  Minus 1 as nrows is number, but indexed from zero
		if(col < 0 || col >= header.getNcolumns())
			col = findBoundary(col,header.getNcolumns());
		int row = header.getNrows() - 1 - (int)((source_y - header.getYllCorner())/header.getXCellSize());
		if(row < 0 || row >= header.getNcolumns())
			row = findBoundary(row,header.getNcolumns());

		if (row < 0 || col < 0) {
			LogWriter.println("Got negative row or col values: " + row + ", "+  col);
		}
		return new RasterKey(col, row);
	}

	private int findBoundary(int x, int upperLimit) {

		int newX = x;
		switch(ModelConfig.BOUNDARYCONDITION) {
		case("reflective"):
			if(x < 0)
				newX = 0;
		if(x >= upperLimit)
			newX = upperLimit-1;

		break;
		case("torus"):
			if(x < 0)
				newX = upperLimit + x;
		if(x >= upperLimit)
			newX = x-upperLimit;

		break;


		}
		return newX;

	}



	public double getXCoordin(RasterKey key) {
		double x = header.getXCellSize() * key.getCol() + header.getXllCorner();
		return x;
	}

	public double getYCoordin(RasterKey key) {
		double y = getYCoordin(key.getRow());
		return y;
	}

	private double getYCoordin(int row) {
		double y = header.getXCellSize() * (header.getNrows() - 1 - row) + header.getYllCorner();
		return y;
	}

	// get the RasterItem if it already exists
	//    public D get(RasterKey key) {
	//		return get(key);
	//	}

	/** Method to really get the data, or create it. Assumes the col and row are in the internal header format */
	public D get(int col, int row) {
		//		if (header.getNcolumns() < col+1)
		//			header.incrementNcolumns();
		//		if (header.getNrows() < row+1) {
		//			header.incrementNrows();
		//		}

		RasterKey key = new RasterKey(col, row);
		D data = get(key);
		if (data == null) {
			data = createRasterData();
			if (data != null)
				put(key, data);
		}
		return data;
	}

	/** Some classes do not create, those that do will need to override */
	protected D createRasterData() {
		return null;
	}

	public int getNcolumns() {
		return header.getNcolumns();
	}

	public int getNrows() {
		return header.getNrows();
	}	

	public RasterHeaderDetails getHeaderDetails() {
		return header;
	}

	/** Check passed in header is consistent with one from this set.
	 *   Actually only checks grid size as can shift rasters*/
	public boolean isConstistentWithHeader(RasterHeaderDetails h) {
		return h.getXCellSize() == header.getXCellSize();
	}

	public RasterSet<D> createSubsetForKeys(Collection<RasterKey> keys) {
		return popSubsetForKeys (new RasterSet<D>(getHeaderDetails()), keys);
	}

	protected RasterSet<D> popSubsetForKeys(RasterSet<D> subset, Collection<RasterKey> keys) {		
		for (RasterKey key : keys) {
			//LogWriter.println("popSubsetForKeys: " + key.getCol() + ", "  + key.getRow() + ": " + getXCoordin(key) + ", " + getYCoordin(key));
			subset.put(key, get(key));
		}
		return subset;
	}

	public void putAll(RasterSet<D> rasterToAdd) {
		if (!isConstistentWithHeader(rasterToAdd.getHeaderDetails()))
			throw new RuntimeException("RasterSet.putAll: Headers not consistent");

		for (Map.Entry<RasterKey, D> e : rasterToAdd.entrySet()) {
			put(e.getKey(), e.getValue());  // bit worried the extend needs to be update, but it will not be
		}
	}

	public RasterKey keys(D value) {

		//	    	List<RasterKey> keyList = this
		//	    		.entrySet()
		//	    		.stream()
		//	    		.filter(entry -> value.equals(entry.getValue()))
		//	    		.map(Map.Entry::getKey)
		//	    		.collect(Collectors.toList());
		//	    	
		//	    	if(keyList.size()> 1)
		//	    		LogWriter.printlnError("More than one key for value " + value.toString() + " will return only the first patch ");
		//	    	
		//	    	return keyList.get(0);

		return this.getKey(value);
	}
}
