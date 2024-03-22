package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionHelper {

	public static <K, V> Map<V, List<K>> invertMap(Map<K, V> map) {
		
		Map<V, List<K>> result = new HashMap<V, List<K>>();
		
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (!result.containsKey(entry.getValue())) {
				result.put(entry.getValue(), new ArrayList<K>());
			}
			result.get(entry.getValue()).add(entry.getKey());
 		}
		
		return result;
	}
}