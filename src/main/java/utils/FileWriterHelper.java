package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import global.ModelConfig;

public class FileWriterHelper {

	static public BufferedWriter getFileWriter(LocalDate timestep, String file, String columnHeadings) throws IOException {
		return getFileWriter(timestep.getYear() == ModelConfig.START_DATE.getYear(), false, file, columnHeadings);
	}
	
	static public BufferedWriter getFileWriter(boolean initialise, boolean addShutdownHook, String file, String columnHeadings) throws IOException {
		FileWriter fstream = new FileWriter(file, !initialise);
		final BufferedWriter outputFile = new BufferedWriter(fstream);
		
		if (initialise) {
			outputFile.write(columnHeadings);
			outputFile.newLine();
		
			if (addShutdownHook) {
		 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					public void run() {
						try {
							outputFile.close();
						} catch (IOException e) {
							LogWriter.print(e);
						}
					}
				}));
			}
		}
		
		return outputFile;
	}
}
