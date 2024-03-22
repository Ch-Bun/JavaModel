package environment;


import FileReaders.AbstractEnvironmentReader;
import global.ModelConfig;
import land.Cell;

public class RainfallReader extends AbstractEnvironmentReader {

	public String getFilePath(int year){
		//return ModelConfig.RAINFALL_DIR + File.separator + year + File.separator + ModelConfig.RAINFALL_FILENAME;
		return ModelConfig.BASE_DIR;
	}



	@Override
	public void setCellData(Cell cell, double[] rainfallArray) {
		cell.setAnnualRainfall(rainfallArray);
	}
	
	
}
