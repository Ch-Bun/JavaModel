package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/** Very basic.... For example, could and should be made more generic to handle more than strings */
public class StringTabularReader {
	private String delimiterRegex;
	private String[] columnNames;
	private List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();

	public StringTabularReader(String delimiterRegex, String[] columnNames) {
		this.delimiterRegex = delimiterRegex;
		this.columnNames = columnNames;
	}
	
	private Map<String, Integer> handleHeader(String[] headertokens) {
		Map<String, Integer> headerMap = new HashMap<String, Integer>();
		
		if (headertokens.length < columnNames.length) {
			LogWriter.printlnError("Too few columns");
			throw new RuntimeException("AbstractTabularReader: Too few columns in tablular file");
		}
		
		for (int i=0; i<headertokens.length; i++) {
			headerMap.put(headertokens[i], i);
		}
		return headerMap;
	}

	public List<Map<String, String>> read(String filename) {
		rowList = new ArrayList<Map<String, String>>();
		
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename)); 
			String line = fileReader.readLine(); // read header
			Map<String, Integer> colNameMap = handleHeader(parseLine(line));

			while ((line=fileReader.readLine()) != null) {
				Map<String, String> valuesMap = new HashMap<String, String>();
				String[] tokens = parseLine(line);
				
				if (tokens.length < columnNames.length)
					LogWriter.printlnError("Too few columns in " + filename + ", " + line);
				
				for (String colString : columnNames) {
					Integer colIndex = colNameMap.get(colString);
					if (colIndex == null) {
						LogWriter.printlnError("Can't find column " + colString);
					}
					valuesMap.put(colString, tokens[colIndex]);
				}
				rowList.add(valuesMap);
			} 
			fileReader.close(); 
			
		} catch (IOException e) {
			LogWriter.printlnError("AbstractTabularReader: Failed in reading file " + filename);
			LogWriter.print(e);
		}
		LogWriter.println("Processed " + filename + ", create " + rowList.size() + " rows");
		return rowList;
	}
	
	private String[] parseLine(String line) {
		return line.trim().split(delimiterRegex);
	}

	/** Queries table but expect single value to be found, throws a RuntimeException if not */
	public Map<String, String> querySingle(Map<String, String> queryMap) {
		List<Map<String, String>> rows = query(queryMap);
		if (rows.size() != 1)
			throw new RuntimeException("Single row not found for " + queryMap + ", " + rows.size());
		
		return rows.get(0);
	}
	
	public List<Map<String, String>> query(Map<String, String> queryMap) {
		List<Map<String, String>> matchingRows = new ArrayList<Map<String, String>>();
		
	    rowLoop:
		for (Map<String, String> rowVals : rowList) {
		    for (Entry<String, String> entry : queryMap.entrySet()) {
		    	String rowV = rowVals.get(entry.getKey());
				if (!entry.getValue().equals(rowV))
					continue rowLoop; // goes to next row, and so skips .add() below
			}
			matchingRows.add(rowVals);
		}
		
		return matchingRows;
	} 
}
