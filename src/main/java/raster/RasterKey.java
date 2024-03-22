package raster;

import java.io.Serializable;

public class RasterKey implements Serializable {

	private static final long serialVersionUID = 7798904172536867819L;
	private int col;
	private int row;
	
	public RasterKey(RasterKey key) {
		col = key.getCol();
		row = key.getRow();
	}
	
	public RasterKey(int col, int row) {
		this.col = col;
		this.row = row;
	}
	
	public RasterKey createShifted(int colShift, int rowShift) {
		if (colShift == 0 && rowShift == 0)
			return this;
		else
			return new RasterKey(col + colShift, row + rowShift);
	}
	
	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public double getDistanceTo(RasterKey x) {
		double d = Math.sqrt(Math.pow(col - x.col,2) + Math.pow(row - x.row,2));
		return d;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RasterKey))
			return false;
		RasterKey other = (RasterKey) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}	
	
	public String toString() {
		return "col:" + col + ", row:" + row;
	}
}
