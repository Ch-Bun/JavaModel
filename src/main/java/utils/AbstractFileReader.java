package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import genetics.CharacterMutationFactory;
import land.LandscapeRaster;

public class AbstractFileReader {

	protected String delimiterRegex;
	protected LandscapeRaster landscapeRaster;
	protected int colNumber;
	protected String noDataValue;

	public AbstractFileReader(int colNumber){
		this.delimiterRegex=",";
		this.colNumber = colNumber; 
		this.noDataValue = "#";
	}

	protected Map<String, Integer> handleHeader(String[] headertokens) {
		Map<String, Integer> headerMap = new HashMap<String, Integer>();

		if (headertokens.length < colNumber) { 
			LogWriter.printlnError("Too few columns");
			throw new RuntimeException("Too few columns in tabular file");
		}

		for (int i=0; i<headertokens.length; i++) {
			headerMap.put(headertokens[i], i);
		}
		return headerMap;
	}


	protected String[] parseLine(String line) {
		return line.trim().split(delimiterRegex);
	}

	protected DistributionSampler whichDistribution(String distribution, Map<String,Float> params, String filename) {
		//try catch block 
		DistributionSampler newDist = null;
		try {
			switch(distribution) {
			case "fixed":
				double value = params.get("value");
				newDist = new FixedValue(value, "fixed");
				break;
			case "negexp":
				double m = params.get("mean");
				newDist = new NegativeExponential(m, "negexp");
				break;
			case "normal":
				double mean =params.get("mean");
				double sd = params.get("sd");
				newDist = new DistributionType(new NormalDistribution(mean,sd),"normal");
				break;
			case "uniform":
				double min =params.get("min");
				double max = params.get("max");
				newDist =  new DistributionType(new UniformRealDistribution(min,max),"uniform");
				break;
			case "gamma":
				double shape = params.get("shape");
				double scale = params.get("scale");
				newDist=  new DistributionType(new GammaDistribution(shape,scale),"gamma");
				break;
			case "exponential":
				double emean = params.get("mean");
				newDist =  new DistributionType(new ExponentialDistribution(emean),"exponential");
				break;
			case "stepChar":
				newDist = new DistributionType(new NormalDistribution(0,0.05),"stepChar"); 
				break;
			case "uniformChar":
				newDist = new DistributionType(new UniformRealDistribution(0,params.size()),"uniformChar"); 
				break;
			default:
				LogWriter.printlnError("No distribution type available for " + distribution + " in file " + filename);
			}
		}
		catch(Exception e) {
			LogWriter.printlnError("Problem setting distributions for "+ filename + " check that parameters are valid input for type of distribution specified");
			LogWriter.print(e);
		}

		return newDist;
	}

	protected Map<String, Float>  convertStringList(String parameterString){

		Map<String, Float> holder = new HashMap<String,Float>();
		if(!parameterString.equals(noDataValue)) {
			try {
				String [] keyVals= StringUtils.substringBetween(parameterString, "{", "}").trim().split(" ");
				for(String keyVal:keyVals)
				{
					String[] parts = keyVal.split("=");
					holder.put(parts[0],Float.parseFloat(parts[1]));
				}
			}catch(Exception e) {
				LogWriter.printlnError("Problem with parameters for distribution, parameters must be encased by { } and have name=value");
				LogWriter.print(e);
			}
		}
		return Map.copyOf(holder);
	}


	protected List<Integer>  convertPositionsList(String parameterString){

		String numbers = StringUtils.substringBetween(parameterString, "{", "}").trim();
		Pattern pattern = Pattern.compile(" ");
		List<String> elements = pattern.splitAsStream(numbers).toList();
		List<Integer> positions = new ArrayList<Integer>();

		for(String element : elements) {
			List<Integer> range = Arrays.stream(element.split("-"))
					.mapToInt(Integer::parseInt)
					.boxed()
					.collect(Collectors.toList());

			IntStream.rangeClosed(range.stream()
					.mapToInt(v -> v)
					.min()
					.getAsInt(),
					range.stream()
					.mapToInt(v -> v)
					.max()
					.getAsInt())
			.forEach(no -> positions.add(no));
		}
		return List.copyOf(positions);
	}

	protected void addCharacterMutation(Map<String, Float> valueMap, Map<String,Float> dominanceMap){

		if(valueMap.size() != dominanceMap.size())
			LogWriter.printlnError("The number of mutation allele values does not match the number of mutation dominance values");

		for(Entry<String,Float> param : valueMap.entrySet()) {

			String key = param.getKey();
			float alleleValue = param.getValue();
			float dominanceValue = dominanceMap.get(key);
			char c = param.getKey().charAt(0);


			CharacterMutationFactory.add(c, alleleValue, dominanceValue);
		}


	}

	protected void addMicrosats(Set<Character> characterSet) {

		for(Character c : characterSet)
			CharacterMutationFactory.add(c, 0.0, 0.0);

	}



	//	protected List<Character>  convertCharacterList(String parameterString){
	//
	//		String numbers = StringUtils.substringBetween(parameterString, "{", "}").trim();
	//
	//		Pattern pattern = Pattern.compile(" ");
	//		List<String> elements = pattern.splitAsStream(numbers).toList();
	//
	//		List<Character> characters = new ArrayList<Character>();
	//
	//		for(String element : elements) {
	//			
	//			List<Character> range = element.replace("-", "").chars().mapToObj(ch -> (char) ch).collect(Collectors.toList());
	//		
	//			IntStream.rangeClosed(
	//					range.stream().min(Comparator.comparing(Character::charValue)).get(),
	//					range.stream().max(Comparator.comparing(Character::charValue)).get())
	//			.forEach(no -> characters.add((char)no));
	//		}
	//		return List.copyOf(characters);
	//	}

}
