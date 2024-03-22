package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import creature.Sex;
import genetics.AbstractMutationFactory;
import genetics.CharacterMutationFactory;
import genetics.DoubleMutationFactory;
import genetics.Trait;
import genetics.TraitParameters;
import genetics.TraitParameters.TraitBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;

public class QTFileReader extends AbstractFileReader {

	public QTFileReader() {
		super(11);
		// TODO Auto-generated constructor stub
	}

	public Map<Sex, Map<Trait, TraitParameters>> readQtlFile(String filename) {

		LogWriter.println("reading quantitative traits file: " + filename);

		Map<Sex,Map<Trait,TraitParameters>> qtlTraits= new HashMap<Sex,Map<Trait,TraitParameters>>();

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {

			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			int traitColumnNo = colNameMap.get("Trait");
			int geneTypeColumnNo = colNameMap.get("GeneType");
			int sexColumnNo = colNameMap.get("Sex");
			int intialDistColumnNo = colNameMap.get("InitialDistribution");
			int initialParamsColumnNo = colNameMap.get("InitialParameters");
			int dominanceDistColumnNo = colNameMap.get("DominanceDistribution");
			int dominanceParamsColumnNo = colNameMap.get("DominanceParameters");
			int mutationDistColumnNo = colNameMap.get("MutationDistribution");
			int mutationParamsColumnNo = colNameMap.get("MutationParameters");
			int mutationRateColumnNo = colNameMap.get("MutationRate");
			int positionsColumnNo = colNameMap.get("Positions");
			
			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				Trait trait = Trait.getForName(tokens[traitColumnNo]);
				String geneType = tokens[geneTypeColumnNo];
				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);

				AbstractMutationFactory factory;
				DistributionSampler mutationDistribution;


				switch(geneType) {
				case "character":
					Map<String,Float> initialParams=convertStringList(tokens[initialParamsColumnNo]);
					addCharacterMutation(initialParams, convertStringList(tokens[dominanceParamsColumnNo]));
					Set<String> stringSet = initialParams.keySet();
					List<Character> alleleList = new ArrayList<Character>();
					for(String allele : stringSet) {
						alleleList.addAll(allele.chars().mapToObj(ch -> (char) ch).collect(Collectors.toList()));
					}
					mutationDistribution = whichDistribution(tokens[mutationDistColumnNo],
							initialParams,filename);
					factory=new CharacterMutationFactory(List.copyOf(alleleList), mutationDistribution);
					break;
				case "double" :
					mutationDistribution = whichDistribution(tokens[mutationDistColumnNo],
							convertStringList(tokens[mutationParamsColumnNo]),filename);
					DistributionSampler initialDistribution = whichDistribution(tokens[intialDistColumnNo],
							convertStringList(tokens[initialParamsColumnNo]),filename);
					DistributionSampler dominanceDistribution = whichDistribution(tokens[dominanceDistColumnNo],
							convertStringList(tokens[dominanceParamsColumnNo]),filename);
					factory = new DoubleMutationFactory(mutationDistribution,initialDistribution,dominanceDistribution);
					break;
				default :
					factory=null;
					LogWriter.printlnError("No mutation type specified for QTL" + trait);
				}



				Float mutationRate = Float.parseFloat(tokens[mutationRateColumnNo]);
				List<Integer> positions = convertPositionsList(tokens[positionsColumnNo]);



				TraitBuilder traitBuilder = new TraitParameters.
						TraitBuilder(geneType, positions,  factory);

				Optional.ofNullable(mutationRate)
				.ifPresent(traitBuilder::mutationRate);


				//get factory from FactoryProvider
				TraitParameters traitParameters = traitBuilder.build();


				Map<Trait,TraitParameters> parameters= qtlTraits.get(sex);

				if(parameters==null) {
					parameters=new HashMap<Trait,TraitParameters>();
					qtlTraits.put(sex, parameters);
				}

				if(parameters.get(trait) != null) 
					throw new RuntimeException("More than one row in file " +filename + " for trait " + 
							trait.toString() + " and sex "+ sex);

				parameters.put(trait, traitParameters);
			}
		}
		catch (IOException e) {
			LogWriter.printlnError("Traits file: Failed in reading file "+ filename +" Check column names spelling and lower/upper case " + filename);
			LogWriter.print(e);
		}

		//returns unmodifiable map using copyOf
		return Map.copyOf(qtlTraits);		
	}



}
