package dispersal;

import utils.DistributionSampler;

public class SMSTransferParameters extends TransferParameters {

	private final Boolean goalBias;
	private final Integer memSize;
	private final double perStepMortality;
	private DistributionSampler dpDistribution;
	private DistributionSampler gbDistribution;
	private DistributionSampler alphaDistribution;
	private DistributionSampler betaDistribution;

	SMSTransferParameters(SMSTransferBuilder smsBuilder) {
		super(smsBuilder.evolving, smsBuilder.maxStepsPerDispersalEvent);

		this.goalBias = smsBuilder.goalBias;
		this.memSize = smsBuilder.memSize;
		this.perStepMortality=smsBuilder.perStepMortality;
		this.dpDistribution = smsBuilder.dpDistribution;
		this.gbDistribution = smsBuilder.gbDistribution;
		this.alphaDistribution = smsBuilder.alphaDistribution;
		this.betaDistribution = smsBuilder.betaDistribution;

	}

	public Boolean isGoalBias() {
		return goalBias;
	}

	public Integer getMemSize() {
		return memSize;
	}

	public double getStepMortality() {
		return perStepMortality;
	}
	
	public double getGoalBias() {
		return gbDistribution.sample();
	}

	public double getDp() {
		return dpDistribution.sample();
	}

	public double getAlpha() {
		return alphaDistribution.sample();
	}

	public double getBeta() {
		return betaDistribution.sample();
	}

	public static class SMSTransferBuilder{

		private final double perStepMortality;
		private boolean evolving=false;
		private final short maxStepsPerDispersalEvent;
		private final boolean goalBias;
		private int memSize;
		private DistributionSampler dpDistribution;
		private DistributionSampler gbDistribution;
		private DistributionSampler alphaDistribution;
		private DistributionSampler betaDistribution;

		public SMSTransferBuilder(int memSize, boolean goalBias, double perStepMortality, short maxStepsPerDispersalEvent) {
			super();
			this.goalBias=goalBias;
			this.memSize=memSize;
			this.perStepMortality=perStepMortality;
			this.maxStepsPerDispersalEvent =maxStepsPerDispersalEvent;
		}

		public void evolving(Boolean evolving) {
			this.evolving = evolving;
		}

		public void dpDistribution(DistributionSampler dpDistribution) {
			this.dpDistribution=dpDistribution;
		}

		public void gbDistribution(DistributionSampler gbDistribution) {
			this.gbDistribution=gbDistribution;
		}

		public void alphaDistribution(DistributionSampler alphaDistribution) {
			this.alphaDistribution=alphaDistribution;
		}

		public void betaDistribution(DistributionSampler betaDistribution) {

			this.betaDistribution=betaDistribution;
		}

		public SMSTransferParameters build() {
			SMSTransferParameters smsParameters =  new SMSTransferParameters(this);
			return smsParameters;
		}

	}



}
