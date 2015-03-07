/**
 * 
 */
package repast.model.heatbugs;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.BufferedGridValueLayer;
import repast.simphony.valueLayer.IGridValueLayer;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

/**
 * Based on Nick Collier's heatbugs model
 * 
 * @author Drew Hoover
 */
public class HeatbugContext extends DefaultContext<Heatbug> {
  
  private ValueLayerDiffuser diffuser;
  private int numAgents, minICTolerance, maxICTolerance, emissionRate, boardXDim, boardYDim, nextInt;
  private double stubbornnessMax, stubbornnessMin;
  private float diffusionConstant, evaporationConstant;
  private repast.simphony.space.grid.Grid grid;
  
  /* (non-Javadoc)
   * @see repast.simphony.context.AbstractContext#addValueLayer(repast.simphony.valueLayer.ValueLayer)
   */
  @Override
  public void addValueLayer(ValueLayer valueLayer) {
    // TODO Auto-generated method stub
    super.addValueLayer(valueLayer);
    diffuser = new ValueLayerDiffuser((IGridValueLayer) valueLayer, evaporationConstant, diffusionConstant, false);
    //= new ValueLayerDiffuser((IGridValueLayer)valueLayer, .99, 1.0, true);
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
  
  @ScheduledMethod(start = 1, interval = 1, priority = 0)
  public void addBugs() {
	  if (this.size() <= numAgents) {
	    	GridPoint destination, startPoint;
	    	if (++nextInt % 2 == 0) {
	      	  destination = new GridPoint(boardXDim - 1, 100); //horizontal stream
	      	  startPoint = new GridPoint(1, 100);
	        } else {
	      	  destination = new GridPoint(100, boardYDim - 1); //vertical stream
	      	  startPoint = new GridPoint(100, 1);
	        }
	        Heatbug bug = new Heatbug(
	        		RandomHelper.nextIntFromTo(minICTolerance, maxICTolerance),
	        		emissionRate,
	        		RandomHelper.nextDoubleFromTo(stubbornnessMin, stubbornnessMax),
	        		destination, this);
	    	this.add(bug);
	    	while(!grid.moveTo(bug, startPoint.getX(), startPoint.getY())){
	    		if (startPoint.getX() > startPoint.getY()) {
	    			startPoint = new GridPoint(startPoint.getX() + 1, startPoint.getY());
	    		} else {startPoint = new GridPoint(startPoint.getX(), startPoint.getY() + 1);}
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
	    numAgents = (Integer)params.getValue("initialNumAgents");
	    diffusionConstant = (float)params.getValue("diffusionConstant");
	    evaporationConstant = (float)params.getValue("evaporationConstant");
	    boardXDim = (Integer)params.getValue("boardXDim");
	    boardYDim = (Integer)params.getValue("boardYDim");
	    nextInt = 0;
	    grid = (Grid) this.getProjection("Bug Grid");
  }
  

}
