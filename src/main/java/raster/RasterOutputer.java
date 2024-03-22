package raster;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import utils.LogWriter;

public abstract class RasterOutputer<T, D extends RasterItem> {
	protected RasterSet<D> results;
	private String fileName;
	
	public RasterOutputer(RasterSet<D> results, String fileName) {
		this.results = results;
		this.fileName = fileName;
	}
	
	abstract public T getValue(RasterKey location);
	
	public int convertToPixelValue(T value) {
		throw new RuntimeException("RasterOutputer: if outputing image, need to override this method. ");
	}

	/** write raster output, but no image */
	public void writeOutput() {
		writeOutput(false);
	}
	
	public void writeOutput(boolean writeImage) {
	    BufferedWriter fileWriter = null;

	    try {
    		fileWriter = new BufferedWriter(new FileWriter(fileName,false));
    
	    	String nullDataString = results.getHeaderDetails().getNodataString();
	    	writeHeader(fileWriter);

			for (int row = 0; row < results.getNrows(); row++) {
				for (int col = 0; col < results.getNcolumns(); col++) {
					
					RasterKey location = new RasterKey(col, row);
					
					T d = getValue(location);
					
					if (d == null)
						fileWriter.write(nullDataString + " ");
					else
						fileWriter.write(d + " ");
				}
				fileWriter.newLine();
	    	}
	    }
	    catch (IOException e) {
			LogWriter.print(e);
	    }
	    finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} 
				catch (IOException e) {
					LogWriter.print(e);
				}
			}
	    }
	    
	    if (writeImage)
	    	writeImage();
	}

	private void writeHeader(BufferedWriter outputFile) throws IOException {
			
		outputFile.write("ncols         " + results.getNcolumns());
    	outputFile.newLine();
		outputFile.write("nrows         " + + results.getNrows());
    	outputFile.newLine();
		outputFile.write("xllcorner     " + results.getHeaderDetails().getXllCorner());
    	outputFile.newLine();
		outputFile.write("yllcorner     " + results.getHeaderDetails().getYllCorner());
    	outputFile.newLine();
		outputFile.write("cellsize      " + results.getHeaderDetails().getXCellSize());
    	outputFile.newLine();
		outputFile.write("NODATA_value  " + results.getHeaderDetails().getNodataString());
    	outputFile.newLine();
	}
	
	public byte[] getPixels(RasterSet<?> aRasterSet) {
		int width = aRasterSet.getNcolumns();
		int height = aRasterSet.getNrows();
		byte[] pixels = new byte[width*height];

		for (int row = 0; row < results.getNrows(); row++) {
			for (int col = 0; col < results.getNcolumns(); col++) {
				
				RasterKey key = new RasterKey(col, row);
				
				T v = getValue(key);
				if (v == null)
					continue;
				
				int d = convertToPixelValue(v);
				
				if (d != 0) {
				
					if (d > 11)
						d = 11;
				
					pixels[getIndexForRaster(key, width, 0, 0)] = (byte) d;
				}
			}
		}

		return pixels;
	}
	
	private int getIndexForRaster(RasterKey key, int width, int xOffset, int yOffset) {
		return (key.getRow() + xOffset) * width + key.getCol() + yOffset;
	}

	private Image createImage(RasterSet<?> aRasterSet) {
		byte[] pixels = getPixels(aRasterSet);
	
		DataBuffer dbuf = new DataBufferByte(pixels, pixels.length, 0);
		int bitMasks[] = new int[]{(byte)0xf};
		SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, aRasterSet.getNcolumns(), aRasterSet.getNrows(), bitMasks);

		WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);
		BufferedImage image = new BufferedImage(generateColorModel(), raster, false, null);
		return image;
	}

	public ColorModel generateColorModel() {
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];

		b[0]=(byte)255; g[0] = (byte)255; r[0] = (byte)255; // background colour
		b[1]=(byte)50;  g[1] = (byte)10; r[1] = (byte)10; 
		b[2] = (byte)0; g[2] = (byte)25; r[2]=(byte)80; 
		b[3] = (byte)0; g[3] = (byte)50; r[3]=(byte)120;
		b[4] = (byte)0; g[4] = (byte)75; r[4]=(byte)160;
		b[5] = (byte)0; g[5] = (byte)100; r[5]=(byte)190;
		b[6] = (byte)0; g[6] = (byte)130; r[6]=(byte)210;
		b[7] = (byte)0; g[7] = (byte)160; r[7]=(byte)230;
		b[8] = (byte)0; g[8] = (byte)190; r[8]=(byte)245;
		b[9] = (byte)0; g[9] = (byte)220; r[9]=(byte)245;
		b[10] = (byte)40; g[10] = (byte)245; r[10]=(byte)245;
		b[11] = (byte)130; g[11] = (byte)255; r[11]=(byte)245;

		return new IndexColorModel(4, 16, r, g, b);
	}

	private void writeImage() {
		try {
			ImageIO.write((RenderedImage) createImage(results), "jpeg", new File(fileName  + ".jpeg"));
		}
		catch (Exception e) {
			System.out.println("Problems saving image to  " +fileName);
			e.printStackTrace(System.err);
		}
	}
}