package utils;

public interface DistributionSampler {

	abstract double sample();
	
	abstract String distributionType();
}
