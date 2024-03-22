package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import genetics.AbstractMutationFactory;
import genetics.CharacterMutationFactory;
import genetics.DoubleMutationFactory;
import genetics.MutationParameters;
import genetics.MutationType;
import genetics.MutationParameters.MutationBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;
import utils.RandomNumberGenerator;

public class MutationFileReader extends AbstractFileReader {

	public MutationFileReader() {
		super(10);
		// TODO Auto-generated constructor stub
	}


	public Map<MutationType,MutationParameters> readMutationFile(String filename) {

		LogWriter.println("reading mutation types file: " + filename);

		Map<MutationType,MutationParameters>  mutationParametersMap = new HashMap<MutationType,MutationParameters> ();

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {


			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			int mutationColumnNo = colNameMap.get("MutationName");
			int geneTypeColumnNo = colNameMap.get("GeneType");
			int isNeutralColumnNo = colNameMap.get("Neutral");
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
			
				MutationType mutationType = MutationType.newInstance(tokens[mutationColumnNo]);
				Boolean isNeutral = Boolean.parseBoolean(tokens[isNeutralColumnNo]);
				String geneType = tokens[geneTypeColumnNo];
				Float mutationRate = Float.parseFloat(tokens[mutationRateColumnNo]);
				List<Integer> positions = convertPositionsList(tokens[positionsColumnNo]);

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
				case "microsatellite":
					Set<Character> alleleSet = new HashSet<Character>();
					while(alleleSet.size() < positions.size()){
						alleleSet.add(RandomNumberGenerator.sampleAlphabet());
					}
					addMicrosats(alleleSet);
					mutationDistribution = whichDistribution(tokens[mutationDistColumnNo],
							convertStringList(tokens[mutationParamsColumnNo]),filename);
					
					factory=new CharacterMutationFactory(List.copyOf(alleleSet), mutationDistribution);
					
				default :
					factory=null;
					LogWriter.printlnError("No gene type specified for mutation type" + mutationType);
				}

				MutationBuilder mutationBuilder = new MutationParameters.
						MutationBuilder(isNeutral,geneType, positions, factory);

				Optional.ofNullable(mutationRate)
				.ifPresent(mutationBuilder::mutationRate);
				
				MutationParameters mutationParameters = mutationBuilder.build();

				if(mutationParametersMap.get(mutationType) != null) 
					throw new RuntimeException("More than one row in file " +filename + " for mutation " + 
							mutationType.toString());

				mutationParametersMap.put(mutationType, mutationParameters);
			}
		}
		catch (Exception e) {
			LogWriter.printlnError("Mutation file: Failed in reading file "+ filename +" Check column names spelling and lower/upper case ");
			LogWriter.print(e);
		}

		//returns unmodifiable map using copyOf
		return Map.copyOf(mutationParametersMap);		
	}
}