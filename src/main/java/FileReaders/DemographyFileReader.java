package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import creature.Sex;
import creature.Stage;
import lifecycle.DemographyType;
import lifecycle.DemographyType.DemographyBuilder;
import utils.AbstractFileReader;
import utils.LogWriter;

public class DemographyFileReader extends AbstractFileReader {

	public DemographyFileReader(){
		super(8); 
	}

	public Map<Stage, Map<Sex, DemographyType>> readDemographyFile(String filename) {

		LogWriter.println("reading stage specific demography file: " + filename);

		Map<Stage,Map<Sex,DemographyType>> demography = new HashMap<Stage,Map<Sex,DemographyType>>();

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {

			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			int sexColumnNo = colNameMap.get("Sex");
			int stageIdColumnNo = colNameMap.get("StageId");
			int minAgeColumnNo = colNameMap.get("MinAge");
			int maxAgeColumnNo = colNameMap.get("MaxAge");
			int fecundityColumnNo = colNameMap.get("Fecundity");
			int survivalColumnNo = colNameMap.get("SurvivalProbability");
			int transitionColumnNo = colNameMap.get("DevelopmentProbability");
//			int fecundityDDColumnNo = colNameMap.get("FecundityDD");
//			int survivalDDColumnNo = colNameMap.get("SurvivalDD");
//			int transitionDDColumnNo = colNameMap.get("DevelopmentDD");
			int dispersesColumnNo = colNameMap.get("Disperses");

			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				int stageId =Integer.parseInt(tokens[stageIdColumnNo]);
				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);
				Stage stage = Stage.getFromCache(stageId);
				Integer minAge = Integer.parseInt(tokens[minAgeColumnNo]);
				Integer maxAge = Integer.parseInt(tokens[maxAgeColumnNo]);
				Double survival = Double.parseDouble(tokens[survivalColumnNo]);
				Double transition = Double.parseDouble(tokens[transitionColumnNo]);
				boolean disperses = Boolean.parseBoolean(tokens[dispersesColumnNo]);

				if(survival + transition > 1) {
					String e = "Survival and transition probabilities for stage " + 
							stage + " cannot sum to greater than 1";
					LogWriter.printlnError(e);
					throw new RuntimeException(e);
				}


				DemographyBuilder demographyBuilder = new DemographyType.
						DemographyBuilder(minAge, maxAge, survival, transition, disperses);

				if(!this.noDataValue.equals(tokens[fecundityColumnNo])) {
					demographyBuilder.fecundity(Double.parseDouble(tokens[fecundityColumnNo]));
					if(stageId == 0 ) 
						throw new IOException("the first life stage (0) cannot be fecund. Use # in file instead.");
				
				}
				else demographyBuilder.fecundity(0.0);
//				if(!this.noDataValue.equals(tokens[survivalDDColumnNo]))
//					demographyBuilder.survivalDD(Double.parseDouble(tokens[survivalDDColumnNo]));
//
//				if(!this.noDataValue.equals(tokens[transitionDDColumnNo]))
//					demographyBuilder.transitionDD(Double.parseDouble(tokens[transitionDDColumnNo]));
//
//				if(!this.noDataValue.equals(tokens[fecundityDDColumnNo]))
//					demographyBuilder.fecundityDD(Double.parseDouble(tokens[fecundityDDColumnNo]));

				DemographyType demographyCharacteristics = demographyBuilder.build();

				Map<Sex, DemographyType> genderStages = demography.get(stage);

				if(genderStages==null) {
					genderStages=new HashMap<Sex,DemographyType>();
					demography.put(stage, genderStages);
				}

				if(genderStages.get(sex) != null) 
					throw new RuntimeException("More than one row in demography file for stage " + 
							stage + " and sex "+ sex);

				genderStages.put(sex, demographyCharacteristics);
			}
		}
		catch (IOException e) {
			LogWriter.printlnError("Demography file: Failed in reading file. Check column names spelling and lower/upper case " + filename);
			LogWriter.print(e);
		}

		return Map.copyOf(demography);
	}

}
