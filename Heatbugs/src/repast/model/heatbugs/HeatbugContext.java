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
  private ValueLayerDiffuser diffuser;
  private int numAgents, minICTolerance, maxICTolerance, emissionRate, boardXDim, boardYDim, nextInt, agentsPerTick;
  private double stubbornnessMax, stubbornnessMin, pathSpread;
  private float diffusionConstant, evaporationConstant;
  private repast.simphony.space.grid.Grid grid;
  
  /* (non-Javadoc)
   * @see repast.simphony.context.AbstractContext#addValueLayer(repast.simphony.valueLayer.ValueLayer)
   */
  @Override
  public void addValueLayer(ValueLayer valueLayer) {
    super.addValueLayer(valueLayer);
    diffuser = new ValueLayerDiffuser((IGridValueLayer) valueLayer, evaporationConstant, diffusionConstant, false);
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
  public void swap() {
    BufferedGridValueLayer gridvl = (BufferedGridValueLayer)getValueLayer("Heat Layer");
    gridvl.swap();
    diffuser.diffuse();
  }
  
  @ScheduledMethod(start = 100, interval = 100, priority = 0)
  public void record() {
	  logger.dumpInfo();
  }
  
  /**
   * 
   */
  @ScheduledMethod(start = 1, interval = 1, priority = 0)
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
	        		RandomHelper.nextIntFromTo(minICTolerance, maxICTolerance),
	        		emissionRate,
	        		RandomHelper.nextDoubleFromTo(stubbornnessMin, stubbornnessMax),
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
  
  public void setDiffuser(ValueLayerDiffuser diffuser) {
	  this.diffuser = diffuser;
  }
  
  public void addParameters() {
	  	Parameters params = RunEnvironment.getInstance().getParameters();
	  	minICTolerance = (Integer)params.getValue("minICTolerance");
	    maxICTolerance = (Integer)params.getValue("maxICTolerance");
	    emissionRate = (Integer)params.getValue("emissionRate");
	    stubbornnessMax = (Double)params.getValue("stubbornnessMax");
	    stubbornnessMin = (Double)params.getValue("stubbornnessMin");
	    diffusionConstant = (float)params.getValue("diffusionConstant");
	    evaporationConstant = (float)params.getValue("evaporationConstant");
	    boardXDim = (Integer)params.getValue("boardXDim");
	    boardYDim = (Integer)params.getValue("boardYDim");
	    agentsPerTick = (Integer)params.getValue("agentsPerTick");
	    nextInt = 0;
	    pathSpread = (Double)params.getValue("pathSpread");
	    grid = (Grid) this.getProjection("Bug Grid");
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
