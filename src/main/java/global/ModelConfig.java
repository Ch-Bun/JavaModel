package global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

//import gui.ConfigCreator;

public class ModelConfig {

	
	///BUILD A NEW CONFIG FOR LANDSCAPE SPECIFIC PARAMETERS
	///-DCONFIG_FILE=C:\Users\s03rh1\eclipse-workspace\Novo_test\config.properties
	private Properties configFile;
	private static ModelConfig modelConfig;
	public static final String CONFIG_FILE = System.getProperty("CONFIG_FILE");
	public static final String GUI_VERSION = System.getProperty("GUI_VERSION");
	
	private ModelConfig() {
		configFile = new Properties();
		
		/*if(GUI_VERSION.equals("true")) {
			configFile = ConfigCreator.getProperties();
		}else {*/
		try {
			System.out.println("Config. file is " + CONFIG_FILE);
			if (CONFIG_FILE != null)
				configFile.load(new FileInputStream(CONFIG_FILE));
		}
		catch (IOException e) {
			System.err.println("Problems reading config file");
			System.err.println(e.getMessage());
		}
//		}
	} 

	public static String getSetupDetails() {
		String buildVerion = System.getProperty("BUILDVER");
		StringBuffer sb = new StringBuffer("Build version: " + buildVerion + "\n");

		Properties props = getModelConfig().configFile;
		Enumeration<?> em = props.keys();
		while(em.hasMoreElements()) {
			String str = (String) em.nextElement();
			sb.append(str + ": " + props.get(str) + "\n");
		}

		return sb.toString();
	}

	private static ModelConfig getModelConfig() {
		if (modelConfig == null)
			modelConfig = new ModelConfig();

		return modelConfig;
	}

	private static String getProperty(String prop) {
		return getModelConfig().getProp(prop);
	}
	private static String getProperty(String prop, String defaultString) {
		String propValue = getProperty(prop);
		return propValue == null ? defaultString : propValue;
	}
	private String getProp(String prop) {
		return configFile.getProperty(prop);
	}

	private static Integer getIntProperty(String prop, Integer defaultInt) {
		Integer propValue = getModelConfig().getIntProp(prop);
		return propValue == null ? defaultInt : propValue;
	}
	private Integer getIntProp(String prop) {
		String v = configFile.getProperty(prop);
		return v==null ? null : Integer.valueOf(v);
	}

	@SuppressWarnings("unused")
	private static Long getLongProperty(String prop, Long defaultLong) {
		Long propValue = getModelConfig().getLongProp(prop);
		return propValue == null ? defaultLong : propValue;
	}
	private Long getLongProp(String prop) {
		String v = configFile.getProperty(prop);
		return v==null ? null : Long.valueOf(v);
	}

	private static Double getDoubleProperty(String prop, Double defaultDouble) {
		Double propValue = getModelConfig().getDoubleProp(prop);
		return propValue == null ? defaultDouble : propValue;
	}
	private Double getDoubleProp(String prop) {
		String v = configFile.getProperty(prop);
		return v==null ? null :  Double.valueOf(v);
	}

	private static Boolean getBooleanProperty(String prop, Boolean defaultBoolean) {
		return getModelConfig().getBooleanProp(prop, defaultBoolean);
	}
	private boolean getBooleanProp(String prop, Boolean defaultBoolean) {
		String v = configFile.getProperty(prop);
		return v==null ? defaultBoolean :  Boolean.valueOf(v);
	}

	private static List<Integer> getListProperty(String prop, List<Integer> defaultList) {
		return getModelConfig().getListProp(prop, defaultList);
	}
	private List<Integer> getListProp(String prop, List<Integer> defaultList) {
		String v = configFile.getProperty(prop);
		
		if(v != null) {
		String numbers = StringUtils.substringBetween(v, "{", "}").trim();

		Pattern pattern = Pattern.compile(" ");
		List<Integer> elements = pattern
				.splitAsStream(numbers)
				.map(s -> Integer.parseInt(s))
				.collect(Collectors.toList());
		return elements;
		}
		else return defaultList;
	}
	
