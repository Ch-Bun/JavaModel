package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import creature.Sex;
import creature.Stage;
import dispersal.EmigrationType;
import dispersal.DensityDependentEmigration;
import dispersal.DensityIndependentEmigration;
import dispersal.EmigrationParameters;
import dispersal.EmigrationParameters.EmigrationBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;

public class EmigrationFileReader extends AbstractFileReader implements DispersalFileReader<EmigrationType> {

	private int sexColumnNo,densityDependantColumnNo, stageIdColumnNo, dOColumnNo, evolvingColumnNo, alphaColumnNo,betaColumnNo,
	dOParametersColumnNo,alphaParametersColumnNo, betaParametersColumnNo;

	public EmigrationFileReader() { 
		super(10); 
	}


	@Override
	public Map<Stage, Map<Sex, EmigrationType>> readDispersalFile(String filename) {

		LogWriter.println("reading stage specific emigration characteristics file: " + filename);

		 Map<Stage, Map<Sex, EmigrationType>> emigrationCharacteristics = new HashMap<Stage, Map<Sex, EmigrationType>>();
		
		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			try {
				sexColumnNo = colNameMap.get("Sex");
				stageIdColumnNo = colNameMap.get("StageId");
				densityDependantColumnNo = colNameMap.get("DensityDependent");
				evolvingColumnNo = colNameMap.get("Evolving");
				dOColumnNo = colNameMap.get("D0Distribution");
				alphaColumnNo = colNameMap.get("AlphaDistribution");
				betaColumnNo = colNameMap.get("BetaDistribution");
				dOParametersColumnNo = colNameMap.get("D0Parameters");
				alphaParametersColumnNo = colNameMap.get("AlphaParameters");
				betaParametersColumnNo = colNameMap.get("BetaParameters");
			}catch(Exception e) {
				throw new RuntimeException("Emigration stages file, error in column names. Hint: Check column spelling and lower/upper case");
			}

			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				//these must all be set in file
				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);
				Stage stage = Stage.getFromCache(Integer.parseInt(tokens[stageIdColumnNo]));
				Boolean evolving =  Boolean.parseBoolean(tokens[evolvingColumnNo]);
				Boolean densityDependant = Boolean.parseBoolean(tokens[densityDependantColumnNo]);

				EmigrationBuilder emigrationBuilder = new EmigrationParameters.
						EmigrationBuilder(densityDependant,evolving);

				try {
					if(!evolving) {
						DistributionSampler doDistribution = whichDistribution(tokens[dOColumnNo],
								convertStringList(tokens[dOParametersColumnNo]), filename);
						emigrationBuilder.doDistribution(doDistribution);
						if(densityDependant) {
							DistributionSampler alphaDistribution = whichDistribution(tokens[alphaColumnNo],
								convertStringList(tokens[alphaParametersColumnNo]), filename);
						emigrationBuilder.alphaDistribution(alphaDistribution);
						
						DistributionSampler betaDistribution = whichDistribution(tokens[betaColumnNo],
								convertStringList(tokens[betaParametersColumnNo]), filename);
						emigrationBuilder.betaDistribution(betaDistribution);
						}
					}
				}catch(Exception e) {
					throw new RuntimeException("Problem setting distribution/value for non-evolving emigration trait values, check no null values are specified for a row where 'evolving' is set to false");
				}

				EmigrationParameters emigrationParameters=emigrationBuilder.build();

				EmigrationType emigrationType = (densityDependant) ? new DensityDependentEmigration(emigrationParameters) : 
					new DensityIndependentEmigration(emigrationParameters);

				Map<Sex, EmigrationType> genderStages = emigrationCharacteristics.get(stage);

				if(genderStages==null) {
					genderStages=new HashMap<Sex,EmigrationType>();
					emigrationCharacteristics.put(stage, genderStages);
				}

				if(genderStages.get(sex) != null) 
					throw new RuntimeException("More than one row in emigration file for stage " + 
							stage.getStageName() + " and sex "+ sex);

				genderStages.put(sex, emigrationType);

			}
		}
		catch (IOException e) {
			LogWriter.printlnError("Emigration stages file: Failed in reading file. Check column spelling and lower/upper case " + filename);
			LogWriter.print(e);
		}
		
		return Map.copyOf(emigrationCharacteristics);
	}

}
