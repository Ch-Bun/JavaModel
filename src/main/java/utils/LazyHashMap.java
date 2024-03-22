package utils;

import java.util.HashMap;

public abstract class LazyHashMap<K,V> extends HashMap<K,V> {

	private static final long serialVersionUID = -7446371661388216067L;

	public LazyHashMap() {
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
