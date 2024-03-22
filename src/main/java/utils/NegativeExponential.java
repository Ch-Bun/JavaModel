package utils;

import global.ModelConfig;

public class NegativeExponential implements DistributionSampler{

	private final double value;
	private final String distributionType;
	
	NegativeExponential(double value, String distributionType){
		this.value=value;
		this.distributionType=distributionType;
	}
	
	public double sample() { //sample default
		
		double r1 = 0.0000001 + RandomNumberGenerator.zeroToOne()*(1.0-0.0000001);
		return (-1.0*value)*Math.log(r1);
	}
	


	@Override
	public String distributionType() {
		return distributionType;
	}
	
}
