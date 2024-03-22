package raster;

import java.io.Serializable;

public class RasterHeaderDetails implements Serializable{
	private static final long serialVersionUID = 8931052507772280805L;
	
	private int ncolumns;
	private int nrows;
	private double xllCorner;
	private double yllCorner;
	private double xcellSize;
	private String nodataString;
	
	public RasterHeaderDetails(int ncolumns, int nrows, double xllCorner, double yllCorner, double xcellSize, String nodataString) {
		super();
		this.ncolumns = ncolumns;
		this.nrows = nrows;
		this.xllCorner = xllCorner;
		this.yllCorner = yllCorner;
		this.xcellSize = xcellSize;
		this.nodataString = nodataString;
	}
	
	public int getNcolumns() {
		return ncolumns;
	}

	public int getNrows() {
		return nrows;
	}

	public double getXllCorner() {
		return xllCorner;
	}

	public double getYllCorner() {
		return yllCorner;
	}

	public double getXCellSize() {
		return xcellSize;
	}
	
	public String getNodataString() {
		return nodataString;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ncolumns;
		result = prime * result
				+ ((nodataString == null) ? 0 : nodataString.hashCode());
		result = prime * result + nrows;
		long temp;
		temp = Double.doubleToLongBits(xcellSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xllCorner);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yllCorner);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RasterHeaderDetails other = (RasterHeaderDetails) obj;
		if (ncolumns != other.ncolumns)
			return false;
		if (nodataString == null) {
			if (other.nodataString != null)
				return false;
		} else if (!nodataString.equals(other.nodataString))
			return false;
		if (nrows != other.nrows)
			return false;
		if (Double.doubleToLongBits(xcellSize) != Double
				.doubleToLongBits(other.xcellSize))
			return false;
		if (Double.doubleToLongBits(xllCorner) != Double
				.doubleToLongBits(other.xllCorner))
			return false;
		if (Double.doubleToLongBits(yllCorner) != Double
				.doubleToLongBits(other.yllCorner))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ncolumns=" + ncolumns + 
				", nrows=" + nrows + 
				", xllCorner=" + xllCorner + 
				", yllCorner=" + yllCorner + 
				", xcellSize" + xcellSize + 
				", nodataString=" + nodataString;
	}

}
