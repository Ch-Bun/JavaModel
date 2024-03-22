package lifecycle;

import creature.Stage;
import dispersal.EmigrationType;
import dispersal.Settlement;
import dispersal.TransferType;

//this is basically a parameter container for accessing parameters for each lifestage
public class LifeStage {

	private final Stage stage;
	private final DemographyType demographyCharacteristics;
	private final EmigrationType emigrationType;
	private final TransferType transferType;
	private final Settlement settlement;

	LifeStage(Stage stage,DemographyType demographyCharacteristics,EmigrationType emigrationType,
			TransferType transferType, Settlement settlementType) {
		this.stage=stage;
		this.demographyCharacteristics = demographyCharacteristics;
		this.emigrationType = emigrationType;
		this.transferType=transferType;
		this.settlement = settlementType;

	}
	
	LifeStage(Stage stage,DemographyType demographyCharacteristics) {
		this.stage=stage;
		this.demographyCharacteristics = demographyCharacteristics;
		this.emigrationType = null;
		this.transferType=null;
		this.settlement =null;
	}
	
	
	public DemographyType getDemography() {
		return demographyCharacteristics;
	}

	public EmigrationType getEmigrationType() {
		return emigrationType;
	}

	public TransferType getTransferType() {
		return transferType;
	}

	public Settlement getSettlementType() {
		return settlement;
	}

	public Stage getStage() {
		return stage;
	}
}
