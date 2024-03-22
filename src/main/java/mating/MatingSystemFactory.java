package mating;

import global.ModelConfig;
import utils.LogWriter;

public class MatingSystemFactory {
	//swap for switch statement
	public static MatingSystem getMatingSystem() {
		String type = ModelConfig.MATINGSYSTEM;
		MatingSystem matingSystem = null;

		switch(type) {
		case "polygyny":
			matingSystem=new Polygyny();
			LogWriter.println("Mating system set as polygyny");
			break;
		case "promiscuity":
			matingSystem=new Promiscuity();
			LogWriter.println("Mating system set as promiscuity");
			break;
		case "monogamy":
			matingSystem=new Monogamy();
			LogWriter.println("Mating system set as monogamy");
			break;
		default:
			LogWriter.printlnError("No mating system type set for" + type);
		} 

		return matingSystem;
	}
}

