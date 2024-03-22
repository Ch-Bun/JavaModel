package global;

import utils.FileWriterHelper;
import utils.LogWriter;
import utils.Position;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import FileReaders.DemographyFileReader;
import FileReaders.DispersalFileReader;
import FileReaders.EmigrationFileReader;
import FileReaders.HabitatCostReader;
import FileReaders.HabitatFileReader;
import FileReaders.HabitatMortalityReader;
import FileReaders.KernelFileReader;
import FileReaders.LifecycleReader;
import FileReaders.SMSFileReader;
import FileReaders.SettlementFileReader;
import creature.Creature;
import creature.Metapopulation;
import creature.Sex;
import creature.Stage;
import dispersal.EmigrationType;
import dispersal.Settlement;
import dispersal.TransferType;
//import environment.RainfallReader;
import genetics.MutationType;
import java.io.FileWriter;
//import gui.AppWindow;
import land.Cell;
import land.LandscapeRaster;
import land.LandscapeReader;
import land.Patch;
import land.PatchMap;
import lifecycle.DemographyType;
import lifecycle.LifeStageFactory;
import lifecycleevents.Initialise;
import lifecycleevents.LifeCycleEvent;
import lifecycleevents.ReproductiveSeason;
import output.CellOutputer;
import raster.RasterHeaderDetails;
import raster.RasterKey;
import utils.RandomNumberGenerator;


public class ModelMain {

	private RasterHeaderDetails desiredProjection;
	private LandscapeRaster landscapeRaster;
	//	private RainfallReader rainfallReader;

	private Metapopulation metapopulation;
	private PatchMap patchMap;

	private List<LifeCycleEvent> lifecycle;

