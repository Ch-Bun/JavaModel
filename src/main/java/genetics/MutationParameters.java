package genetics;

import java.util.List;

public class MutationParameters extends GeneticParameters{

	private final boolean isNeutral;
	
	MutationParameters(MutationBuilder mutationBuilder){
		super(mutationBuilder);
		this.isNeutral=mutationBuilder.isNeutral;
	}

	public boolean isNeutral() {
		return isNeutral;
	}
	
	public static class MutationBuilder extends GeneticParameters.GeneticBuilder<MutationBuilder>{

		private final boolean isNeutral;
		
		public MutationBuilder(boolean isNeutral, String geneType,  List<Integer> positions, AbstractMutationFactory factory){
			super(geneType, positions, factory);
			this.isNeutral=isNeutral;
		}

		public MutationParameters build() {return new MutationParameters(this);}
	}


}
