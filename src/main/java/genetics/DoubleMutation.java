package genetics;

public class DoubleMutation extends Mutation{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4082570832398569241L;

	DoubleMutation(double s, double h) {
		super(s,h);
	}
	
	@Override
	int getAllele() {
	return id;
	}

}
