package land;

import java.io.Serializable;
import java.util.Objects;

import global.ModelConfig;

// currently immutable class, perhaps needs to be mutable if K,mortality,cost was
//to change with dynamic landscape 
public class HabitatType implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8032076186044673510L;
	private final int id;
	private final String habitat;
	private final double K; // or b 
	private final double mortality;
	private final double cost;
	
	HabitatType(HabitatBuilder habitatBuilder) {
		this.id = habitatBuilder.id;
		this.habitat=habitatBuilder.habitat;
		this.K=habitatBuilder.K;
		this.mortality = habitatBuilder.mortality;
		this.cost = habitatBuilder.cost;
	}
	
	public Integer getId() {
		return id;
	}

	
	public String getHabitat() {
		return habitat;
	}
	
	public double getK() {
		return K;
	}

	
	public double getMortality() {
		return mortality;
	}

	public double getCost() {
		return cost;
	}

	@Override
	public String toString() {
		return "HabitatType " + id;
	}

	public static class HabitatBuilder {
		private final int id;
		private final String habitat;
		private final double K;
		private double mortality;
		private double cost;
		
		public HabitatBuilder(int id, String habitat, int k) {
			super();
			this.id = id;
			this.habitat = habitat;
			K = k * (ModelConfig.CELLSIZE * ModelConfig.CELLSIZE)/10000.0; //scale K such that it is K per ha
		}

        public void mortality(double mortality) {
			this.mortality = mortality;
		}

		public void cost(double cost) {
			this.cost = cost;
		}



		public HabitatType build() {
        	HabitatType habitatType =  new HabitatType(this);
            return habitatType;
        }
		
		
	}

	@Override
	public int hashCode() {
		return Objects.hash(habitat, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HabitatType other = (HabitatType) obj;
		return Objects.equals(habitat, other.habitat) && id == other.id;
	}


}
