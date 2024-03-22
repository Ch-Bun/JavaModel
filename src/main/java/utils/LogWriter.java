package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import global.ModelConfig;

public class LogWriter {

	private static LogWriter logWriter;
	private PrintWriter logFile;
	
	public static synchronized void println(String s) {
		getLogWriter().print(s, System.out, true);
	}

	public static synchronized void print(String s) {
		getLogWriter().print(s, System.out, false);
	}
	
	public static synchronized void printlnError(String s) {
		getLogWriter().print("Error: " + s, System.err, true);
	}
	
	public static synchronized void printlnWarning(String s) {
		getLogWriter().print("Warning: " + s, System.err, true);
	}

	
	public static synchronized void print(Exception e) {
	    e.printStackTrace(getLogWriter().logFile);
	    
		if (!ModelConfig.SUPPRESS_STD_OUTPUT)
			e.printStackTrace();
	}
	
	private static LogWriter getLogWriter() {
		if (logWriter == null) {
			logWriter = new LogWriter(ModelConfig.OUTPUT_DIR + File.separator + "log.txt");
			logWriter.print(ModelConfig.getSetupDetails(), System.out, true);
		}
		return logWriter;
	}

	private LogWriter(String outputFileName) {
		try {
			File f = new File(outputFileName);
			if(f.getParentFile().exists()) 
				logFile = new PrintWriter(new BufferedWriter(new FileWriter(outputFileName,false)));
			else
				System.err.println("LogWriter: outputFileName: " + outputFileName + " not found.  Perhaps not set config file properly");
		}
		catch (IOException e) {
			System.err.print(e);
		}
	}

	private void print(String s, PrintStream stream, boolean newLine) {
		if (logFile != null) {
			logFile.write(s);
			if (newLine) logFile.write(System.lineSeparator());
			logFile.flush();
		}
	
		if (!ModelConfig.SUPPRESS_STD_OUTPUT) {
			stream.print(s);
			if (newLine) stream.println();
		}
	}

	// probably not called, but never mind as keep stream flushed
	protected void finalize() {
		logFile.close();
	}
}
