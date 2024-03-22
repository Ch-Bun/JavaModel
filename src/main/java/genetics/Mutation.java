package genetics;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Mutation implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2549979815597462132L;
	protected final int id;
	private final double s;
	private final double h;
	private final static AtomicInteger idCounter = new AtomicInteger(0);
	
	protected Mutation(double s, double h){
		this.id=idCounter.incrementAndGet();
		this.s=s;
		this.h=h;
	}

	public int getId() {
		return id;
	}

	public double getS() {
		return s;
	}

	public double getH() {
		return h;
	}
	
	abstract int getAllele();
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mutation other = (Mutation) obj;
		return id == other.id;
	}


}
