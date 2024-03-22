package output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import utils.LogWriter;
import raster.RasterKey;
import land.Cell;
import land.HabitatFactory;
import land.HabitatType;
import land.LandscapeRaster;
import land.PatchMap;

public class CellOutputer extends AbstractRasterOutputer<Cell> {

	public CellOutputer(int year, LandscapeRaster landscape, PatchMap patchMap, int replicate) {
		super(landscape, year, replicate);
	}

	@Override
	public void writeOutput() {
		File outputDir = getOutputDir(year, replicate);
		BufferedWriter cellWriter = null;

		try {
			String landscapeFileName = outputDir.getPath() + File.separator + "Cells.txt";
			cellWriter = new BufferedWriter(new FileWriter(landscapeFileName, false));

			StringBuffer sbHeader = new StringBuffer("Lon Lat K"); //rainfall, temp, 

			Collection<HabitatType> habitats = HabitatFactory.getHabitats();

			for(HabitatType entry : habitats) {
				sbHeader.append(" " +entry.getHabitat()); 
			}
			cellWriter.write(sbHeader.toString());
			cellWriter.newLine();

			for (Entry<raster.RasterKey, Cell> entry : dataset.entrySet()) {
				RasterKey key = entry.getKey();
				Cell cell = entry.getValue();

				if (cell == null)
					continue;

				double lon = dataset.getXCoordin(key);
				double lat = dataset.getYCoordin(key);


				StringBuffer sbData = new StringBuffer(String.format("%.2f %.2f", lon, lat));
				
				sbData.append(String.format(" %.4f", cell.getK())); //JAA-Sandbjerg - .4f

				for(HabitatType cover : habitats) {
					sbData.append(String.format(" %.8f", cell.getLandCoverPercent(cover)));
				}

				cellWriter.write(sbData.toString());
				cellWriter.newLine();
			}
		}
		catch (IOException e) {
			LogWriter.print(e);
		}
		finally {
			if (cellWriter != null) {
				try {
					cellWriter.close();
				} 
				catch (IOException e) {
					LogWriter.print(e);
				}
			}
		}

	}
        
    /*    	public void writeOutput() {
		File outputDir = getOutputDir(year, replicate);
		BufferedWriter cellWriter = null;

		try {
			String landscapeFileName = outputDir.getPath() + File.separator + "Cells.txt";
			cellWriter = new BufferedWriter(new FileWriter(landscapeFileName, false));

			StringBuffer sbHeader = new StringBuffer("Lon Lat K"); //rainfall, temp, 

			Collection<HabitatType> habitats = HabitatFactory.getHabitats();

			for(HabitatType entry : habitats) {
				sbHeader.append(" " +entry.getHabitat()); 
			}
			cellWriter.write(sbHeader.toString());
			cellWriter.newLine();

			for (Entry<raster.RasterKey, Cell> entry : dataset.entrySet()) {
				RasterKey key = entry.getKey();
				Cell cell = entry.getValue();

				if (cell == null)
					continue;

				double lon = dataset.getXCoordin(key);
				double lat = dataset.getYCoordin(key);


				StringBuffer sbData = new StringBuffer(String.format("%.2f %.2f", lon, lat));
				
				sbData.append(String.format(" %.4f", cell.getK())); //JAA-Sandbjerg - .4f

				for(HabitatType cover : habitats) {
					sbData.append(String.format(" %.8f", cell.getLandCoverPercent(cover)));
				}

				cellWriter.write(sbData.toString());
				cellWriter.newLine();
			}
		}
		catch (IOException e) {
			LogWriter.print(e);
		}
		finally {
			if (cellWriter != null) {
				try {
					cellWriter.close();
				} 
				catch (IOException e) {
					LogWriter.print(e);
				}
			}
		}

	}*/

}
