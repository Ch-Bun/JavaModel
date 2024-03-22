package genetics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.PoissonDistribution;

import FileReaders.MutationFileReader;
import FileReaders.QTFileReader;
import creature.Sex;
import global.ModelConfig;
import utils.RandomNumberGenerator;

public class Genome implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 625882070184565069L;
	private static final QTFileReader qtFileReader;
	private static final Map<Sex,Map<Trait,TraitParameters>> qtlParameters;
	private static final MutationFileReader mutationFileReader;
	private static final Map<MutationType,MutationParameters> mutationParameters;
	public static final boolean isEmpty;
	private static final PoissonDistribution recombinationDistribution;
	private static final int genomeSize;
	private static final List<Integer> chromosomeEnds;

	static {
		genomeSize = ModelConfig.CHROMOSOMESIZES
				.stream()
				.mapToInt(Integer::intValue)
				.sum();

		Integer[] arr = ModelConfig.CHROMOSOMESIZES.toArray(Integer[]::new);
		Arrays.parallelPrefix(arr, Integer::sum);
		chromosomeEnds = (genomeSize > 0) ? Arrays.asList(arr).stream().map(x -> x).toList() : null;

		qtFileReader = (genomeSize > 0) ? new QTFileReader() : null; 
		qtlParameters = (genomeSize > 0) ? qtFileReader.readQtlFile(ModelConfig.QTLPARAMETERSFILE) : null;
		mutationFileReader = (genomeSize > 0) ? new MutationFileReader() : null; 
		mutationParameters = (genomeSize > 0) ? mutationFileReader.readMutationFile(ModelConfig.MUTATIONPARAMETERSFILE) : null; 
		recombinationDistribution = 
				(genomeSize > 0) ? new PoissonDistribution(genomeSize*ModelConfig.GENOMERECOMBINATIONRATE) : null; 
		isEmpty = (genomeSize > 0) ? false : true;

	}


	private final Chromosome maternalChromosome;
	private final Chromosome paternalChromosome;

	public Genome() {

		maternalChromosome = new Chromosome(qtlParameters, mutationParameters);
		paternalChromosome =new Chromosome(qtlParameters, mutationParameters);
	}

	public Genome(Chromosome maternalChromosomes, Chromosome paternalChromosomes) {
		this.maternalChromosome = maternalChromosomes;
		this.paternalChromosome = paternalChromosomes;
	}

	public double getQTLValueFromGenome(Sex sex, Trait trait) {
		//interation type here? additive/multiplicative, assuming additive for now for qtls
		Double value = null;
		if(qtlParameters.get(sex).containsKey(trait)) {

			List<Integer> positions=qtlParameters.get(sex).get(trait).getPositions();
			value=0.0;
			for(int position:positions) {
				Mutation mutationOne = maternalChromosome.getQTLLoci(position);
				Mutation mutationTwo = paternalChromosome.getQTLLoci(position);

				if(mutationOne.equals(mutationTwo)) 
					value += mutationOne.getS() + mutationTwo.getS();
				else value += mutationOne.getS()*mutationOne.getH() + mutationTwo.getS()*mutationTwo.getH();
			}
		}
		return value;
	}


	public boolean containsQTLAllele(char allele,Sex sex, Trait trait) {

		int mutationId=CharacterMutationFactory.idForMutation(allele);
		boolean contains =false;
		if(qtlParameters.get(sex).containsKey(trait)) {

			List<Integer> positions=qtlParameters.get(sex).get(trait).getPositions();
			for(int position:positions) {
				Mutation mutationOne = maternalChromosome.getQTLLoci(position);
				Mutation mutationTwo = paternalChromosome.getQTLLoci(position);

				if(mutationOne.getId() == mutationId || mutationTwo.getId() == mutationId)
					contains =true;
			}
		}
		return contains;
	}

	private Map<Integer, Mutation>  getNewQtlMutations(Map<Integer, Mutation> mutations){

		int count = 0;
		Map<Integer, Mutation> newMutations=null;
		for(Entry<Sex,Map<Trait,TraitParameters>> gender : qtlParameters.entrySet()) {
			for(Entry<Trait, TraitParameters> qtlTrait : gender.getValue().entrySet()){
				TraitParameters traitParameters =qtlTrait.getValue();

				for(int position : traitParameters.getPositions()) {
					if(RandomNumberGenerator.zeroToOne() < traitParameters.getMutationRate()) {
						//if qtls are null then create a new map and take from original (unrecombined) chromosome
						//otherwise qtls have been filled from recombination
						if(count == 0) {
							newMutations = new HashMap<Integer,Mutation>();
							newMutations.putAll(mutations);
						}

						Mutation currentMutation = mutations.get(position); 

						Mutation newMutation = traitParameters.getFactory().mutate(currentMutation);

						newMutations.replace(position, newMutation);
						count++;
					}


				}
			}
		}
		return (newMutations != null) ? newMutations : mutations;
	}


	private Map<Integer, Mutation>  getNewMutations (Map<Integer, Mutation> mutations){

		int count = 0;
		Map<Integer, Mutation> newMutations=null;
		for(Entry<MutationType, MutationParameters> mutationT : mutationParameters.entrySet()) {
			MutationParameters mutationParameters =mutationT.getValue();
			String geneType = mutationParameters.getGeneType();
			AbstractMutationFactory factory = mutationParameters.getFactory();

			for(int position : mutationParameters.getPositions()) {
				if(RandomNumberGenerator.zeroToOne() < mutationParameters.getMutationRate()) {
					//if qtls are null then create a new map and take from original (unrecombined) chromosome
					//otherwise qtls have been filled from recombination
					if(count == 0) {
						newMutations = new HashMap<Integer,Mutation>();
						newMutations.putAll(mutations);
					}

					Mutation currentMutation = mutations.get(position); 

					Mutation newMutation = (geneType.equals("character")) ? factory.mutate(currentMutation) : factory.mutate();
					newMutations.replace(position, newMutation);
					count++;
				}


			}
		}

		return (newMutations != null) ? newMutations : mutations;
	}


	public Chromosome inherit (){


		Chromosome toInherit = (RandomNumberGenerator.getBoolean()) ? maternalChromosome : paternalChromosome;


		int events = recombinationDistribution.sample();

		Set<Integer> recomPositions = new HashSet<Integer>();

		while(recomPositions.size() < events) {
			recomPositions.add(RandomNumberGenerator.getRandomIndex(genomeSize));
		}

		recomPositions.addAll(chromosomeEnds);


		Map<Integer, Mutation>  neutralMutations = toInherit.getAllNeutralMutations(); //this could be null if no mutations yet
		Map<Integer, Mutation>  selectiveMutations = toInherit.getAllSelectiveMutations();
		Map<Integer, Mutation> qtls = toInherit.getAllQtls();

		if(recomPositions.size() > 0) {

			int neutralNo = countNeutralMutationPositions();
			int selectiveNo = countSelectiveMutationPositions();

			if(neutralNo > 0) neutralMutations =  new HashMap<Integer, Mutation>();
			if(selectiveNo > 0) selectiveMutations=  new HashMap<Integer, Mutation>();
			qtls = new HashMap<Integer,Mutation>();

			List<Integer> positionsList = new ArrayList<Integer>(recomPositions);
			Collections.sort(positionsList);

			boolean isMaternalChromosome = (RandomNumberGenerator.zeroToOne() >= 0.5) ? true : false;

			int previousPosition=0;
			for(Integer position : positionsList) {

				Chromosome currentChromosome = (isMaternalChromosome) ? maternalChromosome :  paternalChromosome;

				qtls.putAll(currentChromosome.getQtlsInRange(previousPosition, position));
				if (neutralNo > 0) neutralMutations.putAll(currentChromosome.getNeutralMutationsInRange(previousPosition, position));
				if (selectiveNo > 0) selectiveMutations.putAll(currentChromosome.getSelectiveMutationsInRange(previousPosition, position));
				previousPosition=position; //increment position, 
				isMaternalChromosome = !isMaternalChromosome; //switch chromosome
			}
		}

		//modifies qtl map if new mutations happen 
		qtls=getNewQtlMutations(qtls);
		neutralMutations = getNewMutations(neutralMutations);
		selectiveMutations =  getNewMutations(selectiveMutations);

		//if any containers have changed from the original chromosome
		if(qtls != toInherit.getAllQtls() || neutralMutations != toInherit.getAllNeutralMutations() || selectiveMutations != toInherit.getAllSelectiveMutations()) {
			//create a new chromosome
			toInherit = new Chromosome(neutralMutations,selectiveMutations,qtls);
		}
		return toInherit;
	}

	private int countNeutralMutationPositions() {
		return maternalChromosome.countNeutralMutationPositions() + 
				paternalChromosome.countNeutralMutationPositions();
	}

	private int countSelectiveMutationPositions() {
		return maternalChromosome.countSelectiveMutationPositions() + 
				paternalChromosome.countSelectiveMutationPositions();
	}

	public double calculateNeutralHeterozygosity(MutationType mutationType) {
		MutationParameters parameters = mutationParameters.get(mutationType);

		int homozygoteCount = 0;
		for(int position : parameters.getPositions()) {
                    if(maternalChromosome.getNeutralMutation(position) != null && paternalChromosome.getNeutralMutation(position) != null)	
                        if(maternalChromosome.getNeutralMutation(position).equals(paternalChromosome.getNeutralMutation(position)))
				homozygoteCount++;
		}
                double d = 1 - homozygoteCount/(double)parameters.getPositions().size();
		return d;
                
	}

	public boolean containsMutationType(MutationType mutationType) {
		MutationParameters parameters = mutationParameters.get(mutationType);
		boolean contains = false;
		List<Integer> positions = parameters.getPositions();

		contains = maternalChromosome.containsMutationAtPositions(positions);
		if(!contains)
			contains = paternalChromosome.containsMutationAtPositions(positions);

		return contains;

	}

	public int countMutations(MutationType mutationType) {
		MutationParameters parameters = mutationParameters.get(mutationType);
		int count=0;
		List<Integer> positions = parameters.getPositions();
		count += maternalChromosome.countNeutralMutations(positions);
		count += paternalChromosome.countNeutralMutations(positions);
		return count;
	}

	public boolean isHomozygote(MutationType mutationType) {
		int count = countMutations(mutationType);
		if(count == 2)
			return true;
		return false;
	}

}
