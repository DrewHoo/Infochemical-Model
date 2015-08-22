/**
 * 
 */
package repast.model.heatbugs;
import java.util.ArrayList;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.BufferedGridValueLayer;
import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayer;

/**
 * Based on Nick Collier's heatbugs model
 * 
 * @author Drew Hoover
 */
public class HeatbugContext extends DefaultContext<Heatbug> {
  private Logger logger;
  private InfoChemDiffuser diffuser;
  private int 		boardDim, nextInt, congestionLimit, blockSize,
  					agentsPerTick, tick = 0, evaporationRate, propagationRate;
  private double 	pathSpread, emissionStrength, propagationFactor, 
  					evaporationFactor, minTolerance, maxTolerance, minimumStrength;
  private repast.simphony.space.grid.Grid grid;
  private IGridValueLayer heat;
  private ArrayList<ArrayList<ArrayList<Heatbug>>> blocks;
  
  /* 
   * @see repast.simphony.context.AbstractContext#addValueLayer(repast.simphony.valueLayer.ValueLayer)
   */
  @Override
  public void addValueLayer(ValueLayer valueLayer) {
    super.addValueLayer(valueLayer);
    heat = (IGridValueLayer) valueLayer;
    diffuser = new InfoChemDiffuser((IGridValueLayer) valueLayer, 1 - evaporationFactor, propagationFactor, false);
    diffuser.setMinValue(minimumStrength);
  }

  
  @ScheduledMethod(start = 0, interval = 0, priority = 0)
  public void recordRunInfo() {
	  //add params here
  }
  /**
   * Swaps the buffered heat layers.
   */
// priority = -1 so that the heatbugs action occurs first
  @ScheduledMethod(start = 1, interval = 1, priority = -1)
  public void aggregate() {
    BufferedGridValueLayer gridvl = (BufferedGridValueLayer)getValueLayer("Heat Layer");
    gridvl.swap();
    if (tick++ % evaporationRate == 0)
    	diffuser.evaporate();
    if (tick % propagationRate == 0)
    	diffuser.propagate();
  }
  
  @ScheduledMethod(start = 100, interval = 100, priority = 0)
  public void record() {
	  logger.dumpInfo();
  }
  
  /**
   * 
   */
  public void addBugs() {
	  for (int i = 0; i < agentsPerTick; i++) {
	    	GridPoint destination, startPoint;
	    	if (++nextInt % 2 == 0) {
	    		int y = (int) (boardDim*pathSpread);
	    		int offset = (int) ((1-pathSpread)/2.0*boardDim);
	    		destination = new GridPoint(boardDim - 1, (int)(Math.random()*y + offset)); //horizontal stream
    			startPoint = new GridPoint(1, (int)(Math.random()*y + offset));
	        } else {
	    		int x = (int) (boardDim*pathSpread);
	    		int offset = (int) ((1-pathSpread)/2.0*boardDim);
				destination = new GridPoint((int)(Math.random()*x + offset), boardDim - 1); //vertical stream
	      	  	startPoint = new GridPoint((int)(Math.random()*x + offset), 1);
	        }
	        Heatbug bug = new Heatbug(
	        		RandomHelper.nextIntFromTo((int)minTolerance, (int)maxTolerance),
	        		(int)emissionStrength,
	        		destination, this);
	    	this.add(bug);
	    	boolean moved = false;
	    	//Eventually need to update this so that only spots within path 
	    	//(as determined by pathSpread) are tried
		   for (int j = 0; !moved && j < boardDim*pathSpread; j++) {
		    	try {
		    		moved = grid.moveTo(bug, startPoint.getX(), startPoint.getY());
		    		if (!moved)
			    		if (startPoint.getX() == 1) {
			    			startPoint = new GridPoint(1, (int)(Math.random()*boardDim));
			    		} else {
			    			startPoint = new GridPoint((int)(Math.random()*boardDim), 1);
			    		}
		    	} catch (SpatialException e) {}
		   }
		   addToBlock(bug, startPoint);
	  }	
  }

  public void updateBlock(Heatbug h, GridPoint oldPt) {
	  GridPoint loc = grid.getLocation(h);
	  int x = loc.getX() * blockSize / boardDim;
	  int y = loc.getY() * blockSize / boardDim;
	  int x2 = oldPt.getX() * blockSize / boardDim;
	  int y2 = oldPt.getY() * blockSize / boardDim;
	  blocks.get(x2).get(y2).remove(h);
	  blocks.get(x).get(y).add(h);
  }
  
  public double getCongestion(int x, int y) {
	  x = x* blockSize / boardDim;
	  y = y* blockSize / boardDim;
	  return (double) blocks.get(x).get(y).size() / (blockSize*blockSize);
  }
  
  
  public void addToBlock(Heatbug bug, GridPoint startPoint) {
	  int x = startPoint.getX() * blockSize / boardDim;
	  int y = startPoint.getY() * blockSize / boardDim;
	  blocks.get(x).get(y).add(bug);
  }
  
//  public void setDiffuser(ValueLayerDiffuser diffuser) {
//	  this.diffuser = diffuser;
//  }
  
  public void addParameters() {
	  	Parameters params = RunEnvironment.getInstance().getParameters();
	  	minTolerance = (Double)params.getValue("minTolerance")*Short.MAX_VALUE;
	    maxTolerance = (Double)params.getValue("maxTolerance")*Short.MAX_VALUE;
	    emissionStrength = (Double)params.getValue("emissionStrength")*Short.MAX_VALUE;
	    minimumStrength = (Double)params.getValue("minimumStrength")*Short.MAX_VALUE;
	    propagationFactor = (Double)params.getValue("propagationFactor");
	    propagationRate = (Integer)params.getValue("propagationRate");
	    evaporationFactor = (Double)params.getValue("evaporationFactor");
	    evaporationRate = (Integer)params.getValue("evaporationRate");
	    boardDim = (Integer)params.getValue("boardDim");
	    agentsPerTick = (Integer)params.getValue("agentsPerTick");
	    nextInt = 0;
	    pathSpread = (Double)params.getValue("pathSpread");
	    grid = (Grid) this.getProjection("Bug Grid");
	    blockSize = (Integer)params.getValue("blockSize");
	    congestionLimit = (Integer)params.getValue("congestionLimit");
	    blocks = new ArrayList<ArrayList<ArrayList<Heatbug>>>();
	    for (int i = 0; i < blockSize; i++) {
		    blocks.add(new ArrayList<ArrayList<Heatbug>>());
	    	for (int j = 0; j < blockSize; j++) {
	    		blocks.get(i).add(new ArrayList<Heatbug>());
	    	}
	    }
	    
  }
  
  public void setLogger(Logger logger) {
	  this.logger = logger;
  }
  public void feedLogger(String s) {
	  logger.eat(s);
  }
  public void setDirectory(String s) {
	  logger.setDirectory(s);
  }

}
