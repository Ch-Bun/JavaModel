package creature;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import genetics.Chromosome;
import genetics.Genome;
import genetics.MutationType;
import genetics.Trait;
import land.Patch;
import utils.Position;

public class Creature  implements Serializable {


	private static final long serialVersionUID = -2640049284688989822L;
	//fixed
	protected final int id;
	protected final Sex sex;
	public final Genome genome;
	protected final LocalDate dateOfBirth; 
	protected int maternalId;
	protected final Patch natalPatch;

	//changing
	protected int age;
	protected Stage stage;
	protected double xLocation;
	protected double yLocation;
	protected transient Patch currentPatch;
	protected List<Creature> mates; 
	protected boolean settled;
	protected boolean disperser;
	protected boolean alive;
	protected double directionOfTravel;
	protected short stepsFromNearestPatch=0;
	protected List<Position> path;


	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Creature other = (Creature) obj;
		return id == other.id;
	}

	protected Creature(CreatureBuilder<?> creatureBuilder) {
		this.id = creatureBuilder.id;
		this.sex = creatureBuilder.sex;
		this.genome = creatureBuilder.genome;
		this.stage = creatureBuilder.stage;
		this.age = creatureBuilder.age;
		this.settled=creatureBuilder.settled;
		this.xLocation=creatureBuilder.xLocation;
		this.yLocation=creatureBuilder.yLocation;
		this.dateOfBirth=creatureBuilder.dateOfBirth;
		this.currentPatch=creatureBuilder.natalPatch;
		this.directionOfTravel=-9999.9;
		this.alive=creatureBuilder.alive;
		this.maternalId=creatureBuilder.maternalId;
		this.disperser=false;
		this.natalPatch=creatureBuilder.natalPatch;
		this.path = new ArrayList<Position>();

	}

	public int getId(){
		return id;
	}

	public Stage getStage(){
		return stage;
	}

	public Sex getSex(){
		return sex;
	}

	public int getAge() {
		return age;
	}

	public boolean isSettled() {
		return settled;
	}

	public double getXLocation() {
		return xLocation;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isDisperser() {
		return disperser;
	}

	public Patch getNatalPatch() {
		return natalPatch;
	}

	//	public void setClutchSize() {
	//		numberOfClutches = RandomNumberGenerator.samplePoisson(ModelConfig.MEANCLUTCHES);
	//		if(numberOfClutches <= 0)
	//			killCreature();
	//	}
	//
	//	public void laidClutch() {
	//		numberOfClutches--;
	//		if(numberOfClutches <= 0)
	//			killCreature();
	//	}


	public int getMaternalId() {
		return maternalId;
	}	

	public void killCreature() {
		alive =false;
	}

	public double getYLocation() {
		return yLocation;
	}

	public void setXLocation(double x) {
		xLocation =x;
	}

	public void setYLocation(double y) {
		yLocation = y;
	}

	public Patch getCurrentPatch() {
		return currentPatch;
	}

	public void setDirectionOfTravel(double theta) {
		directionOfTravel = theta;
	}

	public double getDirectionOfTravel() {
		return directionOfTravel;
	}
	//kind of like an observer pattern, but single location observer
	public void move (double newXLocation, double newYLocation, Patch newPatch) {
		if(currentPatch != null)
			currentPatch.removeCreature(this);
		xLocation = newXLocation;
		yLocation = newYLocation;
		currentPatch = newPatch;
		if(currentPatch != null)
			currentPatch.addCreature(this);
		this.addToPath(newXLocation, newYLocation);
	}

	//for shuffling within patch
	public void move (double newXLocation, double newYLocation) {
		xLocation = newXLocation;
		yLocation = newYLocation;
	}

	public int age() {
		this.age++;
		return age;
	}

	public void age(Stage newStage) {
		if(currentPatch != null) {
			currentPatch.removeCreature(this);
			this.stage=newStage;
			currentPatch.addCreature(this);
		}
		else this.stage=newStage;
	}

	public void remove() {
		if(currentPatch != null)
			currentPatch.removeCreature(this);
	}

	public void updateCurrentPatch(Patch patch) {
		currentPatch = patch;
	}

	public LocalDate getDOB() {
		return dateOfBirth;
	}

	public void addMate(Creature mate) {

		if(mates == null)
			mates = new ArrayList<Creature>();
		mates.add(mate);
	}

	public void clearMates() {
		if(mates != null)
			mates.clear();
	}

	public void settle() {
		settled=true;
	}

	public void unsettle() {
		settled=false;
	}


	public void emigrate() {
		disperser=true;
		this.unsettle();
	}

	public Chromosome inheritance() {
		return genome.inherit();
	}

	public List<Creature> getMates(){
		return mates;
	}

	public double getQTLValueFromGenome(Trait trait) {

		return genome.getQTLValueFromGenome(sex, trait);

	}

	public boolean checkForAllele(char allele,Trait trait) {
		Sex sex = Sex.BOTH;
		return genome.containsQTLAllele(allele, sex, trait);
	}

	public boolean containsMutationType(MutationType mutationType) {
		return genome.containsMutationType(mutationType);
	}

	public int countMutations(MutationType mutationType) {
		return genome.countMutations(mutationType);
	}

	public boolean isHomozygote(MutationType mutationType) {
		return genome.isHomozygote(mutationType);
	}    


	public void incrementSteps() {
		stepsFromNearestPatch++;
	}

	public short getStepsFromNearestPatch() {
		return stepsFromNearestPatch;
	}

	public void resetStepCount() {
		stepsFromNearestPatch=0;
	}


	public boolean isHome() {
		return currentPatch == natalPatch;
	}

	public void addToPath(double x, double y) {
		Position position = new Position(x,y);
		path.add(position);
	}

	public List<Position> getPath(){
		return path;
	}

	public static class CreatureBuilder<T extends CreatureBuilder<T>>{

		int id;
		int age;
		Stage stage;
		Sex sex;
		boolean settled;
		boolean alive;
		double distanceMoved;
		Genome genome;
		LocalDate dateOfBirth; 
		Patch natalPatch;
		double xLocation;
		double yLocation;
		int maternalId;

		public CreatureBuilder(int id, Stage stage, int age, Sex sex, LocalDate dateOfBirth,Patch natalPatch, double xLocation, double yLocation) {
			this.id = id;
			this.stage = stage;
			this.age=age;
			this.dateOfBirth=dateOfBirth;
			this.natalPatch=natalPatch;
			this.xLocation=xLocation;
			this.yLocation=yLocation;
			this.sex=sex;
			this.settled=true;
			this.distanceMoved=0.0;
			this.alive=true;
		}

		public void genome(Chromosome maternalChromosome, Chromosome paternalChromosome) {
			this.genome = new Genome(maternalChromosome, paternalChromosome);
		}

		public void genome() {
			this.genome = new Genome();
		}

		public void maternalId(int maternalId) {
			this.maternalId=maternalId;
		}

		public void maternalId() {
			this.maternalId=-9999;
		}

		public Creature build() {
			Creature creature =  new Creature(this);
			return creature;
		}
	}





}
