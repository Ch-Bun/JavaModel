package FileReaders;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import creature.Sex;
import creature.Stage;
import dispersal.Settlement;
import dispersal.SettlementParameters;
import dispersal.SettlementParameters.SettlementBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;

public class SettlementFileReader extends AbstractFileReader implements DispersalFileReader<Settlement>{

	private int sexColumnNo,stageIdColumnNo,densityDependantColumnNo,
	evolvingColumnNo,findMateColumnNo,soDistributionColumnNo,soParametersColumnNo,
	alphaDistributionColumnNo,alphaParametersColumnNo,betaDistributionColumnNo,
	betaParametersColumnNo,probabilityOfUnsettlingColumnNo;

	public SettlementFileReader() {
		super(12);
	}

	public Map<Stage, Map<Sex, Settlement>> readDispersalFile(String filename) {

		LogWriter.println("reading stage specific settlement characteristics file: " + filename);

		Map<Stage, Map<Sex, Settlement>> settlementCharacteristics = new HashMap<Stage, Map<Sex, Settlement>>();
		
		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			try {
				sexColumnNo = colNameMap.get("Sex");
				stageIdColumnNo = colNameMap.get("StageId");
				densityDependantColumnNo = colNameMap.get("DensityDependent");
				evolvingColumnNo = colNameMap.get("Evolving");
				findMateColumnNo = colNameMap.get("FindMate");
				probabilityOfUnsettlingColumnNo = colNameMap.get("ProbabilityUnsettle");
				soDistributionColumnNo = colNameMap.get("SODistribution");
				soParametersColumnNo = colNameMap.get("SOParameters");
				alphaDistributionColumnNo = colNameMap.get("AlphaDistribution");
				alphaParametersColumnNo = colNameMap.get("AlphaParameters");
				betaDistributionColumnNo = colNameMap.get("BetaDistribution");
				betaParametersColumnNo = colNameMap.get("BetaParameters");

			}catch(Exception e) {
				throw new RuntimeException("Settlement stages file, error in column names. Hint: Check column spelling and lower/upper case");
			}
			

			
			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);
				Stage stage = Stage.getFromCache(Integer.parseInt(tokens[stageIdColumnNo]));
				Boolean evolving = (this.noDataValue.equals(tokens[evolvingColumnNo])) ? false : Boolean.parseBoolean(tokens[evolvingColumnNo]);	
				Boolean densityDep = (this.noDataValue.equals(tokens[densityDependantColumnNo])) ? false : Boolean.parseBoolean(tokens[densityDependantColumnNo]);	
				Boolean findMate = (this.noDataValue.equals(tokens[findMateColumnNo])) ? false : Boolean.parseBoolean(tokens[findMateColumnNo]);			
				Double probUnsettling = (this.noDataValue.equals(tokens[probabilityOfUnsettlingColumnNo])) ? 0.0 : Double.parseDouble(tokens[probabilityOfUnsettlingColumnNo]);
				
				SettlementBuilder settlementBuilder = new SettlementBuilder(findMate,probUnsettling);

				try {
					if(!evolving) {
						DistributionSampler soDistribution = whichDistribution(tokens[soDistributionColumnNo],
								convertStringList(tokens[soParametersColumnNo]), filename);
						settlementBuilder.sODistribution(soDistribution);


						if(densityDep) {
						
							DistributionSampler alphaDistribution = whichDistribution(tokens[alphaDistributionColumnNo],
									convertStringList(tokens[alphaParametersColumnNo]), filename);
							settlementBuilder.alphaDistribution(alphaDistribution);

							DistributionSampler betaDistribution = whichDistribution(tokens[betaDistributionColumnNo],
									convertStringList(tokens[betaParametersColumnNo]), filename);
							settlementBuilder.betaDistribution(betaDistribution);
						}
					}
				}catch(Exception e) {
					throw new RuntimeException("Problem setting distribution/value for non-evolving settlement trait values, check no null values are specified for a row where 'evolving' is set to false");
				}
				
				
				
			

				SettlementParameters smsSettlementParameters =settlementBuilder.build();
				Settlement smsSettlement= new Settlement(smsSettlementParameters);

				Map<Sex, Settlement> genderStages = settlementCharacteristics.get(stage);

				if(genderStages==null) {
					genderStages=new HashMap<Sex,Settlement>();
					settlementCharacteristics.put(stage, genderStages);
				}

				if(genderStages.get(sex) != null) 
					throw new RuntimeException("More than one row in settlement file for stage " + 
							stage.getStageName() + " and sex "+ sex);

				genderStages.put(sex, smsSettlement);
			}
		}
		catch (IOException e) {
			LogWriter.printlnError("Settlement stages file: Failed in reading file. Check column spelling and lower/upper case " + filename);
			LogWriter.print(e);
		}
		
		return Map.copyOf(settlementCharacteristics);
	}


}
