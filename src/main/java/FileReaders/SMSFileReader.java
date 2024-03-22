package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import creature.Sex;
import creature.Stage;
import dispersal.TransferType;
import dispersal.SMSTransfer;
import dispersal.SMSTransferParameters;
import dispersal.SMSTransferParameters.SMSTransferBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;

public class SMSFileReader extends AbstractFileReader implements DispersalFileReader<TransferType> {

	private int sexColumnNo, stageIdColumnNo, evolvingColumnNo, perStepMortalityColumnNo,maxStepsPerDispersalEventColumnNo,
	goalBiasColumnNo,dpDistributionColumnNo,dpParametersColumnNo,memSizeColumnNo,gbDistributionColumnNo,
	gbParametersColumnNo,alphaDistributionColumnNo,alphaParametersColumnNo,betaDistributionColumnNo,betaParametersColumnNo;

	public SMSFileReader() { 
		super(15);
	}

	public  Map<Stage, Map<Sex, TransferType>>  readDispersalFile(String filename) {

		LogWriter.println("reading stage specific SMS transfer characteristics file: " + filename);

		Map<Stage, Map<Sex, TransferType>> transferCharacteristics=new  HashMap<Stage, Map<Sex, TransferType>>();

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			try {
				sexColumnNo = colNameMap.get("Sex");
				stageIdColumnNo = colNameMap.get("StageId");
				evolvingColumnNo = colNameMap.get("Evolving");
				memSizeColumnNo = colNameMap.get("MemSize");
				perStepMortalityColumnNo = colNameMap.get("PerStepMortality");
				maxStepsPerDispersalEventColumnNo = colNameMap.get("MaxStepsPerDispersalEvent");
				dpDistributionColumnNo = colNameMap.get("DPDistribution");
				dpParametersColumnNo = colNameMap.get("DPParameters");
				goalBiasColumnNo = colNameMap.get("GoalBias");
				gbDistributionColumnNo = colNameMap.get("GBDistribution");
				gbParametersColumnNo = colNameMap.get("GBParameters");
				alphaDistributionColumnNo = colNameMap.get("AlphaDistribution");
				alphaParametersColumnNo = colNameMap.get("AlphaParameters");
				betaDistributionColumnNo = colNameMap.get("BetaDistribution");
				betaParametersColumnNo = colNameMap.get("BetaParameters");

			}catch(Exception e) {
				throw new RuntimeException("sms stages file, error in column names. Hint: Check column spelling and lower/upper case");
			}


			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);
				Stage stage =Stage.getFromCache(Integer.parseInt(tokens[stageIdColumnNo]));
				boolean evolving = Boolean.parseBoolean(tokens[evolvingColumnNo]);

				int memSize = (this.noDataValue.equals(tokens[memSizeColumnNo])) ? 1 : Integer.parseInt(tokens[memSizeColumnNo]);
				short maxStepsPerDispersalEvent = (this.noDataValue.equals(tokens[maxStepsPerDispersalEventColumnNo])) ? 1000 : Short.parseShort(tokens[maxStepsPerDispersalEventColumnNo]);

				
				boolean goalBias = Boolean.parseBoolean(tokens[goalBiasColumnNo]);
				
				double perStepMortality = Double.parseDouble(tokens[perStepMortalityColumnNo]);

				SMSTransferBuilder smsBuilder = new SMSTransferParameters.
						SMSTransferBuilder(memSize, goalBias, perStepMortality, maxStepsPerDispersalEvent);

				try {
					if(!evolving) {
						DistributionSampler dpDistribution = whichDistribution(tokens[dpDistributionColumnNo],
								convertStringList(tokens[dpParametersColumnNo]), filename);
						smsBuilder.dpDistribution(dpDistribution);

						if(goalBias) {

							DistributionSampler gbDistribution = whichDistribution(tokens[gbDistributionColumnNo],
									convertStringList(tokens[gbParametersColumnNo]), filename);
							smsBuilder.gbDistribution(gbDistribution);

							DistributionSampler alphaDistribution = whichDistribution(tokens[alphaDistributionColumnNo],
									convertStringList(tokens[alphaParametersColumnNo]), filename);
							smsBuilder.alphaDistribution(alphaDistribution);

							DistributionSampler betaDistribution = whichDistribution(tokens[betaDistributionColumnNo],
									convertStringList(tokens[betaParametersColumnNo]), filename);
							smsBuilder.betaDistribution(betaDistribution);
						}
					}
				}catch(Exception e) {
					throw new RuntimeException("Problem setting distribution/value for non-evolving sms trait values, check no null values are specified for a row where 'evolving' is set to false");
				}
				SMSTransferParameters smsParameters=smsBuilder.build();
				SMSTransfer smsType= new SMSTransfer(smsParameters);

				Map<Sex, TransferType> genderStages = transferCharacteristics.get(stage);

				if(genderStages==null) {
					genderStages=new HashMap<Sex,TransferType>();
					transferCharacteristics.put(stage, genderStages);
				}

				if(genderStages.get(sex) != null) 
					throw new RuntimeException("More than one row in sms file for stage " + 
							stage.getStageName() + " and sex "+ sex);

				genderStages.put(sex, smsType);
			}
		}
		catch (IOException e) {
			LogWriter.printlnError("sms stages file: Failed in reading file. " + filename);
			LogWriter.print(e);
		}
		return Map.copyOf(transferCharacteristics);
	}

}
