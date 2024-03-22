package creature;

/** This is currently really just a marker class to aid type safety, using Integer directly would have worked, but been less clear*/
public class Age {
	private Integer age;
	
	Age(Integer age) {
		this.age = age;
	}
	
	public Integer getAge() {
		return age;
	}

	@Override
	public String toString() {
		return "age" + age;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Age other = (Age) obj;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (!age.equals(other.age))
			return false;
		return true;
	}
}
