package utils;

public class FixedValue implements DistributionSampler {

	private final double value;
	private final String distributionType;
	
	FixedValue(double value, String distributionType){
		this.value=value;
		this.distributionType=distributionType;
	}
	
	public double sample() {
		return value;
	}

	@Override
	public String distributionType() {
		return distributionType;
	}
}
