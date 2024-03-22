package lifecycleevents;

import java.time.LocalDate;

import creature.Metapopulation;
import land.LandscapeRaster;
import land.PatchMap;


public interface LifeCycleEvent {

  void execute(Metapopulation metapopulation, LandscapeRaster landscape, PatchMap patchMap, LocalDate date, boolean balancing);
}
