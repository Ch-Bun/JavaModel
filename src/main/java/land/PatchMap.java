package land;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import global.ModelConfig;
import raster.IntegerRasterItem;
import raster.IntegerRasterReader;
import raster.RasterHeaderDetails;
import raster.RasterKey;
import raster.RasterSet;
import utils.LogWriter;

public class PatchMap extends HashMap<Patch, Set<RasterKey>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -982603002592004771L;
	private Map<Integer, Patch> patchAgents;
	private RasterSet<IntegerRasterItem> patchRaster;

	public void createPatches(LandscapeRaster landscapeRaster, RasterHeaderDetails desiredProjection, int year) {

		patchAgents = new HashMap<Integer, Patch>();

		if(ModelConfig.PATCH_BASED) {

			patchRaster = getPatchRasterFromFile(desiredProjection, year);
			
			createMatrixPatch(landscapeRaster);

			for (Entry<RasterKey, IntegerRasterItem> entry : patchRaster.entrySet()) {
				RasterKey key = entry.getKey();
				int patchId = entry.getValue().getInt();
				Cell cell = landscapeRaster.get(key);

				if(cell == null) {
					LogWriter.printlnError("No landscape cell at col: " + key.getCol() + 
							", row: " + key.getRow() + " for Patch " + patchId);
				}
				else {
					Patch patch = patchAgents.get(patchId);
					if(patch == null) {
						patch = new Patch(patchId, key.getRow(), key.getCol());//JAA - 22 november 2022
						patchAgents.put(patchId, patch);
					} 
					patch.addCell(cell);


					Set<RasterKey> keys = get(patch);

					if (keys == null) {
						keys = new HashSet<RasterKey>();
						put(patch, keys);
					}
					keys.add(key);
				}
			}
			LogWriter.println("Created " + patchAgents.size() + " patch agents from user input patch raster");
		}
		else {
			int patchIdCounter = 1;
			
			patchRaster = getPatchRaster(desiredProjection, year);

			for (Entry<RasterKey, Cell> entry : landscapeRaster.entrySet()) {
				Cell cell = entry.getValue();
				RasterKey key = entry.getKey();
                                double x = cell.getK();                                
				if(cell.getK() >= 0) { //JAA - 23 November 2022 - was if(cell.getK() >0)

					Patch patch = patchAgents.get(patchIdCounter);
					if(patch == null) {
						patch = new Patch(patchIdCounter, key.getRow(), key.getCol());//JAA - 21 november 2022
						patchAgents.put(patchIdCounter, patch);
						patch.addCell(cell);

					} 
					else LogWriter.printlnWarning("Assigning more than one cell to non-patch based landscape in patch: " + patchIdCounter + 
							", cell: " + cell.toString());

					Set<RasterKey> keys = get(patch);

					if (keys == null) {
						keys = new HashSet<RasterKey>();
						put(patch, keys);
					}
					keys.add(entry.getKey());

					IntegerRasterItem intItem = patchRaster.get(key);
					if (intItem == null) {
						patchRaster.put(key, new IntegerRasterItem(patchIdCounter));
					}
					
					else LogWriter.printlnWarning("Assigning more than one patch id to non-patch based landscape in patch: " + patchIdCounter + 
							", raster key: " + key.toString());
					
					patchIdCounter++;
				}
				else {
					
					Patch patch = patchAgents.get(ModelConfig.MATRIX_PATCH_ID);
					
					IntegerRasterItem intItem = patchRaster.get(key);
					if (intItem == null) {
						patchRaster.put(key, new IntegerRasterItem(ModelConfig.MATRIX_PATCH_ID));
					}
					
					if(patch == null) {
						patch = new Patch(ModelConfig.MATRIX_PATCH_ID, key.getRow(), key.getCol());//JAA - 21 november 2022
						patchAgents.put(ModelConfig.MATRIX_PATCH_ID, patch);
					} 
					patch.addCell(cell);
					
					Set<RasterKey> keys = get(patch);
					if (keys == null) {
						keys = new HashSet<RasterKey>();
						put(patch, keys);
					}
						keys.add(entry.getKey());
				}

			}	
		}

	}

	private RasterSet<IntegerRasterItem> getPatchRasterFromFile(RasterHeaderDetails desiredProjection, int year) {
		RasterSet<IntegerRasterItem> patches = new RasterSet<IntegerRasterItem>(desiredProjection) {
			private static final long serialVersionUID = 2467452274591854417L;

			@Override
			protected IntegerRasterItem createRasterData() {
				return new IntegerRasterItem(0);
			}
		};
		String filePath = (ModelConfig.DYNAMIC_LANDSCAPE) ? ModelConfig.TEMPORAL_DIR + File.separator + year  : ModelConfig.LANDSCAPE_DIR;
		IntegerRasterReader patchReader = new IntegerRasterReader(patches);
		patchReader.getRasterDataFromFile(filePath + File.separator + ModelConfig.PATCH_FILENAME);
		return patches;
	}

	private RasterSet<IntegerRasterItem> getPatchRaster(RasterHeaderDetails desiredProjection, int year) {
		RasterSet<IntegerRasterItem> patches = new RasterSet<IntegerRasterItem>(desiredProjection) {
			private static final long serialVersionUID = 2467452274591854417L;

			@Override
			protected IntegerRasterItem createRasterData() {
				return new IntegerRasterItem(0);
			}
		};
		return patches;
	}
	

	public Patch getPatchForLocation(RasterKey location) {

		Patch patch = null;

		IntegerRasterItem id=patchRaster.get(location);
		if(id != null)
			patch =  patchAgents.get(id.getInt());

		return patch;
	}

	public Set<Patch> getPatches() {
		return this.keySet();
	}

	public Patch getPatchForID(int id){

		return patchAgents.get(id);
	}

	public Map<Patch, Set<RasterKey>>  getInitialPatches() {

		if(ModelConfig.INITIALISE_SUBSET_PATCHES) {

			List<Integer> initialPatches = ModelConfig.INITIAL_PATCHES;

			if(initialPatches.size() < 1)
				LogWriter.printlnError("Initialising subset of patches but no patches have been specified");

			Map<Patch, Set<RasterKey>> entrySet = new HashMap<Patch, Set<RasterKey>>();

			for(Integer patchId : initialPatches) {

				Patch patch =  getPatchForID(patchId);
				Set<RasterKey> keys = get(patch);

				entrySet.put(patch, keys);

			}
			return entrySet;
		}
		else
			return this;
	}

	private void createMatrixPatch(LandscapeRaster landscapeRaster) {

		Patch matrixPatch = new Patch(ModelConfig.MATRIX_PATCH_ID, landscapeRaster.getKey(this).getRow(), landscapeRaster.getKey(this).getCol()); //patch Id for matrix = -1
		// JAA - 22 November 2022 
		Set<RasterKey> keys = get(matrixPatch);
		if (keys  == null) {
			keys = new HashSet<RasterKey>();
			put(matrixPatch, keys);

		for (Entry<RasterKey, Cell> entry : landscapeRaster.entrySet()) {
			Cell cell =entry.getValue();
			RasterKey key = entry.getKey();
			IntegerRasterItem patchId = patchRaster.get(key);
			if (patchId != null) 
				matrixPatch.addCell(cell);
			
			keys.add(key);
			}
			
			

		}
	}
	
}
