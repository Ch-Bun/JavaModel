package utils;

import java.util.stream.Stream;

/**
 * @param <T> the object type this factory creates.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz WilhelmstÃ¶tter</a>
 * @since 1.0
 * @version 3.0
 */
@FunctionalInterface
public interface Factory<T> {

	/**
	 * Create a new instance of type T.
	 *
	 * @return a new instance of type T
	 */
	T newInstance();

	/**
	 * Return a new stream of object instances, created by this factory.
	 *
	 * @since 3.0
	 *
	 * @return a stream of objects, created by this factory
	 */
	default Stream<T> instances() {
		return Stream.generate(this::newInstance);
	}

}
