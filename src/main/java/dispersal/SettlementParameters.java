package dispersal;

import utils.DistributionSampler;

public class SettlementParameters {

	private final boolean findMate;
	private final boolean evolving;
	private final boolean densDep;
	private final double densityBValue;
	private final DistributionSampler betaDistribution;
	private final DistributionSampler sODistribution;
	private final DistributionSampler alphaDistribution;
	private final double probabilityOfUnsettling; 
	
	
	private SettlementParameters(SettlementBuilder smsSettlementBuilder) {
		this.probabilityOfUnsettling=smsSettlementBuilder.probabilityOfUnsettling;
		this.densDep = smsSettlementBuilder.densDep;
		this.findMate = smsSettlementBuilder.findMate;
		this.evolving = smsSettlementBuilder.evolving;
		this.sODistribution = smsSettlementBuilder.sODistribution;
		this.alphaDistribution = smsSettlementBuilder.alphaDistribution;
		this.betaDistribution = smsSettlementBuilder.betaDistribution;
		this.densityBValue = smsSettlementBuilder.densityBValue;
	}

	public double getProbabilityOfUnsettling() {
		return probabilityOfUnsettling;
	}
	
	public boolean isDensDep() {
		return densDep;
	}



	public boolean isFindMate() {
		return findMate;
	}
	
	public boolean isEvolving() {
		return evolving;
	}

	public double getsO() {
		return sODistribution.sample();
	}

	public double getAlpha() {
		return alphaDistribution.sample();
	}

	public double getBeta() {
		return betaDistribution.sample();
	}
	
	public double getDensityBValue() {
		return densityBValue;
	}

	public static class SettlementBuilder {
		
		private boolean densDep;
		private boolean evolving;
		private boolean findMate;
		private DistributionSampler sODistribution;
		private DistributionSampler alphaDistribution;
		private DistributionSampler betaDistribution;
		private double densityBValue;
		private double probabilityOfUnsettling;
		
		public SettlementBuilder(boolean findMate, double probabilityOfUnsettling) {
			this.findMate = findMate;
			this.probabilityOfUnsettling=probabilityOfUnsettling;
		}

        public void densDep(boolean densDep) {
			this.densDep = densDep;
		}

		public void evolving(boolean evolving) {
			this.evolving=evolving;
		}

		public void sODistribution(DistributionSampler sODistribution) {
			this.sODistribution=sODistribution;
		}

		public void alphaDistribution(DistributionSampler alphaDistribution) {
			this.alphaDistribution=alphaDistribution;
		}

		public void betaDistribution(DistributionSampler betaDistribution) {
			this.betaDistribution=betaDistribution;
		}
		
		public void densityBValue(double densityBValue) {
			this.densityBValue=densityBValue;
		}
		
		
		public SettlementParameters build() {
			SettlementParameters smsSettlement =  new SettlementParameters(this);
            return smsSettlement;
        }
	}
}
