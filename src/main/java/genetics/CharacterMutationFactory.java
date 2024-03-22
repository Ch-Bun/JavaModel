package genetics;


import java.util.List;
import java.util.NavigableMap;

import java.util.TreeMap;

import utils.DistributionSampler;
import utils.LogWriter;


public class CharacterMutationFactory implements AbstractMutationFactory{

	//nb this means that cannot have a neutral and a qtl with dominance allele 'a' 
	private static NavigableMap<Character, CharacterMutation> mutationMap = new TreeMap<Character,CharacterMutation>();
	private final List<Character> alleleCharacters;
	private final DistributionSampler mutationDistribution; //either +1 or random A-Z

	public CharacterMutationFactory( List<Character> alleleCharacters, DistributionSampler mutationDistribution){
		this.alleleCharacters=alleleCharacters;
		this.mutationDistribution=mutationDistribution;
	}
	
	public static void add(char letter, double s, double h) {
		CharacterMutation mutation = mutationMap.get(letter);
		if(mutation == null) {
			mutation = new CharacterMutation(s, h, letter);
			mutationMap.put(letter, mutation);
		}
		else LogWriter.printlnWarning("Already a character allele value for " + letter + " first value will be used");
	}
	
	private static CharacterMutation add(char c) {
		CharacterMutation mutation = mutationMap.get(c);
		if(mutation == null) {
			mutation = new CharacterMutation(c);
			mutationMap.put(c, mutation);
		}
		return mutation;
	}

	@Override
	public CharacterMutation mutate(Mutation original) {
		CharacterMutation newMutation;
		switch(mutationDistribution.distributionType()) {
		case "stepChar" :
			newMutation = stepMutate(original);
			break;
		case "uniformChar" :
			newMutation = uniformMutate();
			break;
		default:
			newMutation = null;
			LogWriter.printlnError("Cannot mutate character alleles, check spelling of mutation distribution type in files");
		
		}
		
		return newMutation;
	}
	
	
	private CharacterMutation uniformMutate() {

		int index = (int) mutationDistribution.sample();
		char allele = alleleCharacters.get(index);
		return mutationMap.get(allele);	
	}
	
	private CharacterMutation stepMutate(Mutation original) {
		
		char currentAllele = (char) original.getAllele();
		
		char newAllele = (mutationDistribution.sample() > 0) ? currentAllele++ : currentAllele--;
		
		CharacterMutation newMutation = add(newAllele);
		return newMutation;
		
	}
	
	public static int idForMutation(char allele) {
		return mutationMap.get(allele).getId();
	}
	
	//never used as character allele types are either QTL or microsats so always initialised in genome
	@Override
	public CharacterMutation mutate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharacterMutation create() {
		return uniformMutate();
	}

}
