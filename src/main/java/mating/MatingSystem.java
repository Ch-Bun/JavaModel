package mating;

import java.util.List;
import creature.Creature;

public abstract class MatingSystem {
//popsizes for calculating DD reproduction, weights need to be added for each stage. 
	

	public abstract void mating(List<Creature> femalePool, List<Creature> malePool);
}
