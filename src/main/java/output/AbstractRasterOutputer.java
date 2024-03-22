package output;

import java.io.File;

import global.ModelConfig;
import raster.RasterSet;
import raster.RasterItem;

public abstract class AbstractRasterOutputer<D extends RasterItem> {

	protected RasterSet<D> dataset;
	protected int year;
	protected int replicate;

	public AbstractRasterOutputer (RasterSet<D> dataset, int year, int replicate) {
		this.dataset = dataset;
		this.year=year;
		this.replicate=replicate;
	}

	abstract public void writeOutput();
	
	protected static File getOutputDir(int year, int replicate) {
		String outputDirName = ModelConfig.OUTPUT_DIR + File.separator + "replicate_" + replicate + File.separator + year;
		File outputDir = new File(outputDirName);
		outputDir.mkdirs();
		return outputDir;
	}
	
}
