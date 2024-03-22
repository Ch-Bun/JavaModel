package genetics;

import utils.DistributionSampler;

public class DoubleMutationFactory implements  AbstractMutationFactory{

	private final DistributionSampler dominanceDistribution;
	private final DistributionSampler mutationDistribution;
	private final DistributionSampler initialDistribution;
	
	public DoubleMutationFactory( DistributionSampler initialDistribution,DistributionSampler dominanceDistribution,DistributionSampler mutationDistribution){
		this.dominanceDistribution=dominanceDistribution;
		this.mutationDistribution=mutationDistribution;
		this.initialDistribution=initialDistribution;
	}
	
	@Override
	public DoubleMutation mutate(Mutation mutation) {
		double s = mutationDistribution.sample() + mutation.getS();
		double h = mutation.getH();
		return new DoubleMutation(s,h);
	}

	@Override
	public DoubleMutation mutate() {
		return new DoubleMutation(mutationDistribution.sample(),dominanceDistribution.sample());
	}

	@Override
	public DoubleMutation create() {
		return new DoubleMutation(initialDistribution.sample(),dominanceDistribution.sample());
	}


}