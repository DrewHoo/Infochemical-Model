/**
 * 
 */
package repast.model.heatbugs;

import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

/**
 * @author DrewHoo
 * This is basically Jerry Vos's implementation of ValueLayerDiffuser without differing weights
 * for Von Neumann neighbors and Moore neighbors
 *
 */
public class InfoChemDiffuser extends ValueLayerDiffuser {
	public InfoChemDiffuser(IGridValueLayer valueLayer, double evaporationFactor, double propagationFactor, boolean toroidal) {
		super(valueLayer, evaporationFactor, propagationFactor, toroidal);
		
	}
	
	@Override
	public void diffuse() {
	    int size = valueLayer.getDimensions().size();
	    if (size == 1) {
	    	double[] newVals = (double[]) computedVals;
	    	for (int x = 0; x < newVals.length; x++) {
	      		valueLayer.set(newVals[x], x);
    		}
	    } else if (size == 2) {
	     	double[][] newVals = (double[][]) computedVals;
	    	for (int x = 0; x < newVals.length; x++) {
	        	for (int y = 0; y < newVals[0].length; y++) {
	          		valueLayer.set(newVals[x][y], x, y);
	        	}
	      	}
	    } else {
	      	double[][][] newVals = (double[][][]) computedVals;
	      	for (int x = 0; x < newVals[0].length; x++) {
	        	for (int y = 0; y < newVals[0][0].length; y++) {
	          		for (int z = 0; z < newVals.length; z++) {
	            		valueLayer.set(newVals[x][y][z], x, y, z);
	          		}
        		}
	      	}
	    }
	}

	protected void evaporate() {
		int size = valueLayer.getDimensions().size();
      	int width = (int) valueLayer.getDimensions().getWidth();
	    if (size == 1) {
	    	for (int x = 0; x < width; x++) {
	      		valueLayer.set(valueLayer.get(x)*evaporationConst, x);
    		}
	    } else if (size == 2) {
	    	int height = (int) valueLayer.getDimensions().getHeight();
	    	for (int x = 0; x < width; x++) {
	        	for (int y = 0; y < height; y++) {
	          		valueLayer.set(valueLayer.get(x,y)*evaporationConst, x, y);
	        	}
	      	}
	    } else {
	    	int height = (int) valueLayer.getDimensions().getHeight();
	    	int depth = (int) valueLayer.getDimensions().getDepth();
	      	for (int x = 0; x < width; x++) {
	        	for (int y = 0; y < height; y++) {
	          		for (int z = 0; z < depth; z++) {
	            		valueLayer.set(valueLayer.get(x,y,z)*evaporationConst, x, y, z);
	          		}
        		}
	      	}
	    }
	}

	protected void propagate() {
	    // this is being based on
	    // http://www.mathcs.sjsu.edu/faculty/rucker/capow/santafe.html
	    int size = valueLayer.getDimensions().size();
	
	    if (size == 1) {
	      int width = (int) valueLayer.getDimensions().getWidth();
	
	      double sum;
	      double[] newVals = new double[width];
	      for (int x = 0; x < width; x++) {
	        // sum the cell to the left and the right of the given one
	        sum = getValue(x - 1);
	        sum += getValue(x + 1);
	
	        double weightedAvg = sum / 2.0;
	
	        // apply the diffusion and evaporation constants
	        double oldVal = getValue(x);
	        double delta = weightedAvg - oldVal;
	
	        double newVal = oldVal + delta * diffusionConst;
	        // bring the value into range [min, max]
	        newVals[x] = constrainByMinMax(newVal);
	      }
	      computedVals = newVals;
	    } else if (size == 2) {
	      int width = (int) valueLayer.getDimensions().getWidth();
	      int height = (int) valueLayer.getDimensions().getHeight();
	      double[][] newVals = new double[width][height];
	      for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	          // these are the neighbors that are directly north/south/east/west to
	          // the given cell 4 times those that are diagonal to the cell
	          double uE = getValue(x + 1, y);
	          double uN = getValue(x, y + 1);
	          double uW = getValue(x - 1, y);
	          double uS = getValue(x, y - 1);
	
	          // these are the neighbors that are diagonal to the given cell
	          double uNE = getValue(x + 1, y + 1);
	          double uNW = getValue(x - 1, y + 1);
	          double uSW = getValue(x - 1, y - 1);
	          double uSE = getValue(x + 1, y - 1);
	
	          // compute the weighted avg, those directly north/south/east/west
	          // are given 4 times the weight of those on a diagonal
	          double weightedAvg = (uE + uN + uW + uS + uNE + uNW + uSW + uSE) / 8.0;
	
	          // apply the diffusion and evaporation constants
	          double oldVal = getValue(x, y);
	          double delta = weightedAvg - oldVal;
	
	          double newVal = oldVal + delta * diffusionConst;
	
	          // bring the value into [min, max]
	          newVals[x][y] = constrainByMinMax(newVal);
	
	          // System.out.println("x: " + x + " y: " + y + "val: " + oldVal +
	          // " delta: "
	          // + delta + " d: " + newVals[x][y]);
	        }
	      }
	      computedVals = newVals;
	    }
	    diffuse();  
	}
}
