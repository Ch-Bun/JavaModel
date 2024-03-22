package dispersal;

import utils.DistributionSampler;

public class DispersalKernelParameters extends TransferParameters{

	private final DistributionSampler meanKernelDistanceDistribution;
	private final boolean distanceDependentMortality;
	private final double mortalityRate; 
	private final double mortalitySlope;
	private final double mortalityInflection;
	
	
	DispersalKernelParameters(DispersalKernelBuilder dispersalKernelBuilder) {
		super(dispersalKernelBuilder.evolving, dispersalKernelBuilder.maxStepsPerDispersalEvent);

		this.meanKernelDistanceDistribution = dispersalKernelBuilder.meanKernelDistanceDistribution;
		this.distanceDependentMortality = dispersalKernelBuilder.distanceDependentMortality;
		this.mortalityRate = dispersalKernelBuilder.mortalityRate;
		this.mortalitySlope = dispersalKernelBuilder.mortalitySlope;
		this.mortalityInflection = dispersalKernelBuilder.mortalityInflection;


	}
	
	

	
	public double getMeanKernel() {
		return meanKernelDistanceDistribution.sample();
	}




	public boolean isDistanceDependentMortality() {
		return distanceDependentMortality;
	}




	public double getMortalityRate() {
		return mortalityRate;
	}




	public double getMortalitySlope() {
		return mortalitySlope;
	}




	public double getMortalityInflection() {
		return mortalityInflection;
	}


	public double sample() {
		return meanKernelDistanceDistribution.sample();
	}


	public static class DispersalKernelBuilder{

		private boolean evolving=false;
		private final boolean distanceDependentMortality;
		private DistributionSampler meanKernelDistanceDistribution;
		private double mortalityRate;
		private double mortalityInflection;
		private double mortalitySlope;
		private final short maxStepsPerDispersalEvent = 1;
	

		public DispersalKernelBuilder(DistributionSampler meanKernelDistanceDistribution, boolean distanceDependentMortality) {
			super();
			this.meanKernelDistanceDistribution=meanKernelDistanceDistribution;
			this.distanceDependentMortality = distanceDependentMortality;
		}
		
		public void mortalityInflection(double mortalityInflection) {
			this.mortalityInflection=mortalityInflection;
		}
		
		public void mortalityRate(double mortalityRate) {
			this.mortalityRate=mortalityRate;
		}

		public void mortalitySlope(double mortalitySlope) {
			this.mortalitySlope=mortalitySlope;
		}

		public void meanKernelDistanceDistribution(DistributionSampler meanKernelDistanceDistribution) {
			this.meanKernelDistanceDistribution=meanKernelDistanceDistribution;
		}

		public DispersalKernelParameters build() {
			DispersalKernelParameters kernelParameters =  new DispersalKernelParameters(this);
			return kernelParameters;
		}

	}

	
	
}