	private static List<Double> getDoubleListProperty(String prop, List<Double> defaultList) {
		return getModelConfig().getDoubleListProp(prop, defaultList);
	}
	
	private List<Double> getDoubleListProp(String prop, List<Double> defaultList) {
		String v = configFile.getProperty(prop);
		
		if(v != null) {
		String numbers = StringUtils.substringBetween(v, "{", "}").trim();

		Pattern pattern = Pattern.compile(" ");
		List<Double> elements = pattern
				.splitAsStream(numbers)
				.map(s -> Double.parseDouble(s))
				.collect(Collectors.toList());
		return elements;
		}
		else return defaultList;
	}


	public static final boolean SUPPRESS_STD_OUTPUT = getBooleanProperty("SUPPRESS_STD_OUTPUT", Boolean.FALSE);

	// Directory information
	public static final String BASE_DIR = getProperty("BASE_DIR");  // this must to be set in config file
	public static final String OUTPUT_DIR = getProperty("OUTPUT_DIR", ".");
	public static final String DATA_DIR = getProperty("DATA_DIR", BASE_DIR + File.separator + "data");
	public static final int FILE_MONITOR_TIMEOUT_SEC = getIntProperty(" FILE_MONITOR_TIMEOUT_SEC", 60*60*2);
	public static final String LANDSCAPE_DIR = getProperty("LANDSCAPE_DIR", DATA_DIR + File.separator + "landscape");
	public static final String DEMOGRAPHY_DIR = getProperty("DEMOGRAPHY_DIR", DATA_DIR + File.separator + "demography");
	public static final String TEMPORAL_DIR = getProperty("TEMPORAL_DIR",DATA_DIR + File.separator + "temporal");
        public static final String ANNUAL_PATCH_OUTPUT_DIR = getProperty("ANNUAL_PATCH_OUTPUT_DIR", OUTPUT_DIR + File.separator + "annualpatches");
	
	
	public static final boolean DYNAMIC_LANDSCAPE = getBooleanProperty("DYNAMIC_LANDSCAPE", false);
	public static final String PATCH_FILENAME = getProperty("PATCH_FILENAME", "patchRaster.txt");
	public static final String LANDSCAPE_FILENAME = getProperty("LANDSCAPE_FILENAME", "landscape.txt"); //codes or quality
	public static final boolean USING_HABITAT_CODES = getBooleanProperty("USING_HABITAT_CODES", false);
	public static final String HABITAT_FILENAME = getProperty("HABITAT_FILENAME", "habitatTypes.csv"); //if using above then need this file


	//this is needed for quality habitats but also if movement isnt always landscape related i.e. maybe cost surface is habitat x temperature
	public static final boolean MOVEMENT_COSTS_FROM_RASTER = getBooleanProperty("MOVEMENT_COSTS_FROM_RASTER", false);
	public static final String HABITAT_COST_RASTER_FILENAME = getProperty("HABITAT_COST_RASTER_FILENAME", "habitatCosts.asc");
	public static final String HABITAT_MORTALITY_RASTER_FILENAME = getProperty("HABITAT_MORTALITY_RASTER_FILENAME",null);
	public static final int HABITAT_NO_DATA_COST = getIntProperty("HABITAT_NO_DATA_COST",100000);
	public static final String BOUNDARYCONDITION = getProperty("BOUNDARYCONDITION", "reflective");
	
	
	
	//use the last year if directory doesn't exist
	//if landscape is dynamic then put all data in first directory
	
	public static final String LIFECYCLEFILE = getProperty("LIFECYCLEFILE", DATA_DIR + File.separator + "demography" + File.separator + "lifecycle.csv");
	public static final String DEMOGRAPHYFILE = getProperty("DEMOGRAPHYFILE", DATA_DIR + File.separator + "demography" + File.separator + "demography.csv");
	
