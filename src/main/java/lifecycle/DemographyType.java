package lifecycle;

import java.util.Objects;

public class DemographyType {

	private final int minAge;
	private final int maxAge;
	private final Double  fecundity;
	private final Double  survival;
	private final Double  transition;
	private final Double  fecundityDD; //these are not used but could be used as scaling factors for each sex/stage for strength of DD
	private final Double  survivalDD;
	private final Double  transitionDD;
	private final Boolean disperses;
	
	public DemographyType(DemographyBuilder demographyBuilder) {
		this.minAge=demographyBuilder.minAge;
		this.maxAge = demographyBuilder.maxAge;
		this.fecundity = demographyBuilder.fecundity;
		this.survival = demographyBuilder.survival;
		this.transition = demographyBuilder.transition;
		this.fecundityDD = demographyBuilder.fecundityDD; 
		this.survivalDD = demographyBuilder.survivalDD;
		this.transitionDD = demographyBuilder.transitionDD;
		this.disperses = demographyBuilder.disperses;
	}

	public int getMinAge() {
		return minAge;
	}
	
	public int getMaxAge() {
		return maxAge;
	}

	public Double getFecundity() {
		return fecundity;
	}

	public Double getSurvival() {
		return survival;
	}

	public Double getTransition() {
		return transition;
	}
	
	public Double getFecundityDD() {
		return fecundityDD;
	}

	public Double getSurvivalDD() {
		return survivalDD;
	}

	public Double getTransitionDD() {
		return transitionDD;
	}

	public Boolean disperses() {
		return disperses;
	}
	
	public static class DemographyBuilder{
	
		private final int minAge;
		private final int maxAge;
		private Double fecundity;
		private final Double survival;
		private final Double transition;
		private Double fecundityDD=0.0; //default is no DD 
		private Double survivalDD=0.0;//default is no DD 
		private Double transitionDD=0.0;//default is no DD 
		private Boolean disperses;
		
		public DemographyBuilder(int minAge, int maxAge, Double survival, Double transition, Boolean disperses) {
			super();
			this.minAge = minAge;
			this.maxAge = maxAge;
			this.survival = survival;
			this.transition = transition;
			this.disperses = disperses;
		}

		public void fecundity(Double fecundity) {
			this.fecundity = fecundity;
		}

		public void fecundityDD(Double b) {
			this.fecundityDD = b;
		}

		public void survivalDD(Double survivalDD) {
			this.survivalDD = survivalDD;
		}

		public void transitionDD(Double transitionDD) {
			this.transitionDD = transitionDD;
		}
		
		public void disperses(Boolean disperses) {
			this.disperses=disperses;
		}
	
        public DemographyType build() {
        	DemographyType demographyType =  new DemographyType(this);
            return demographyType;
        }
	}

	@Override
	public int hashCode() {
		return Objects.hash(fecundity, fecundityDD, maxAge, survival, survivalDD, transition, transitionDD);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DemographyType other = (DemographyType) obj;
		return Objects.equals(fecundity, other.fecundity) && Objects.equals(fecundityDD, other.fecundityDD)
				&& Objects.equals(maxAge, other.maxAge) && Objects.equals(survival, other.survival)
				&& Objects.equals(survivalDD, other.survivalDD) && Objects.equals(transition, other.transition)
				&& Objects.equals(transitionDD, other.transitionDD);
	}
	
	
}

