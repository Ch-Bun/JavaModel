package genetics;

import java.util.List;


public class TraitParameters extends GeneticParameters {
	
	TraitParameters(TraitBuilder traitBuilder){
		super(traitBuilder);
	}

	public static class TraitBuilder extends GeneticParameters.GeneticBuilder<TraitBuilder>{

	  public TraitBuilder(String geneType, List<Integer> positions, AbstractMutationFactory factory){
			super(geneType,positions, factory);
		}
		
        public TraitParameters build() {return new TraitParameters(this);}
	}
	
}
