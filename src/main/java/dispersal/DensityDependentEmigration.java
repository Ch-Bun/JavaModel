package dispersal;

public class DensityDependentEmigration extends EmigrationType {

	public DensityDependentEmigration(EmigrationParameters emigrationParameters) {
		super(emigrationParameters);
	}
	
	public double calculateEmigrationProbability(double dO, double alpha, double beta, int Nt, double b) {
	
		 double emProb = dO/1+Math.exp(-b/Nt-beta)*alpha;	
		
		return emProb;
	}
	
	public double calculateEmigrationProbability(int Nt, double b) {
		
		 double dO = emigrationParameters.getdO();
		 double alpha = emigrationParameters.getAlpha();
		 double beta = emigrationParameters.getBeta();
	
		 double emProb = dO/1+Math.exp(-b/Nt-beta)*alpha;	
		return emProb;
	}
	
}
