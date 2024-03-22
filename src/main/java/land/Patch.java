package land;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import creature.Creature;
import creature.Sex;
import creature.Stage;
import genetics.MutationType;
import lifecycle.LifeStageFactory;
import raster.RasterItem;
import utils.LogWriter;
import global.ModelConfig;
import java.util.List;
import utils.RandomNumberGenerator;


public class Patch implements RasterItem, Serializable {

	private static final long serialVersionUID = -5920784925038694415L;
	

	private final int patchId; //used for hashcode
	private float area=0; //in ha 
	private double k=0.0;
	//this needs more flexibility if we have multispecies 
	private Map<Stage,Map<Sex,Set<Creature>>> creatures;
	private double matedFemaleProportion;
        public boolean toBeDisturbed = false;
        private final int longitude, latitude; //JAA - 21 november 2022
        public int undisturbedGenerations = 1;


	public Patch(int id, int row, int col) {//JAA - 21 november 2022
		this.patchId=id;
                longitude = row*100;//JAA - 21 november 2022
                latitude = col*100; //JAA - 21 november 2022
		creatures = new HashMap<Stage,Map<Sex,Set<Creature>>>();

		Sex[] sexes = Sex.values();

		Set<Stage> stages =LifeStageFactory.getAllStages(Sex.FEMALE);
		for(Stage stage : stages) {
			creatures.put(stage, new HashMap<Sex,Set<Creature>>());
			for(Sex sex :sexes) {
				creatures.get(stage).put(sex, new HashSet<Creature>());
			}
		}
		if(ModelConfig.DISTURBANCE/* && undisturbedGenerations == ModelConfig.GENERAIONS_BETWEEN_DISTURBANCES*/){ //JAA - 23 november 2022
                        if(RandomNumberGenerator.zeroToOne()< ModelConfig.FRACTION_OF_PATCHES_TO_DISTURB){
                            toBeDisturbed = true;
 //                           undisturbedGenerations = 0; 
                        }
                }
 //               undisturbedGenerations++;		
	}
        
        public double getHeterozygosity(Sex sex, Stage stage){
           // System.out.println("first step inside getHeterozygosity");
            MutationType mT = new MutationType("a");
            double d =  0;
            int creatureSize = creatures.get(stage).get(sex).size(); 
            if(creatureSize > 1){ // Otherwise there is a risk of failure in the conversion from Map to List - I have not understood why
                //A failure in this conversion causes a crash of the program
                List<Creature> insects = creatures.get(stage).get(sex).parallelStream().toList();
                if(creatureSize == 1)
                    System.out.println("Size of insects = " + insects.size());
                try{
                    for(Creature crt : insects){
                        d += crt.genome.calculateNeutralHeterozygosity(mT);
                    }
                }        
                    catch (Exception e) {
                        System.out.println("Something went wrong in getHeterozygocity");
                    }
            //System.out.println("end of getHeterozygosity");
                return d/(double)insects.size();
            }
            else 
                return Double.NaN;
        };

	public double getArea() {
		return area;
	}

	public int getId() {
		return patchId;
	}
	
	public double getK() {
		return k;
	}
        
        public int getLongitude(){
            return longitude;
        }
        
        public int getLatitude(){
            return latitude;
        }
        
	public void addCell(Cell cell) {
	//	if (cells == null)
	//		cells = new HashSet<Cell>();
	//	cells.add(cell);
		area += cell.getArea();
		k += cell.getK();
	}
	
	public void resetK() {
		k = 0.0;
	}


	public void addCreature(Creature creature) {

		Map<Sex, Set<Creature>> sexMap = creatures.get(creature.getStage());
		Set<Creature> indList = sexMap.get(creature.getSex());
		indList.add(creature);
	}

	public void removeCreature(Creature creature) {
		Stage stage = creature.getStage();
		Sex sex = creature.getSex();


		Set<Creature> indList = creatures.get(stage).get(sex);
	
		boolean isRemoved =indList.remove(creature);
		if(!isRemoved) 
			LogWriter.printlnError("trying to remove creature id " + creature.getId() + " hashcode " +creature.hashCode() +" from patch " + patchId + " hashcode " + this.hashCode() + " but doesn't exist there");
	}


	//doesn't include transient individuals
	public Set<Creature> getStage(Sex gender, Stage stage){

		Set<Creature> adults = creatures.get(stage).get(gender).stream().filter(u -> u.isSettled() && u.isAlive()).collect(Collectors.toSet());
		return adults;
	}

	public int getStageSize(Sex gender, Stage stage){

		int size = creatures.get(stage).get(gender).size();
		return size;
	}
	
	public void resetProportionOfMatedFemales() {
		matedFemaleProportion=-9999.9;
		
	}
	
	public void setProportionOfMatedFemales(double proportionOfMatedFemales) {
		matedFemaleProportion = proportionOfMatedFemales;
	}
	
	public double getProportionOfMatedFemales() {
		return matedFemaleProportion;
	}

	public Map<Stage, Map<Sex, Set<Creature>>> getCreatures() {
		return creatures;
	}
	public int getNt(Sex sex, Stage stage) {

		return creatures.get(stage).get(sex).size();

	}

	public int getNt(Sex sex) {

		Set<Stage> stages =LifeStageFactory.getAllStages(sex);
		int Nt=0;
		for(Stage stage : stages) {
			Nt += creatures.get(stage).get(sex).size();
		}
		return Nt;
	}

	
	public int getSettledNt(Sex sex) {

		Set<Stage> stages =LifeStageFactory.getAllStages(sex);
		int Nt=0;
		for(Stage stage : stages) {
			Nt += creatures.get(stage).get(sex).stream().filter(u -> u.isSettled()).collect(Collectors.toList()).size();
		}
		return Nt;
	}
	

	public int getFecundNt(Sex sex) {

		Set<Stage> stages =LifeStageFactory.getAllStages(sex);
		int Nt=0;
		for(Stage stage : stages) {
			
			if(LifeStageFactory.getLifeStage(stage, sex).getDemography().getFecundity() > 0.0 ) {
				Nt += creatures.get(stage).get(sex).size();
			}
		}
		return Nt;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + patchId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patch other = (Patch) obj;
		if (patchId != other.patchId)
			return false;
		return true;
	}
        


}
