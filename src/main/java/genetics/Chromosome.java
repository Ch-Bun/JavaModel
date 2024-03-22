package genetics;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import creature.Sex;
import utils.RandomNumberGenerator;

public class Chromosome implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -106957865598717011L;
	//count of this chromosome in population 
	private final Map<Integer, Mutation>  neutralMutations;
	private final Map<Integer, Mutation>  selectiveMutations;
	private final Map<Integer, Mutation> qtls;

	//This will set up at intialisation, should only be called then, selective containers remain empty as this is like SNPS with wildtype and muts(snps) on top
	public Chromosome(Map<Sex, Map<Trait, TraitParameters>> qtlparameters, Map<MutationType,MutationParameters> mutationParameters) {

		qtls=initialiseQTLs(qtlparameters);
		neutralMutations = intialiseNeutral(mutationParameters);
		selectiveMutations = Collections.emptyMap();

	}
	//kinda crap to loop over qtl and neutrals for every chromosome but to do single loop over qtl and extract chromosome would leave chromosomes mutable 
	private	Map<Integer, Mutation>  initialiseQTLs(Map<Sex, Map<Trait, TraitParameters>> qtlparameters){

		Map<Integer, Mutation> qtlsTemp = new HashMap<Integer,Mutation>();
		for(Entry<Sex,  Map<Trait, TraitParameters>> sex : qtlparameters.entrySet()){
			for(Entry<Trait, TraitParameters> paramMap : sex.getValue().entrySet()) {
				TraitParameters param = paramMap.getValue();
				List<Integer> positions = param.getPositions();

				for(int position : positions) {
					if(qtlsTemp.get(position)==null) {
						Mutation mutation = param.getFactory().create();
						qtlsTemp.put(position, mutation);
					}
				}
			}
		}
		return Map.copyOf(qtlsTemp);
	}

	private  Map<Integer, Mutation> intialiseNeutral(Map<MutationType,MutationParameters> mutationParameters) {

		Map<Integer, Mutation> neutralMuts = new HashMap<Integer, Mutation>();
		for(Entry<MutationType,MutationParameters> mutationMap : mutationParameters.entrySet()) {
			MutationParameters params = mutationMap.getValue();
			String geneType = params.getGeneType();

			if(!geneType.equals("double") && params.isNeutral()) {

				List<Integer> positions = params.getPositions();

				for(int position : positions) {
					if(RandomNumberGenerator.zeroToOne() < 0.89) //HACK to create binary with presence or absence of character rather than two characters, reduce memory
                                            neutralMuts.put(position, params.getFactory().create());
				}

			}
		}

		return (neutralMuts.size() > 0) ? Map.copyOf(neutralMuts) : Collections.emptyMap();
	}

	//new Chromosome for inheritance after mutation and/or recombination, copyOf creates immutable maps 
	public Chromosome(Map<Integer, Mutation>  neutralMutations, Map<Integer, Mutation>  selectiveMutations, Map<Integer, Mutation> qtls) {

		this.qtls=Map.copyOf(qtls);
		this.selectiveMutations=Map.copyOf(selectiveMutations);
		this.neutralMutations=Map.copyOf(neutralMutations);
	}

	public Mutation getQTLLoci(int position) {
		return qtls.get(position);
	}

	public Map<Integer, Mutation> getQtlsInRange(int min, int maxA){

		Map<Integer, Mutation> filteredMap = qtls.entrySet()
				.stream()
				.filter(x->x.getKey() >= min)
				.filter(x->x.getKey() < maxA)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return filteredMap;
	}

	public Map<Integer, Mutation> getNeutralMutationsInRange(int min, int maxA){

		Map<Integer, Mutation> filteredMap = neutralMutations.entrySet()
				.stream()
				.filter(x->x.getKey() >= min)
				.filter(x->x.getKey() < maxA)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return filteredMap;
	}
	
	public boolean containsMutationAtPositions(List<Integer> positions){
		for(int position : positions) {
			if(neutralMutations.get(position) != null)
				return true;
		}
		return false;
		
	}

	public Map<Integer, Mutation> getSelectiveMutationsInRange(int min, int maxA){

		Map<Integer, Mutation> filteredMap = selectiveMutations.entrySet()
				.stream()
				.filter(x->x.getKey() >= min)
				.filter(x->x.getKey() < maxA)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return filteredMap;
	}

	public Map<Integer, Mutation> getAllNeutralMutations(){
		return neutralMutations;
	}
	
	public Mutation getNeutralMutation(int position) {
		return neutralMutations.get(position);
	}

	//warning this is not an actual count as doesn't count within the list, just positions with at least one mutation
	public int countNeutralMutationPositions(){
		return neutralMutations.size();
	}

	public int countSelectiveMutationPositions(){
		return selectiveMutations.size();
	}
	
	public int countNeutralMutations(List<Integer> positions) {
		int count =0;
		for(int position:positions) {
		if(neutralMutations.get(position) != null)
			count++;
		}
		return count;
	}

	public Map<Integer, Mutation> getAllSelectiveMutations(){
		return selectiveMutations;
	}

	public Map<Integer, Mutation> getAllQtls(){
		return qtls;
	}
}
