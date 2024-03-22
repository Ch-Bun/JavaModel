package dispersal;


import utils.DistributionSampler;

public class EmigrationParameters {

	private final Boolean densityDependant;
	private final Boolean evolving; //if false then the below parameters are used otherwise the trait genome values are used 
	private final DistributionSampler doDistribution;
	private final DistributionSampler alphaDistribution;
	private final DistributionSampler betaDistribution;

	private EmigrationParameters(EmigrationBuilder emigrationBuilder) {
		this.densityDependant = emigrationBuilder.densityDependant;
		this.evolving=emigrationBuilder.evolving;
		this.doDistribution=emigrationBuilder.doDistribution;
		this.alphaDistribution=emigrationBuilder.alphaDistribution;
		this.betaDistribution=emigrationBuilder.betaDistribution;
	}

	public Boolean getDensityDependant() {
		return densityDependant;
	}

	public Boolean getEvolving() {
		return evolving;
	}

	public double getdO() {
		return doDistribution.sample();
	}

	public double getAlpha() {
		return alphaDistribution.sample();
	}

	public double getBeta() {
		return betaDistribution.sample();
	}

	public static class EmigrationBuilder{

		private final Boolean densityDependant;
		private final Boolean evolving; //if true then the below sets the initial distribution
		private DistributionSampler doDistribution;
		private DistributionSampler alphaDistribution;
		private DistributionSampler betaDistribution;


		public EmigrationBuilder(Boolean densityDependant, Boolean evolving) {
			this.densityDependant = densityDependant;
			this.evolving = evolving;
		}

		public void doDistribution(DistributionSampler doDistribution) {
			this.doDistribution=doDistribution;
		}

		public void alphaDistribution(DistributionSampler alphaDistribution) {
			this.alphaDistribution=alphaDistribution;
		}

		public void betaDistribution(DistributionSampler betaDistribution) {

			this.betaDistribution=betaDistribution;
		}

		public EmigrationParameters build() {
			EmigrationParameters emigrationParameters =  new EmigrationParameters(this);
			return emigrationParameters;
		}

	}

}

