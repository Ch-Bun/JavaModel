package utils;

import org.apache.commons.math3.distribution.RealDistribution;

public class DistributionType implements DistributionSampler {

	private final String distributionType;
	private final RealDistribution distribution;
	
	DistributionType(RealDistribution distribution, String distributionType){
		this.distribution=distribution;
		this.distributionType=distributionType;
	}
	
	public double sample() {
		return distribution.sample();
	}
	
	public String distributionType() {
		return distributionType;
	}
}
