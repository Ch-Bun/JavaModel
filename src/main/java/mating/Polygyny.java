package mating;

import java.util.List;
import java.util.Collections;

import creature.Creature;
import global.ModelConfig;
import utils.RandomNumberGenerator;

public class Polygyny extends MatingSystem {

	@Override
	public void mating(List<Creature> femalePool, List<Creature> malePool) {

		if(!malePool.isEmpty()) {
			List<Creature> matingMales;

			if(malePool.size() > ModelConfig.MATINGMALESSIZE) {
				Collections.shuffle(malePool, RandomNumberGenerator.getSeed());
				matingMales = malePool.subList(0, ModelConfig.MATINGMALESSIZE); 
			}
			else
				matingMales = malePool;

			for(Creature female : femalePool) {
				female.clearMates(); 			//clear previous sperm
				double probabilityOfMating = 1.0;
				//basically female carries sperm from male chosen from pool of males in patch, 
				if(RandomNumberGenerator.zeroToOne() < probabilityOfMating) {
					Creature mate = matingMales.get(RandomNumberGenerator.getRandomIndex(matingMales.size()));
					female.addMate(mate);
				}

			}

		}

	}

}
