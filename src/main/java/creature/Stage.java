package creature;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import utils.LogWriter;

public enum Stage implements Serializable {

	EGG(0,"egg"),
	JUVENILE(1,"juvenile"),
	ADULT(2,"adult");

	private Integer stageId;
	private String stageName;


	Stage(Integer stageId, String stageName) {
		this.stageId = stageId;
		this.stageName = stageName;
	}
	
	public Integer getStageId() {
		return stageId;
	}

	public String getStageName() {
		return stageName;
	}
	
	@Override
	public String toString() {
		return stageName;
	}
	
	private static final Map<Integer, Stage> lowercaseCache = new HashMap<Integer, Stage>();
	static {
		for (Stage stage : values()) {
			lowercaseCache.put(stage.getStageId(), stage);
		}
	}

	public static Stage getFromCache(int id) {
		Stage s = lowercaseCache.get(id);

		if (s == null)
			LogWriter.printlnError("Can't find stage for " + id);

		return s;
	}

	public static Stage getNextStage(Stage stageNow) {
		int nextStage =stageNow.getStageId()+1;
		return getFromCache(nextStage);
	}

}