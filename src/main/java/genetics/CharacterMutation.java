package genetics;

import java.util.Objects;

public class CharacterMutation extends Mutation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4663423843301291795L;
	private final char allele; 

	CharacterMutation(double h, double s, char c) {
		super(s,h);
		this.allele=c;
	}
	
	//this is neutral character mutation, if file has dominance distribution set as #
	CharacterMutation(char c) {
		super(0.0,0.0);
		this.allele=c;
	}
	
	public int getAllele() {
		return allele;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(allele);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CharacterMutation other = (CharacterMutation) obj;
		return allele == other.allele;
	}

}
