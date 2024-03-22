package dispersal;

public class TransferParameters {

	protected final Boolean evolving;
	protected final short maxStepsPerDispersalEvent;
	
	TransferParameters(boolean evolving, short maxStepsPerDispersalEvent){
		this.evolving=evolving;
		this.maxStepsPerDispersalEvent=maxStepsPerDispersalEvent;
	}
	
	public Boolean getEvolving() {
		return evolving;
	}

	public short getMaxStepsPerDispersalEvent() {
		return maxStepsPerDispersalEvent;
	}

}
