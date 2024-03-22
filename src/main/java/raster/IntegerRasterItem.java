package raster;


public class IntegerRasterItem implements RasterItem {
	private int theInteger;

	public IntegerRasterItem (int i) {
		this.theInteger = i;
	}
	
	public int getInt() {
		return theInteger;
	}

	public void setInt(int i) {
		theInteger = i;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + theInteger;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		IntegerRasterItem other = (IntegerRasterItem) obj;
//		if (theInteger != other.theInteger)
//			return false;
//		return true;
//	}


}