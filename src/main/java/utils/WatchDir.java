package utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class WatchDir {

	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	private boolean recursive;
	private final Callback callback;
	private boolean stop = false;

	public static interface Callback {
		public void dirChange(Path path,  Kind<?> kind);
	}

	public void stop() {
		try {
			watcher.close();
		} 
		catch (IOException e) {
			LogWriter.print(e);
		}
		this.stop = true;
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir, Kind<?>... events) throws IOException {

		if (events == null || events.length == 0) // if events are not specified call with all types
			register(dir, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		else {
			WatchKey key = dir.register(watcher, events);
			keys.put(key, dir);
		}
	}
	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start, final Kind<?>... events) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir, events);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public WatchDir(Path dir, boolean recursive, Callback callback, Kind<?>... events) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();
		this.callback = callback;
		this.recursive = recursive;

		if (recursive) {
			//LogWriter.println(String.format("Scanning %s ...", dir));
			registerAll(dir, events);
		}
		else {
			register(dir, events);
		}
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void processEvents() {
		while (!stop) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				LogWriter.printlnError("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event: key.pollEvents()) {
				Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				//LogWriter.format("%s: %s\n", kind.name(), child);
				callback.dirChange(child, kind);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException x) {
						LogWriter.printlnError("WatchDir. Error on event " + x);
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}    
}