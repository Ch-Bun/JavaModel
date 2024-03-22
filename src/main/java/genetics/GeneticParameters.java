package genetics;

import java.util.List;

public class GeneticParameters {

	private final float mutationRate;
	private final List<Integer> positions;
	private final AbstractMutationFactory factory;
	private final String geneType;
	//private final interaction; //additive, multiplicative
	
	GeneticParameters(GeneticBuilder<?> geneticBuilder){
		this.mutationRate=geneticBuilder.mutationRate;
		this.positions=geneticBuilder.positions;	
		this.factory= geneticBuilder.factory;
		this.geneType=geneticBuilder.geneType;
	}

	public float getMutationRate() {
		return mutationRate;
	}

	public  List<Integer>  getPositions() {
		return positions;
	}
	
	public AbstractMutationFactory getFactory() {
		return factory;
	}
	
	public String getGeneType() {
		return geneType;
	}

	
	public static class GeneticBuilder<T extends GeneticBuilder<T>>{
		
		private float mutationRate;
		private final List<Integer> positions;
		private final AbstractMutationFactory factory;
		private final String geneType;
		
	  public GeneticBuilder(String geneType, List<Integer> positions, AbstractMutationFactory factory){
			this.positions=positions;
			this.factory=factory;
			this.geneType=geneType;
		}
		
		public void mutationRate(float mutationRate) {
			this.mutationRate=mutationRate;
		}
		
        public GeneticParameters build() {
        	GeneticParameters geneticParameters =  new GeneticParameters(this);
            return geneticParameters;
        }
	}
	
}
