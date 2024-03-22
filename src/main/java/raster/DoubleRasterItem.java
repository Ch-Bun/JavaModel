package raster;

public class DoubleRasterItem implements RasterItem {
	private double theDouble;

	public DoubleRasterItem (double d) {
		this.theDouble = d;
	}
	
	public double getDouble() {
		return theDouble;
	}

	public void setDouble(double d) {
		theDouble = d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(theDouble);
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
		DoubleRasterItem other = (DoubleRasterItem) obj;
		if (Double.doubleToLongBits(theDouble) != Double
				.doubleToLongBits(other.theDouble))
			return false;
		return true;
	}


}