	public static final String PATCH_OUTPUT_FILE = getProperty("PATCH_OUTPUT_FILE", OUTPUT_DIR + File.separator + "patches.csv");
        public static final String PATCH_ZYGOSITY_OUTPUT_FILE = getProperty("PATCH_OUTPUT_FILE", OUTPUT_DIR + File.separator + "patchZygosity.csv");
	public static final String INDIVIDUALS_OUTPUT_FILE = getProperty("INDIVIDUALS_OUTPUT_FILE", OUTPUT_DIR + File.separator + "individuals.csv");
	public static final String INDIVIDUAL_PATH_OUTPUT_FILE = getProperty("INDIVIDUAL_PATH_OUTPUT_FILE", OUTPUT_DIR + File.separator + "individual_paths.csv");

	
	public static final Boolean DENSITY_DEPENDENT_FECUNDITY = getBooleanProperty("DENSITY_DEPENDENT_FECUNDITY", false);
	public static final Boolean DENSITY_DEPENDENT_SURVIVAL = getBooleanProperty("DENSITY_DEPENDENT_SURVIVAL", false);
	public static final Boolean DENSITY_DEPENDENT_DEVELOPMENT = getBooleanProperty("DENSITY_DEPENDENT_DEVELOPMENT", false);
	
	public static final boolean DYNAMIC_REPRODUCTION = getBooleanProperty("DYNAMIC_REPRODUCTION", true);
	
	public static final boolean IS_CALIBRATION_RUN = getBooleanProperty("IS_CALIBRATION_RUN", true);
	public static final String CALIB_DIR = IS_CALIBRATION_RUN ? OUTPUT_DIR : getProperty("CALIB_DIR", OUTPUT_DIR + File.separator + "calibration");
	public static final String SERIALIZED_DATA_FILE = CALIB_DIR + File.separator +  "data.ser";
	public static final String SERIALIZED_LANDSCAPE_FILE = CALIB_DIR + File.separator +  "landscape.ser";
	public static final boolean FROM_CALIBRATION_RUN = getBooleanProperty("FROM_CALIBRATION_RUN", false);
	
	
	//dispersal files
	public static final String EMIGRATIONFILE = getProperty("EMIGRATIONFILE", DATA_DIR + File.separator + "demography" + File.separator + "emigration.csv");
	public static final String TRANSFERFILE = getProperty("TRANSFERFILE", DATA_DIR + File.separator + "demography" + File.separator + "transfer.csv");
	public static final String SETTLEMENTFILE = getProperty("SETTLEMENTFILE", DATA_DIR + File.separator + "demography" + File.separator + "settlement.csv");
	
	//genetic files 
	public static final String QTLPARAMETERSFILE = getProperty("QTLPARAMETERSFILE", DATA_DIR + File.separator + "genetics" + File.separator + "QTLParameters.csv");
	public static final String MUTATIONPARAMETERSFILE = getProperty("MUTATIONPARAMETERSFILE", DATA_DIR + File.separator + "genetics" + File.separator + "MutationParameters.csv");
	
	//landscape parameters

