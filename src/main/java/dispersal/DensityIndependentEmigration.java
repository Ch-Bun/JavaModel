package dispersal;

public class DensityIndependentEmigration extends EmigrationType {

	public DensityIndependentEmigration(EmigrationParameters emigrationParameters) {
		super(emigrationParameters);
	}

	//this is messy, arguements not used 
	@Override
	public double calculateEmigrationProbability(int Nt, double b) {
		return emigrationParameters.getdO();
	}

	@Override
	public double calculateEmigrationProbability(double dO, double alpha, double beta, int Nt, double b) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}

