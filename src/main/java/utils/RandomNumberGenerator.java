package utils;

import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class RandomNumberGenerator {


	// private static Random randomNumberGenerator =  new Random();
	private static final Random randomNumberGenerator =  new Random();
	
//	static {
//		randomNumberGenerator.setSeed(1);
//	}
	
		public static Random getSeed() {
			return randomNumberGenerator;
		}
	 
	    public static int getZeroOrOne() { 
	    	    return randomNumberGenerator.nextDouble() >= 0.5? 1 : 0;
	    }
	    
		public static double negOnePosOne() {
			return randomNumberGenerator.nextDouble() * 2 - 1;
		}
		
		public static double zeroToOne() {
			return randomNumberGenerator.nextDouble();
		}
		
		public static int getRandomIndex(int arraySize) {
			return randomNumberGenerator.nextInt(arraySize);
		}
		
		public static boolean getBoolean() {
			return randomNumberGenerator.nextBoolean();
		}
		
		public static int samplePoisson(double lambda) {
			PoissonDistribution pois = new PoissonDistribution(lambda);
			//pois.reseedRandomGenerator((long)zeroToOne());
			return pois.sample();
		}
		
		public static char sampleAlphabet() {
			Character randomisedCharacter = (char) (randomNumberGenerator.nextInt(26) + 'a');
			return Character.toUpperCase(randomisedCharacter);
		}
}
