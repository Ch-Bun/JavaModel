package raster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.LogWriter;

/** Assumes first two columns are x/y and rest are doubles */
public abstract class AbstractTabularRasterReader<D extends RasterItem> {

	private static int X_COL = 0;
	private static int Y_COL = 1;

	protected RasterSet<D> dataset;
	private String delimiterRegex;
	private int minColNum;
	protected String[] dataColNames;

	public AbstractTabularRasterReader (String delimiterRegex, int minColNum) {
		this.delimiterRegex = delimiterRegex;
		this.minColNum = minColNum;
	}
	
	public AbstractTabularRasterReader (String delimiterRegex, int minColNum, RasterSet<D> dataset) {
		this(delimiterRegex, minColNum);
		this.dataset = dataset;
	}
	
	private String[] parseLine(String line) {
		return line.trim().split(delimiterRegex);
	}
	
	protected int getXCol() {
		return X_COL;
	}
	
	protected int getYCol() {
		return Y_COL;
	}
	
	private String[] handleHeader(String[] headertokens) {
		if (headertokens.length < minColNum) {
			LogWriter.printlnError("Too few columns");
			throw new RuntimeException("Too few columns in tablular raster file");
		}
		//	List<String> colNames = new ArrayList<String>(Arrays.asList(headertokens));

		for (int i=0; i<headertokens.length; i++) {
			headertokens[i] = headertokens[i].toLowerCase().replace("\"", "");
		}
		
		return headertokens;
	}
	
	public RasterSet<D> getRasterDataFromFile(String filename) {
		long startTime = System.currentTimeMillis();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			String line = in.readLine(); // read header
			dataColNames = handleHeader(parseLine(line));

			while ((line=in.readLine()) != null) {
				if (line.isEmpty()) // skip empty lines
					continue;
				
				String[] tokens = parseLine(line);
				
				try {
					double x = Double.parseDouble(tokens[getXCol()]);
					double y = Double.parseDouble(tokens[getYCol()]);
				
					Map<String, Double> rowValues = new HashMap<String, Double>(dataColNames.length);
					for (int i=2; i<tokens.length; i++) {
						try {
							double d = Double.parseDouble(tokens[i]);
							rowValues.put(dataColNames[i], d);
						}
						catch (Exception e) {
							LogWriter.println("Problem getting col: " + i + " for x: " + x + ", y: " + y + " - " + e.getMessage());
						}
					}
					
					D item = dataset.getFromCoordinates(x, y);
					RasterKey key = dataset.getKeyFromCoordinates(x, y);
					setData(key, item, rowValues);
				}
				catch (Exception e) {
					LogWriter.printlnError("Problem reading data line {" + line + "}");
					LogWriter.print(e);
					throw new RuntimeException(e);
				}
			}
			
			in.close();
		}
		catch (Exception e) {
			LogWriter.printlnError("Problem reading data file " + filename);
			LogWriter.print(e);
			throw new RuntimeException(e);
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					LogWriter.print(e);
				}
		}

		LogWriter.println("Reading " + filename + ", took " + (System.currentTimeMillis() - startTime) + " ms");

		return dataset;
	}
	
	protected int getColForName(String colName) {
		String lowerColName = colName.toLowerCase();
		int i=0;
		for (; i<colName.length(); i++)
			if (dataColNames[i].toLowerCase().equals(lowerColName))
				return i;
			
		return Integer.MIN_VALUE;
	}
	
	protected double getValueForCol(Map<String, Double> rowValues, String colName) {
		Double d = rowValues.get(colName.toLowerCase());
		if (d == null) {
			LogWriter.printlnError("Can't find column name " + colName);
			throw new RuntimeException("Can't find column name " + colName);
		}
		
		return d;
	}

	abstract protected void setData(RasterKey key, D item, Map<String, Double> rowValues);
}
