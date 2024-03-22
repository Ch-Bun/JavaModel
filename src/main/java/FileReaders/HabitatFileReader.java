package FileReaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import land.HabitatFactory;
import land.HabitatType;
import land.HabitatType.HabitatBuilder;
import utils.AbstractFileReader;
import utils.LogWriter;

public class HabitatFileReader extends AbstractFileReader {

	int codeColumnNo,kColumnNo, habitatColumnNo,mortalityColumnNo,costColumnNo;

	public HabitatFileReader(){
		super(5); // 3 is code, habitat, K
	}

	public void readHabitatFile(String filename) {

		LogWriter.println("reading habitat file: " + filename);

		try(BufferedReader fileReader = new BufferedReader(new FileReader(filename))){
			String line = fileReader.readLine(); 
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			try {
				codeColumnNo = colNameMap.get("Code");
				habitatColumnNo = colNameMap.get("Habitat");
				kColumnNo = colNameMap.get("K");
				mortalityColumnNo = colNameMap.get("Mortality");
				costColumnNo = colNameMap.get("Cost");
			}catch(Exception e) {
				throw new RuntimeException("Habitat type file, error in column names. Hint: Check column spelling and lower/upper case");
			}

			while ((line=fileReader.readLine()) != null) {
				String[] tokens = parseLine(line);

				if (tokens.length < colNameMap.size()) { //TODO check if need log writer here or runtime enough
					LogWriter.printlnError("Number of columns doesn't match number of headers " + filename + ", " + line);
					throw new RuntimeException("Too few columns in tabular file " + filename + " line " + line);
				}

				Integer code = (this.noDataValue.equals(tokens[codeColumnNo])) ? null : Integer.parseInt(tokens[codeColumnNo]);
				String name = (this.noDataValue.equals(tokens[habitatColumnNo] )) ? null : tokens[habitatColumnNo];
				int K =  (this.noDataValue.equals(tokens[kColumnNo])) ? null : Integer.parseInt(tokens[kColumnNo]);
				Double mortality = (this.noDataValue.equals(tokens[mortalityColumnNo])) ? null : Double.parseDouble(tokens[mortalityColumnNo]);
				Double cost = (this.noDataValue.equals(tokens[costColumnNo])) ? null : Double.parseDouble(tokens[costColumnNo]);

				HabitatBuilder habitatBuilder = new HabitatType
						.HabitatBuilder(code, name, K);

				Optional.ofNullable(mortality)
				.ifPresent(habitatBuilder::mortality);
				Optional.ofNullable(cost)
				.ifPresent(habitatBuilder::cost);

				HabitatType habitatType=habitatBuilder.build();

				HabitatFactory.setHabitatType(habitatType);

			}
		}
		catch (IOException e) {
			LogWriter.printlnError("Habitat types file: Failed in reading file " + filename);
			LogWriter.print(e);
		}
		LogWriter.println("Processed " + filename + ", created " + HabitatFactory.getNoOfHabitats() + " habitat types");
	}



}
