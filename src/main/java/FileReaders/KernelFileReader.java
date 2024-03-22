package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import creature.Sex;
import creature.Stage;
import dispersal.DispersalKernelParameters;
import dispersal.DispersalKernelParameters.DispersalKernelBuilder;
import dispersal.DispersalKernelTransfer;
import dispersal.SMSTransfer;
import dispersal.SMSTransferParameters;
import dispersal.TransferType;
import dispersal.SMSTransferParameters.SMSTransferBuilder;
import utils.AbstractFileReader;
import utils.DistributionSampler;
import utils.LogWriter;

public class KernelFileReader  extends AbstractFileReader implements DispersalFileReader<TransferType> {

	private int sexColumnNo, stageIdColumnNo, evolvingColumnNo,kernelDistributionColumnNo, kernelParametersColumnNo, distDepMortalityColumnNo,
	mortalityRateColumnNo,mortalitySlopeColumnNo,mortalityInflectionColumnNo;


	public KernelFileReader() {
		super(9);
		// TODO Auto-generated constructor stub
	}


	public  Map<Stage, Map<Sex, TransferType>>  readDispersalFile(String filename) {

		LogWriter.println("reading stage specific kernel transfer characteristics file: " + filename);

		Map<Stage, Map<Sex, TransferType>> transferCharacteristics=new  HashMap<Stage, Map<Sex, TransferType>>();

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			try {
				sexColumnNo = colNameMap.get("Sex");
				stageIdColumnNo = colNameMap.get("StageId");
				evolvingColumnNo = colNameMap.get("Evolving");
				kernelDistributionColumnNo = colNameMap.get("KernelDistribution");
				kernelParametersColumnNo = colNameMap.get("KernelParameters");
				distDepMortalityColumnNo = colNameMap.get("DistDependantMortality");
				mortalityRateColumnNo= colNameMap.get("MortalityRate");
				mortalitySlopeColumnNo = colNameMap.get("MortalitySlope");
				mortalityInflectionColumnNo= colNameMap.get("MortalityInflection");

			}catch(Exception e) {
				throw new RuntimeException("kernel stages file, error in column names. Hint: Check column spelling and lower/upper case");
			}


			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				Sex sex = Sex.getFromCache(tokens[sexColumnNo]);
				Stage stage =Stage.getFromCache(Integer.parseInt(tokens[stageIdColumnNo]));
				Boolean evolving = Boolean.parseBoolean(tokens[evolvingColumnNo]);
				Boolean distDepMortality = Boolean.parseBoolean(tokens[distDepMortalityColumnNo]);

				DistributionSampler kernelDistribution = whichDistribution(tokens[kernelDistributionColumnNo],
						convertStringList(tokens[kernelParametersColumnNo]), filename);

				DispersalKernelBuilder kernelBuilder = new DispersalKernelParameters.
						DispersalKernelBuilder(kernelDistribution, distDepMortality);

				if(distDepMortality) {

					Double mortalitySlope = Double.parseDouble(tokens[mortalitySlopeColumnNo]);
					kernelBuilder.mortalitySlope(mortalitySlope);

					Double mortalityInflection = Double.parseDouble(tokens[mortalityInflectionColumnNo]);
					kernelBuilder.mortalityInflection(mortalityInflection);


				}else {
					Double mortalityRate = Double.parseDouble(tokens[mortalityRateColumnNo]);
					kernelBuilder.mortalityRate(mortalityRate);
				}


				DispersalKernelParameters dispersalKernelParameters=kernelBuilder.build();
				DispersalKernelTransfer kernelType = new DispersalKernelTransfer(dispersalKernelParameters);

				Map<Sex, TransferType> genderStages = transferCharacteristics.get(stage);

				if(genderStages==null) {
					genderStages=new HashMap<Sex,TransferType>();
					transferCharacteristics.put(stage, genderStages);
				}

				if(genderStages.get(sex) != null) 
					throw new RuntimeException("More than one row in kernel file for stage " + 
							stage.getStageName() + " and sex "+ sex);

				genderStages.put(sex, kernelType);
			}
		}
		catch (IOException e) {
			LogWriter.printlnError("kernel stages file: Failed in reading file. " + filename);
			LogWriter.print(e);
		}
		return Map.copyOf(transferCharacteristics);
	}


}
