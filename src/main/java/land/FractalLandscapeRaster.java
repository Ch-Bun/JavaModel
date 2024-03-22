package land;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import raster.DoubleRasterItem;
import raster.RasterHeaderDetails;
import raster.RasterKey;
import raster.RasterSet;
import utils.LogWriter;
import utils.RandomNumberGenerator;

public class FractalLandscapeRaster extends RasterSet<DoubleRasterItem> {

	private static final long serialVersionUID = 3903276078507423808L;
	private final int width;
	private final int height;
	private double hurstExponent;
	private int featureSize; 


	public FractalLandscapeRaster(RasterHeaderDetails header, double hurstExp){
		super(header);
		width = this.getHeaderDetails().getNcolumns();
		height = this.getHeaderDetails().getNrows();
		featureSize = Math.min(width,height);
		hurstExponent = hurstExp;

		if(hurstExp < 0.0) {
			hurstExponent = 0;
			LogWriter.printlnWarning("User has specified hurst exponent for fractal as < 0, " + hurstExp +" setting to 0");
		}
		if(hurstExp > 1.0) {
			hurstExponent = 1.0;
			LogWriter.printlnWarning("User has specified hurst exponent for fractal as > 1, " + hurstExp +" setting to 1");
		}

		if(width != height)
			LogWriter.printlnWarning("Height and width of fractal landscape must be equal");

		if ((height % 2 == 0 )|| (width % 2 == 0 ))
			LogWriter.printlnWarning("Height and width must be an odd number for fractal landscape");


		generate();
	}



	private int imod( int a,  int b) {
		int res = a % b;
		if(res < 0) return res + b;
		return res;
	}

	private RasterKey getKey(int x,int y ) {
		//int xWrap = imod(x, width);
		//int yWrap = imod(y,height);

		RasterKey key  = new RasterKey(x, y);
		return key;
	}

	public List<Double> getMapValues() {

		List<Double> fractalValues = new ArrayList<Double>();

		for(DoubleRasterItem cellValue : this.values()) {

			fractalValues.add(cellValue.getDouble());
		}
		return fractalValues;
	}

	private void generate() {

		//	int featuresize = (int) Math.pow(2, Math.floor(Math.log(featureSize) / Math.log(2)));

		for (int y = 0; y < height; y += height-1) {
			for (int x = 0; x < width; x += width-1) {
				RasterKey key = new RasterKey(x,y);
				double randomDouble= RandomNumberGenerator.zeroToOne();  // JAA - 23 november 2022 negOnePosOne(); //needs to be between -1 and 1 
				DoubleRasterItem dblRasterItem = new DoubleRasterItem(randomDouble);
				put(key,dblRasterItem); 

			}
		}
		int gridsize = featureSize-1;
		double scale_factor = Math.pow(2.0, -hurstExponent);
		double scale = scale_factor;

		step(gridsize, scale);
	}


	private void step(int gridsize, double scale) {
		if (gridsize <= 1) {
			return;
		}

		this.diamond(gridsize, scale);
		this.square(gridsize, scale);
		scale *= scale;

		this.step(gridsize / 2, scale);
	}


	private void diamond(int gridsize, double scale) {
		int halfGridSize = (int) Math.floor(gridsize / 2);
		//  const magnitude = scale / this.grid.getSize();

		for (int y = halfGridSize; y < featureSize; y += gridsize) {
			for (int x = halfGridSize; x < featureSize; x += gridsize) {
				sampleDiamond(RandomNumberGenerator.negOnePosOne() * scale, x, y, halfGridSize);

			}
		}
	}

	private void square(int gridsize, double scale) {
		int halfGridSize = (int) Math.floor(gridsize / 2);
		//  const magnitude = scale / this.grid.getSize();

		for (int y = 0; y < height; y += gridsize) {
			for (int x = 0; x < width; x += gridsize) {
				int focalX = x + halfGridSize;
				int focalY = y;
				if(focalX <= width)
					sampleSquare( RandomNumberGenerator.negOnePosOne() * scale, focalX, focalY, halfGridSize);

				focalX = x;
				focalY = y + halfGridSize;
				if(focalY <= height)
					sampleSquare( RandomNumberGenerator.negOnePosOne() * scale, focalX, focalY, halfGridSize);


			}
		}

	}

	private void sampleDiamond(double value, int x, int y, int halfStep) {

		// a     b
		//    x
		// c     d
		List<RasterKey> keys = new ArrayList<RasterKey>();
		double avgValue =0;
		int count = 0;

		keys.add(getKey(x-halfStep,y-halfStep));
		keys.add(getKey(x+halfStep,y-halfStep));
		keys.add(getKey(x-halfStep,y+halfStep));
		keys.add(getKey(x+halfStep,y+halfStep));

		for (RasterKey key : keys) {
			if(get(key) != null) {
				avgValue += get(key).getDouble();
				count++;
			}
		}
		avgValue /= count;
		
	    DoubleRasterItem updatedValue = new DoubleRasterItem(avgValue+value);
		
		RasterKey key = new RasterKey(x,y); 
		if(get(key) != null)
			LogWriter.printlnWarning("existing raster cell at  " + x + " "+ y);
		else put(key,updatedValue);

	}

	private void sampleSquare(double value, int x, int y, int halfStep) {
		//   c
		//a  x  b
		//   d
		List<RasterKey> keys = new ArrayList<RasterKey>();
		double avgValue =0;
		int count = 0;

		keys.add(getKey(x-halfStep,y));
		keys.add(getKey(x+halfStep,y));
		keys.add(getKey(x,y+halfStep));
		keys.add(getKey(x,y-halfStep));

		for (RasterKey key : keys) {
			if(get(key) != null) {
				avgValue += get(key).getDouble();
				count++;
			}
		}
		avgValue /= count;

	    DoubleRasterItem updatedValue = new DoubleRasterItem(avgValue+value);
		
		RasterKey key = new RasterKey(x,y); 
		if(get(key) != null)
			LogWriter.printlnWarning("existing raster cell at  " + x + " "+ y);
		else put(key,updatedValue);

	}



}
