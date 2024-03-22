package utils;

public class Interpolator {

	public static Double interpolate(Double from, Double to, double factor) {
		if (from == null) {
			if (to == null) 
				return 0.0;
			return to;
		}
		else if (to == null)
			return from;
			
		Double res = interpolate(from.doubleValue(), to.doubleValue(), factor);
		return res;
	}
	
	public static double interpolate(double from, double to, double factor) {
		return from + factor * (to - from);
	}
}