	public static void main(String[] args) {
		ModelMain theModel = new ModelMain();

/*		if(System.getProperty("GUI_VERSION").equals("true")) {
			try {
				AppWindow window = new AppWindow();
				window.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else */
                System.out.println("Working Directory = " + System.getProperty("user.dir"));

		theModel.setup();
		for (int i = 0; i < ModelConfig.REPLICATES; i++) {
			try {
				theModel.run(i);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/* setup landscape and patches (maybe climate stuff) this means for each call to Novo the landscape and patches are the same but replicates
	 * i.e. population dynamics differ. If wanting different landscapes must call Novo multiple times */
	private void setup() {
		LogWriter.println("Starting setup");
		LogWriter.println("Start date: " + ModelConfig.START_DATE + ". End date: " + ModelConfig.END_DATE);
		LogWriter.println("Reproductive season starts: " + ModelConfig.START_DATE_REP_SEASON +". Ends: " + ModelConfig.END_DATE_REP_SEASON);

		LogWriter.println("Setting up stage and sex specific characteristics");

		createLifestages();

		LogWriter.println("Setting up life cycle");

		lifecycle=setLifecyle();

		LogWriter.println("Setting up landscape");

		desiredProjection = new RasterHeaderDetails(ModelConfig.NCOLS, ModelConfig.NROWS, ModelConfig.XLLCORNER, ModelConfig.YLLCORNER, 
				ModelConfig.CELLSIZE, ModelConfig.NODATASTRING);

		landscapeRaster = getLandscapeRaster(desiredProjection, ModelConfig.START_DATE.getYear());
		patchMap = new PatchMap();
		patchMap.createPatches(landscapeRaster, desiredProjection, ModelConfig.START_DATE.getYear());

		if(ModelConfig.FROM_CALIBRATION_RUN) {
			LogWriter.println("Initialising population from calibration");
			deserializeIndividualsAndPatches();
			LogWriter.println("Finished initialising population");

		}
		else {
			metapopulation =new Metapopulation();
			//patches will be single cells in grid based landscape or multi-celled in patch based landscape 

			//this means every replicate is initialised the same 
			LogWriter.println("Initialising population from scratch");
			Initialise initialise = new Initialise();
			initialise.execute(metapopulation, landscapeRaster, patchMap, ModelConfig.START_DATE, false);// false is just a dummy variable
			LogWriter.println("Finished initialising population from scratch");
		}
	}

	private void run(int replicate) {

		LocalDate date =  ModelConfig.START_DATE;
                int generations = 0;
                boolean balancing = true;

		//writePatchStats(date);
		//writeIndividualStats(date);

		while (date.isBefore(ModelConfig.END_DATE) || date.equals(ModelConfig.END_DATE)) {
			try {
				if(generations > ModelConfig.BALANCING_GENERATIONS)
                                    balancing = false;
                                if(balancing)
                                    LogWriter.println("balancing generations = "  +generations);
                                doTimestep(date, replicate, balancing);
                                generations++;
				if(!balancing)
                                    date =  date.plusYears(1);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
                
		if(ModelConfig.IS_CALIBRATION_RUN) {
			serializeIndividualsAndPatches(metapopulation);
			//serializeLandscape(landscapeRaster);
		}
	}

	private void doTimestep(LocalDate dateD, int replicate, boolean balancing) {
		int year = dateD.getYear();
		LogWriter.println("Year: " + year);

		if(year> ModelConfig.START_DATE.getYear()) {
			if(ModelConfig.DYNAMIC_LANDSCAPE) 
				dynamicLandscape(year);
		}

		LocalDate date = dateD;
                if(ModelConfig.DISTURBANCE && !balancing)
                    disturbPatches(metapopulation);
		for(LifeCycleEvent event : lifecycle) {
			if(event instanceof ReproductiveSeason)
				date = ModelConfig.START_DATE_REP_SEASON.atYear(year);
			event.execute(metapopulation, landscapeRaster, patchMap, date, balancing);
		}
		
                    writePatchStats(date);
                    writePatchZygosity(date);
                if(!balancing){
                    writeAnnualPatchStats(date);
                    writeAnnualPatchZygocityStats(date);
                    writeIndividualStats(date);
                    writeIndividualPaths(date);
                    outputTimestepResults(year, replicate);
                }
	}

        private void disturb(Metapopulation metapopulation){ //JAA-Sandbjerg
            for(Creature creature : metapopulation.setView()) {
                if(creature.getCurrentPatch().toBeDisturbed){
                    creature.getCurrentPatch().undisturbedGenerations++;
                    System.out.println("Undistirubed generations = " + creature.getCurrentPatch().undisturbedGenerations);
                    if(creature.getCurrentPatch().undisturbedGenerations == ModelConfig.GENERAIONS_BETWEEN_DISTURBANCES)
                        if(creature.isAlive() && RandomNumberGenerator.zeroToOne()< ModelConfig.FRACTION_KILLED_BY_DISTURBANCE){  
                            if(!metapopulation.removeCreature(creature))
                             LogWriter.printlnError("Trying to remove creature " + creature.getId() +  "from "
                                    + "metapopulation container when it doesn't exist in there");
                            creature.remove();
                        //    System.out.println("Disturbance performed");
                            creature.getCurrentPatch().undisturbedGenerations = 0;
                        }
                //    creature.getCurrentPatch().undisturbedGenerations++;
                    }
                 //   creature.getCurrentPatch().undisturbedGenerations++;
                }
            }

        private void disturbPatches(Metapopulation metapopulation){ //JAA-Sandbjerg
            if(ModelConfig.DISTURBANCE)
                for(Patch patch : patchMap.getPatches()) {
                    if(patch.toBeDisturbed)
                        if(patch.undisturbedGenerations == ModelConfig.GENERAIONS_BETWEEN_DISTURBANCES){
   //                         patch.toBeDisturbed = true;
                            patch.undisturbedGenerations = 1;
                            }
                            else{
  //                              patch.toBeDisturbed = false;
                               patch.undisturbedGenerations++;
                            }
                    }
        }
        
        
	private void dynamicLandscape(int year) {

		if(new File(ModelConfig.TEMPORAL_DIR + File.separator + year).exists()){

			String landscapeRasterPath = ModelConfig.TEMPORAL_DIR + File.separator + year + 
					File.separator + ModelConfig.LANDSCAPE_FILENAME;
			if(new File(landscapeRasterPath).exists()){
				LandscapeReader landscapeReader = new LandscapeReader(landscapeRaster);
				landscapeReader.getRasterDataFromFile(landscapeRasterPath);
			}

			if(ModelConfig.TRANSFERTYPE.equals("SMS") && ModelConfig.MOVEMENT_COSTS_FROM_RASTER) {

				String costRasterPath = ModelConfig.TEMPORAL_DIR + File.separator + year + 
						File.separator + ModelConfig.HABITAT_COST_RASTER_FILENAME;
				if(new File(costRasterPath).exists()){
					HabitatCostReader habitatCostReader = new HabitatCostReader(landscapeRaster);
					habitatCostReader.getRasterDataFromFile(costRasterPath);
				}

				String habitatMortalityRasterPath = ModelConfig.TEMPORAL_DIR + File.separator + year + 
						File.separator + ModelConfig.HABITAT_MORTALITY_RASTER_FILENAME;
				if(new File(habitatMortalityRasterPath).exists()){
					HabitatMortalityReader habitatMortalityReader = new HabitatMortalityReader(landscapeRaster);
					habitatMortalityReader.getRasterDataFromFile(habitatMortalityRasterPath);
				}
			}


		}
	}


	private LandscapeRaster getLandscapeRaster(RasterHeaderDetails desiredProjection, int year) {

		LandscapeRaster landscape;
		HabitatFileReader habitatFileReader ;
		String filePath = (ModelConfig.DYNAMIC_LANDSCAPE) ? ModelConfig.TEMPORAL_DIR + File.separator + year  : ModelConfig.LANDSCAPE_DIR;

		//if(!ModelConfig.FROM_CALIBRATION_RUN) {
		landscape = new LandscapeRaster(desiredProjection);


		if(ModelConfig.REAL_LANDSCAPE) {
			if(ModelConfig.USING_HABITAT_CODES) {
				habitatFileReader = new HabitatFileReader();
				habitatFileReader.readHabitatFile(filePath + File.separator +ModelConfig.HABITAT_FILENAME);
			}
			LandscapeReader landscapeReader = new LandscapeReader(landscape);
			landscapeReader.getRasterDataFromFile(filePath + File.separator +ModelConfig.LANDSCAPE_FILENAME);
			//not applicable for butterfly model as pgi rasters are the movement 'costs'
			if(ModelConfig.TRANSFERTYPE.equals("SMS") ) {
				if(ModelConfig.MOVEMENT_COSTS_FROM_RASTER) {
					HabitatCostReader habitatCostReader = new HabitatCostReader(landscape);
					habitatCostReader.getRasterDataFromFile(filePath + File.separator +ModelConfig.HABITAT_COST_RASTER_FILENAME);

					if(new File(filePath + File.separator + ModelConfig.HABITAT_MORTALITY_RASTER_FILENAME).exists()){
						HabitatMortalityReader habitatMortalityReader = new HabitatMortalityReader(landscape);
						habitatMortalityReader.getRasterDataFromFile(filePath + File.separator +ModelConfig.HABITAT_MORTALITY_RASTER_FILENAME);	
					}
				}
			}
		}
		else {
			habitatFileReader = new HabitatFileReader();
			habitatFileReader.readHabitatFile(filePath + File.separator +ModelConfig.HABITAT_FILENAME);
			landscape.generate();
		}


		//}
		//else landscape = deserializeLandscape();

		return landscape;
	}

	public void outputTimestepResults(int year, int replicate) {
		// Output LandUses to tabular file, for analysis (perhaps)
		LogWriter.println("Outputing cells for generation: " + year);
		CellOutputer cellOutputer = new CellOutputer(year, landscapeRaster, patchMap, replicate);
		cellOutputer.writeOutput();

	}

	private void createLifestages() {

		DemographyFileReader demographyReader = new DemographyFileReader();
		EmigrationFileReader emigrationReader = new EmigrationFileReader();
		DispersalFileReader<TransferType> transferReader = null;
		DispersalFileReader<Settlement> settlementReader=null;

		switch (ModelConfig.TRANSFERTYPE) {
		case "SMS":
			transferReader = new SMSFileReader();
			settlementReader = new SettlementFileReader();
			break;
		case "Kernel":
			transferReader = new KernelFileReader();
			settlementReader = new SettlementFileReader();
			break;
		case "NearestNeighbour":
			//transferReader = new nearestNeighbourFileReader();
			break;
		default:
			LogWriter.printlnError("TRANSFERTYPE parameter not recognised, <i>hint check spelling?<i/>");
		}

		//set up parameter maps so that they are immutable, methods return copyOf, which should be immutable maps 
		Map<Stage, Map<Sex, DemographyType>> demographies = demographyReader.readDemographyFile(ModelConfig.DEMOGRAPHYFILE);
		Map<Stage, Map<Sex, EmigrationType>> emigrationTypes = emigrationReader.readDispersalFile(ModelConfig.EMIGRATIONFILE);
		Map<Stage, Map<Sex, TransferType>> transferCharacteristics = transferReader.readDispersalFile(ModelConfig.TRANSFERFILE);
		Map<Stage, Map<Sex, Settlement>> settlementCharacteristics= settlementReader.readDispersalFile(ModelConfig.SETTLEMENTFILE);

		for(Entry<Stage, Map<Sex, DemographyType>> entry : demographies.entrySet()){
			Stage stage = entry.getKey();
			for(Entry<Sex, DemographyType> genderMapEntry : entry.getValue().entrySet()){
				Sex sex = genderMapEntry.getKey();
				DemographyType demographyType = genderMapEntry.getValue();
				if(demographyType.disperses()) {
					EmigrationType emigrationType = emigrationTypes.get(stage).get(sex);
					TransferType transferType = transferCharacteristics.get(stage).get(sex);
					Settlement settlementType = settlementCharacteristics.get(stage).get(sex);
					LifeStageFactory.setLifeStage(sex, stage, demographyType, emigrationType, transferType, settlementType);
				}
				else
					LifeStageFactory.setLifeStage(sex, stage, demographyType);

			}
		}
		LogWriter.println("Finished processing stage and sex specific characteristics files");

	}

	private void checkNumberOfStages(int noOfStages, int compare, String filename) {

		if(compare != noOfStages) {
			String e = "Number of rows in " + filename + " does not match demography file";
			LogWriter.printlnError(e); 
			throw new RuntimeException(e);
		}

	}

	private List<LifeCycleEvent> setLifecyle() {
		List<LifeCycleEvent> lifecycle = new ArrayList<LifeCycleEvent>();
		LifecycleReader lifecycleReader=new LifecycleReader();	
		lifecycleReader.readLifeCycleFile(ModelConfig.LIFECYCLEFILE, lifecycle);

		LogWriter.println("Finished setting up life cycle");
		return lifecycle;
	}

	private void writePatchStats(LocalDate date) {

                    try {
			StringBuffer sbHeadings = new StringBuffer("Year, Patch, Nt, Males, Females");
                        BufferedWriter outputFile = FileWriterHelper.getFileWriter(date, ModelConfig.PATCH_OUTPUT_FILE, sbHeadings.toString());

			for(Patch patch : patchMap.getPatches()) {


				StringBuffer sbData = new StringBuffer();
				sbData.append(String.format("%d,%d,%d,%d,%d", date.getYear(), patch.getId(), (patch.getNt(Sex.MALE)+patch.getNt(Sex.FEMALE)), patch.getNt(Sex.MALE), patch.getNt(Sex.FEMALE)));
                                // JAA - 21 November 2022
				outputFile.write(sbData.toString());
				outputFile.newLine();
			}

			outputFile.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}
	}
        
        private void writePatchZygosity(LocalDate date) {
            double TotalHeterozygozity = 0;
            int NumberOfPatches = 0;
                    try {
			StringBuffer sbHeadings = new StringBuffer("Year, Patch, Nt, Males, Females");
                        BufferedWriter outputFile = FileWriterHelper.getFileWriter(date, ModelConfig.PATCH_ZYGOSITY_OUTPUT_FILE, sbHeadings.toString());
                        System.out.println( "Total patches = " + patchMap.size());
			for(Patch patch : patchMap.getPatches()) {
				StringBuffer sbData = new StringBuffer();
                                double HZ = patch.getHeterozygosity(Sex.MALE, Stage.JUVENILE);
                                //System.out.println("Heterozygocity = "+ HZ + " Number of patches = "+ NumberOfPatches);
                                if(!Double.isNaN(HZ)){
                                    TotalHeterozygozity += HZ;
                                    NumberOfPatches++;
                                }
                                //System.out.println("writePatchZygosity - Number of patches = "+ NumberOfPatches);
				// sbData.append(String.format("%d,%d,%d,%d,%d", date.getYear(), patch.getId(), (patch.getNt(Sex.MALE)+patch.getNt(Sex.FEMALE)), patch.getNt(Sex.MALE), patch.getNt(Sex.FEMALE)));
                                sbData.append(String.format("%d,%d,%f", date.getYear(), patch.getId(), HZ)); 
                                // JAA - 21 November 2022
				outputFile.write(sbData.toString());
				outputFile.newLine();
			}

			outputFile.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}
            System.out.println("Total heterozygocity "+ date.getYear()+" " + TotalHeterozygozity/NumberOfPatches);
	}
        
        private void writeAnnualPatchStats(LocalDate date) {
                
            String year = String.valueOf(date.getYear());
            String outputDirName = ModelConfig.OUTPUT_DIR + File.separator + "AnnualPatches" + File.separator + year;
		File outputDir = new File(outputDirName);
		outputDir.mkdirs();
                BufferedWriter patchWriter = null;
                try {
                    String FileName = outputDir.getPath() + File.separator + "Patch.txt";
                    patchWriter = new BufferedWriter(new FileWriter(FileName, false));   
                    
                    StringBuffer sbHeadings = new StringBuffer("Longitude, Latitude, Total, Males, Females");
                    patchWriter.write(sbHeadings.toString());
                    patchWriter.newLine();
                   //     patchWriter = FileWriterHelper.getFileWriter(date, ModelConfig.ANNUAL_PATCH_OUTPUT_DIR, sbHeadings.toString());                  
                        for(Patch patch : patchMap.getPatches()) {
				StringBuffer sbData = new StringBuffer();
				sbData.append(String.format("%d,%d,%d,%d,%d", patch.getLatitude(), patch.getLongitude(), (patch.getNt(Sex.MALE)+patch.getNt(Sex.FEMALE)), patch.getNt(Sex.MALE), patch.getNt(Sex.FEMALE)));
                                // JAA - 21 November 2022
				patchWriter.write(sbData.toString());
				patchWriter.newLine();
			}

			patchWriter.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}
	}

        private void writeAnnualPatchZygocityStats(LocalDate date) {
                
            String year = String.valueOf(date.getYear());
            String outputDirName = ModelConfig.OUTPUT_DIR + File.separator + "AnnualPatches" + File.separator + year;
		File outputDir = new File(outputDirName);
		outputDir.mkdirs();
                BufferedWriter patchWriter = null;
                try {
                    String FileName = outputDir.getPath() + File.separator + "PatchZygocity.txt";
                    patchWriter = new BufferedWriter(new FileWriter(FileName, false));   
                    
                    StringBuffer sbHeadings = new StringBuffer("Longitude, Latitude, Heterozygocity");
                    patchWriter.write(sbHeadings.toString());
                    patchWriter.newLine();
                   //     patchWriter = FileWriterHelper.getFileWriter(date, ModelConfig.ANNUAL_PATCH_OUTPUT_DIR, sbHeadings.toString());                  
                        for(Patch patch : patchMap.getPatches()) {
				StringBuffer sbData = new StringBuffer();
				sbData.append(String.format("%d,%d,%f", patch.getLatitude(), patch.getLongitude(), patch.getHeterozygosity(Sex.MALE, Stage.JUVENILE)));
                                // JAA - 21 November 2022
				patchWriter.write(sbData.toString());
				patchWriter.newLine();
			}

			patchWriter.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}
	}
        
	private void writeIndividualStats(LocalDate date) {

		try {
			StringBuffer sbHeadings = new StringBuffer("Year,Id,Stage,Age,Sex,CurrentX,CurrentY,CurrentPatch");
			BufferedWriter outputFile = FileWriterHelper.getFileWriter(date, ModelConfig.INDIVIDUALS_OUTPUT_FILE, sbHeadings.toString());

			Set<Creature> metapop = metapopulation.setView();
			for(Creature creature : metapop) {

				int patchId = (creature.getCurrentPatch() != null) ? creature.getCurrentPatch().getId() : -9999;

				StringBuffer sbData = new StringBuffer();
				sbData.append(String.format("%d,%d,%s,%d,%s,", date.getYear(), creature.getId(), creature.getStage().getStageName(), creature.getAge(), creature.getSex()));
				sbData.append(String.format("%f,%f,%d", creature.getXLocation(), creature.getYLocation(), patchId));

				outputFile.write(sbData.toString());
				outputFile.newLine();
			}

			outputFile.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}



	}
	
	private void writeIndividualPaths(LocalDate date) {

		try {
			StringBuffer sbHeadings = new StringBuffer("Year,Id,Step,X,Y");
			BufferedWriter outputFile = FileWriterHelper.getFileWriter(date, ModelConfig.INDIVIDUAL_PATH_OUTPUT_FILE, sbHeadings.toString());

			Set<Creature> metapop = metapopulation.setView();
			for(Creature creature : metapop) {

				if(creature.getAge()==1 && creature.getPath().size() > 0) {
				StringBuffer sbData = new StringBuffer();
				List<Position> path = creature.getPath();
				int step =1;
				for(Position pos : path) {
				sbData.append(String.format("%d,%d,", date.getYear(), creature.getId()));
				sbData.append(String.format("%d,%f,%f",step, pos.getX(),pos.getY()));

				outputFile.write(sbData.toString());
				outputFile.newLine();
				step++;
				}
				}
			}

			outputFile.close();

		} catch (IOException e) {
			LogWriter.print(e);
		}



	}


	private void serializeIndividualsAndPatches(Metapopulation metaPopulation) {
		try {
			LogWriter.println("Starting serializing individuals to " + ModelConfig.SERIALIZED_DATA_FILE);
			FileOutputStream fileOut = new FileOutputStream(ModelConfig.SERIALIZED_DATA_FILE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(metaPopulation);

			out.close();
			fileOut.close();
			LogWriter.println("Serialized data is saved");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void deserializeIndividualsAndPatches() {
		try {
			FileInputStream fileIn = new FileInputStream(ModelConfig.SERIALIZED_DATA_FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			metapopulation = (Metapopulation) in.readObject();
			in.close();
			fileIn.close();
			fillPatchesAfterDeserializing();

			LogWriter.println("Deserialized " + ModelConfig.SERIALIZED_DATA_FILE);
		} catch (IOException i) {
			LogWriter.printlnError("Problem deserializing " + ModelConfig.SERIALIZED_DATA_FILE);
			LogWriter.print(i);
		} catch (ClassNotFoundException c) {
			LogWriter.printlnError("Metapopulation or PatchMap class not found");
			c.printStackTrace();
		}
	}

	private void fillPatchesAfterDeserializing() {

		Set<Creature> metapop =  metapopulation.setView();

		for(Creature creature : metapop) {
			RasterKey location =landscapeRaster.getKeyFromCoordinates(creature.getXLocation(), creature.getYLocation());

			Patch patch = patchMap.getPatchForLocation(location);
			if(patch !=null) {
				patch.addCreature(creature);
				creature.updateCurrentPatch(patch);
			}
		}

	}



	private void serializeLandscape(LandscapeRaster landscape) {
		try {
			LogWriter.println("Starting serializing individuals to " + ModelConfig.SERIALIZED_LANDSCAPE_FILE);
			FileOutputStream fileOut = new FileOutputStream(ModelConfig.SERIALIZED_LANDSCAPE_FILE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(landscape);
			out.close();
			fileOut.close();
			LogWriter.println("Serialized data is saved");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}



	@SuppressWarnings("unchecked")
	private LandscapeRaster deserializeLandscape() {
		try {
			LandscapeRaster landscape;
			FileInputStream fileIn = new FileInputStream(ModelConfig.SERIALIZED_LANDSCAPE_FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			landscape = (LandscapeRaster) in.readObject();
			in.close();
			fileIn.close();
			LogWriter.println("Deserialized " + ModelConfig.SERIALIZED_LANDSCAPE_FILE);
			return landscape;
		} catch (IOException i) {
			LogWriter.printlnError("Problem deserializing " + ModelConfig.SERIALIZED_LANDSCAPE_FILE);
			LogWriter.print(i);
			return null;
		} catch (ClassNotFoundException c) {
			LogWriter.printlnError("Metapopulation class not found");
			c.printStackTrace();
			return null;
		}
	}



}
