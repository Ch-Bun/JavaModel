package utils;

import java.util.TreeMap;

public abstract class LazyTreeMap <K,V> extends TreeMap<K,V> {

	private static final long serialVersionUID = -4392188954477560133L;

	public LazyTreeMap() {
		super();
	}
	
	public V lazyGet(K key) {
		V value = get(key);
		if (value == null) {
			value = createValue();
			put(key, value);	
		}
		return value;
	}

	protected abstract V createValue();
}