	public static final int NCOLS = getIntProperty("NCOLS", -9999);
	public static final int NROWS = getIntProperty("NROWS", -9999);
	public static final double XLLCORNER = getDoubleProperty("XLLCORNER", -9999.0);
	public static final double YLLCORNER = getDoubleProperty("YLLCORNER", -9999.0);
	public static final double CELLSIZE = getDoubleProperty("CELLSIZE", -9999.0);
	public static final String NODATASTRING = getProperty("NODATASTRING", "-9999");
	public static final boolean PATCH_BASED = getBooleanProperty("PATCH_BASED", false);
	public static final int MATRIX_PATCH_ID = getIntProperty("MATRIX_PATCH_ID", -1);
	public static final boolean REAL_LANDSCAPE = getBooleanProperty("REAL_LANDSCAPE", false);
	public static final double PROPORTION_SUITABLE = getDoubleProperty("PROPORTION_SUITABLE", 1.0);
	public static final boolean FRACTAL = getBooleanProperty("FRACTAL", false);
	public static final boolean BINARY_FRACTAL = getBooleanProperty("BINARY_FRACTAL", false);
	public static final double HURST_EXPONENT = getDoubleProperty("HURST_EXPONENT", 0.5);
	public static final int REPLICATES = getIntProperty("REPLICATES", 1);
        public static final boolean DISTURBANCE = getBooleanProperty("DISTURBANCE", false);//JAA-Sandbjerg
        public static final double FRACTION_OF_PATCHES_TO_DISTURB = getDoubleProperty("FRACTION_OF_PATCHES_TO_DISTURB", 0.75);//JAA-Sandbjerg
        public static final double FRACTION_KILLED_BY_DISTURBANCE = getDoubleProperty("FRACTION_KILLED_BY_DISTURBANCE", 0.90);//JAA-Sandbjerg
        public static final int GENERAIONS_BETWEEN_DISTURBANCES = getIntProperty("GENERAIONS_BETWEEN_DISTURBANCES", 3);//JAA-23 november 2022
        public static final int BALANCING_GENERATIONS = getIntProperty("BALANCING_GENERATIONS", 100);//JAA-24 february 2024
        
       
	//time parameters

	public static final LocalDate START_DATE = LocalDate.of(getIntProperty("START_YEAR", 1961),getIntProperty("START_MONTH", 1),getIntProperty("START_DAY", 1));
	public static final LocalDate END_DATE = LocalDate.of(getIntProperty("END_YEAR", 1961),getIntProperty("END_MONTH", 1),getIntProperty("END_DAY", 1));
	public static final MonthDay START_DATE_REP_SEASON = MonthDay.of(getIntProperty("START_MONTH_REP_SEASON", 5),getIntProperty("START_DAY_REP_SEASON", 1));
	public static final MonthDay END_DATE_REP_SEASON = MonthDay.of(getIntProperty("END_MONTH_REP_SEASON", 6),getIntProperty("END_DAY_REP_SEASON", 30));
	
	//Demographic parameters
	public static final int NUMBEROFSEXES = getIntProperty("NUMBEROFSEXES", 2);
	public static final String MATINGSYSTEM = getProperty("MATINGSYSTEM", "polygyny");
	public static final int MATINGMALESSIZE = getIntProperty("MATINGMALESSIZE", 1000);
	public static final int INITIALPOPULATIONSTAGEID = 1;
	public static final int INITIALPOPULATIONAGE = 1;
	public static final int DAYSOFGESTATION = getIntProperty("DAYSOFGESTATION",60);
	public static final int TIMETAKENTOMATE = getIntProperty("TIMETAKENTOMATE",0);
	
	//Dispersal parameters
	public static final String TRANSFERTYPE = getProperty("TRANSFERTYPE", "Kernel");
	public static final double PERCEPTUALRANGE = getDoubleProperty("PERCEPTUALRANGE", 1.0);
	public static final String PERCEPTUALRANGEMETHOD = getProperty("PERCEPTUALRANGE","arithmetic");
	public static final int MAX_STEPS_PER_DISPERSAL_EVENT = getIntProperty("MAX_STEPS_PER_DISPERSAL_EVENT", 1000);
	
	//Genetic parameters
	public static final List<Integer> CHROMOSOMESIZES = getListProperty("CHROMOSOMESIZES",List.of(0));
	public static final double GENOMERECOMBINATIONRATE = getDoubleProperty("GENOMERECOMBINATIONRATE",0.001);
	
	//Initialisation parameters
	public static final double INITIALISE_K_PROPORTION = getDoubleProperty("INITIALISE_K_PROPORTION", 1.0);
	public static final boolean INITIALISE_SUBSET_PATCHES = getBooleanProperty("INITIALISE_SUBSET_PATCHES", false);
	public static final List<Integer> INITIAL_PATCHES = getListProperty("INITIAL_PATCHES", List.of(0));
	public static final List<Double> INITIAL_STAGE_PROPORTIONS = getDoubleListProperty("INITIAL_STAGE_PROPORTIONS", List.of(0.0));
	
	



	
	

	
}
