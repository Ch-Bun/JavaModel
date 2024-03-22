package creature;

import java.time.LocalDate;
import land.Patch;


//class of butterfly is also a butterfly factory 
public class Butterfly extends Creature {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2962175984026040286L;

	protected Butterfly(ButterflyBuilder builder) {
		super(builder);
	}
	/*
	 * Butterfly(int id, Stage lifestage,int age, Sex sex) { ButterflyBuilder(id,
	 * lifestage,sex, age); }
	 * 
	 * Butterfly(int id, Stage lifestage,int age, Sex sex, Chromosome maternal,
	 * Chromosome paternal) { super(id, lifestage,sex, age, maternal,paternal); }
	 */

	public static class ButterflyBuilder extends Creature.CreatureBuilder<ButterflyBuilder> {

		public ButterflyBuilder(int id, Stage stage, int age, Sex sex, LocalDate date, Patch patch, double xLocation, double yLocation) {
			super(id, stage, age, sex, date, patch, xLocation, yLocation);
		}  

		public Butterfly build() { return new Butterfly(this); }
	}

}
