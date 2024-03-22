package raster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import utils.LogWriter;

public abstract class AbstractRasterReader<D extends RasterItem> {
	protected static final boolean DEBUG = false;
	private static final int HEADER_LENGTH = 6;
	private static final int HEADER_DATA_COL = 1;
	private static final int NCOLUMN_ROW = 0;
	private static final int NROW_ROW = 1;
	private static final int XLL_CORNER = 2;
	private static final int YLL_CORNER = 3;
	private static final int CELL_SIZE = 4;
	private static final int NODATA_VALUE_ROW = 5;
			
	protected RasterSet<D> dataset;
	
	public AbstractRasterReader () {
	}

	public AbstractRasterReader (RasterSet<D> dataset) {
		this.dataset = dataset;
	}
	
	protected String[] parseLine(String line) {
		return line.split(" +|,");
	}
	
	protected RasterHeaderDetails handleHeader(BufferedReader br) throws IOException {
		String headerLine = new String();
		int ncolumns=0;
		int nrows=0;
		double xllCorner=0.0;
		double yllCorner = 0.0;
		double xcellSize = 0;
		String nodataString=null;
		
		for (int i=0; i<HEADER_LENGTH; i++) {
			headerLine = br.readLine();
			if (headerLine == null)
				throw new IOException("Raster file format problem. Too short, not even a full header:" + i);
			
			String[] tokens = parseLine(headerLine);
			
			if (DEBUG)
				for (String token : tokens)
					LogWriter.println(token);		

			switch (i) {
			case NCOLUMN_ROW:
				ncolumns = Integer.parseInt(tokens[HEADER_DATA_COL]);
				break;
			case NROW_ROW:
				nrows = Integer.parseInt(tokens[HEADER_DATA_COL]);
				break;
			case XLL_CORNER:
				xllCorner = Double.parseDouble(tokens[HEADER_DATA_COL]);
				if (xllCorner != dataset.getHeaderDetails().getXllCorner())
					throw new IOException("Raster file format problem. Lower x corner doesn't match user input");
				break;
			case YLL_CORNER:
				yllCorner = Double.parseDouble(tokens[HEADER_DATA_COL]);
				if (yllCorner != dataset.getHeaderDetails().getYllCorner())
					throw new IOException("Raster file format problem. Lower y corner doesn't match user input");
				break;
			case CELL_SIZE:
				xcellSize =  Double.parseDouble(tokens[HEADER_DATA_COL]);
				break;
			case NODATA_VALUE_ROW:
				nodataString = tokens[HEADER_DATA_COL];
				break;
			}
		}
		
		if (DEBUG) LogWriter.println("Creating RasterDataset col:"+ncolumns + ", rows:" + nrows);
		
		RasterHeaderDetails headerDetails = new RasterHeaderDetails(ncolumns, nrows, xllCorner, yllCorner, xcellSize, nodataString);
		if (dataset == null) createDataSet(headerDetails);
		return headerDetails;
	}
	
	protected void createDataSet(RasterHeaderDetails headerDetails) {
		throw new RuntimeException("AbstractRasterReader.createDataSet: This should be overridden if dataset not passed in");
	}
	
	public RasterSet<D> getRasterDataFromFile(String filename) {

		LogWriter.println("AbstractRasterReader: Reading " + filename);
		
		long startTime = System.currentTimeMillis();
		int row = 0, col = 0;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			RasterHeaderDetails header = handleHeader(in);
			String line;			
			
			while ((line=in.readLine()) != null) {
				
				String[] tokens = parseLine(line.trim());

				for (String token : tokens) {
					
					if (header.getNodataString().equals(token)) {
						if (DEBUG) LogWriter.println(",");
					}
					else {
						if (DEBUG) LogWriter.println(token + ",");

						Collection <D> dataPoints = dataset.getCollection(col, row, header);
						for (D data : dataPoints) {
							setData(data, token);
						}
					}					
					col++;
				}
				row++;
				if (DEBUG) LogWriter.println(filename + ", row:" + row + ", col:" + col);
				col=0;
			}
			in.close();
		}
		catch (Exception e) {
			LogWriter.println("row:" + row + ", col:" + col);
			LogWriter.print(e);
		}

		LogWriter.println("Reading " + filename + ", took " + (System.currentTimeMillis() - startTime) + " ms");

		return dataset;
	}
	
	protected boolean areHeadersConsistent(RasterHeaderDetails header) {
		return dataset.isConstistentWithHeader(header);
	}
	
	public abstract void setData(D data, String value);

}
