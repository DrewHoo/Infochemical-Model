/**
 * 
 */
package repast.model.heatbugs;
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
import repast.simphony.valueLayer.ValueLayerDiffuser;

import repast.simphony.context.DefaultContext;

/**
 * Based on Nick Collier's heatbugs model
 * 
 * @author Drew Hoover
 */
public class HeatbugContext extends DefaultContext<Heatbug> {
  private Logger logger;
  private ValueLayerDiffuser evaporator, propagator;
  private int 		cleanupRate, boardXDim, boardYDim, nextInt,
  					agentsPerTick, tick = 0, evaporationRate, propagationRate;
  private double 	pathSpread, emissionStrength, propagationFactor, 
  					evaporationFactor, minTolerance, maxTolerance, minimumStrength;
  private repast.simphony.space.grid.Grid grid;
  private IGridValueLayer heat;
  
  /* (non-Javadoc)
   * @see repast.simphony.context.AbstractContext#addValueLayer(repast.simphony.valueLayer.ValueLayer)
   */
  @Override
  public void addValueLayer(ValueLayer valueLayer) {
    super.addValueLayer(valueLayer);
    heat = (IGridValueLayer) valueLayer;
    propagator = new ValueLayerDiffuser((IGridValueLayer) valueLayer, 1, propagationFactor, false);
    evaporator = new ValueLayerDiffuser((IGridValueLayer) valueLayer, 1 - evaporationFactor, 0, false);
  }

  
  @ScheduledMethod(start = 0, interval = 0, priority = 0)
  public void recordRunInfo() {
	  //add params here
  }
  /**
   * Swaps the buffered heat layers.
   */
  @SuppressWarnings("unchecked")
// priority = -1 so that the heatbugs action occurs first
  @ScheduledMethod(start = 1, interval = 1, priority = -1)
  public void aggregate() {
    BufferedGridValueLayer gridvl = (BufferedGridValueLayer)getValueLayer("Heat Layer");
    gridvl.swap();
    if (tick++ % evaporationRate == 0)
    	evaporator.diffuse();
    if (tick % propagationRate == 0)
    	propagator.diffuse();
    if (tick % cleanupRate == 0)
    	for (int i = 0; i < boardXDim; i++) {
    		for (int j = 0; j < boardYDim; j++) {
    			if (Double.compare(heat.get(i,j), minimumStrength) < 0)
    				heat.set(0,i,j);
    		}
    	}
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
	    		int y = (int) (boardYDim*pathSpread);
	    		int offset = (int) ((1-pathSpread)/2.0*boardYDim);
	    		destination = new GridPoint(boardXDim - 1, (int)(Math.random()*y + offset)); //horizontal stream
    			startPoint = new GridPoint(1, (int)(Math.random()*y + offset));
	        } else {
	    		int x = (int) (boardXDim*pathSpread);
	    		int offset = (int) ((1-pathSpread)/2.0*boardXDim);
				destination = new GridPoint((int)(Math.random()*x + offset), boardYDim - 1); //vertical stream
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
	   for (int j = 0; !moved && j < boardXDim*pathSpread; j++) {
	    	try {
	    		moved = grid.moveTo(bug, startPoint.getX(), startPoint.getY());
	    		if (!moved)
		    		if (startPoint.getX() == 1) {
		    			startPoint = new GridPoint(1, (int)(Math.random()*boardYDim));
		    		} else {
		    			startPoint = new GridPoint((int)(Math.random()*boardXDim), 1);
		    		}
	    	} catch (SpatialException e) {}
	    }
	  }
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
	    boardXDim = (Integer)params.getValue("boardXDim");
	    boardYDim = (Integer)params.getValue("boardYDim");
	    agentsPerTick = (Integer)params.getValue("agentsPerTick");
	    nextInt = 0;
	    pathSpread = (Double)params.getValue("pathSpread");
	    grid = (Grid) this.getProjection("Bug Grid");
	    cleanupRate = (Integer)params.getValue("cleanupRate");
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
