package creature;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lifecycle.LifeStageFactory;

public class Metapopulation extends HashMap<Stage,HashMap<Sex,Set<Creature>>>  {

	private final CreatureFactory creatureFactory = new ButterflyFactory(); 

	private static final long serialVersionUID = -1248057791218498194L;

	public Metapopulation() {
		Set<Stage> stages = LifeStageFactory.getAllStages(Sex.FEMALE);

		for(Stage stage : stages) {
			put(stage,new HashMap<Sex,Set<Creature>>());
			for(Sex sex : Sex.values()) {
				get(stage).put(sex, new HashSet<Creature>());
			}
		}

	}

	public CreatureFactory getFactory() {
		return creatureFactory;
	}

	public void addCreature(Creature creature) {
		get(creature.getStage()).get(creature.getSex()).add(creature);
	}

	public boolean removeCreature(Creature creature) {
		return get(creature.getStage()).get(creature.getSex()).remove(creature);
	}

	//have to do below to allow metapopulation container to be updated 
	public Set<Creature> setView(){

		Set<Creature> allCreatures = new HashSet<Creature>();
		for(Entry<Stage, HashMap<Sex, Set<Creature>>> entry : entrySet()) {
			for(Entry<Sex, Set<Creature>> genderMap : entry.getValue().entrySet()) {
				for(Creature creature : genderMap.getValue()) {
					allCreatures.add(creature);
				}
			}
		}
		return Collections.unmodifiableSet(allCreatures);
	}
}

