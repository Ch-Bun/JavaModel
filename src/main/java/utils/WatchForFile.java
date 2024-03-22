package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.Observable;

@SuppressWarnings("deprecation")
public class WatchForFile extends Observable implements WatchDir.Callback {

	private File fileToWaitFor;
	private WatchDir watchDir;
	private Thread thread;

	public WatchForFile(final File fileToWaitFor) {
		this.fileToWaitFor = fileToWaitFor;
		
		if (fileToWaitFor.exists()) {
			LogWriter.println("Marker file already exists: " + fileToWaitFor);
			return;
		}
					
		thread = new Thread("Watch Directory Thread") {
			@Override
			public void run() {
				
				Path dirToMonitor = fileToWaitFor.getParentFile().getParentFile().toPath(); // goes up a directory level as lowest directory not always exists before
				
				LogWriter.println(String.format("Scanning %s ... for %s ", dirToMonitor, fileToWaitFor));

				try {
					watchDir = new WatchDir(dirToMonitor, true, WatchForFile.this, StandardWatchEventKinds.ENTRY_CREATE); 
					checkForFile(); // just in case file has been created before we get here
					watchDir.processEvents(); // in here while (!stop)
				} 
				catch (IOException e) {
					LogWriter.printlnError("Problem watching directory " + e.getMessage());
					throw new RuntimeException(e);
				}			
				catch (ClosedWatchServiceException e) {
					//		LogWriter.println("Service still updating when closed.  Done it's job so doesn't matter.");
				}
			}
		};
		thread.start();  // the thread terminated by running through as while loop ended by watchDir.stop()
	}

	@Override
	public void dirChange(Path path, Kind<?> kind) {		
		LogWriter.println(String.format("Got  %s: %s", kind.name(), path));
		checkForFile();
	}
	
	public synchronized boolean checkForFile() {
		if (fileToWaitFor.exists()) {
			LogWriter.println("Found our marker file: " + fileToWaitFor);
			stop();
			notify();
			return true;
		}
		else
			return false;
	}

	private void stop() {
		if (watchDir != null)
			watchDir.stop();
	}

	public synchronized boolean await(long timeoutSec) {
		if (!fileToWaitFor.exists()) {
			try {
				wait(timeoutSec * 1000);
			} catch (InterruptedException e) {
				LogWriter.print(e);
			}
			finally {
				stop();
			}
		}
		return fileToWaitFor.exists();
	}
}
