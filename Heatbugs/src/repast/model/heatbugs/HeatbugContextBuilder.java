/**
 * 
 */
package repast.model.heatbugs;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.BufferedGridValueLayer;
import repast.simphony.valueLayer.ValueLayerDiffuser;

/**
 * @author Drew Hoover
 * based on Nick Collier's heatbugs model
 */
public class HeatbugContextBuilder implements ContextBuilder<Heatbug> {
  
  /* (non-Javadoc)
   * @see repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context.Context)
   */
  public Context<Heatbug> build(Context<Heatbug> context) {
	Parameters params = RunEnvironment.getInstance().getParameters();
	int boardXDim = (Integer)params.getValue("boardXDim");
	int boardYDim = (Integer)params.getValue("boardYDim");
	GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
	Grid<Heatbug> grid = gridFactory.createGrid("Bug Grid", context, 
			new GridBuilderParameters<Heatbug>(new WrapAroundBorders(), 
			new RandomGridAdder<Heatbug>(), false, new int[]{boardXDim, boardYDim}, new int[]{0, 0}));
    BufferedGridValueLayer heat = new BufferedGridValueLayer("Heat Layer", 0, true, new WrapAroundBorders(), 
    		new int[]{boardXDim, boardYDim}, new int[]{0,0});
    context.addValueLayer(heat);
    
    int minICTolerance = (Integer)params.getValue("minICTolerance");
    int maxICTolerance = (Integer)params.getValue("maxICTolerance");
    int emissionRate = (Integer)params.getValue("emissionRate");
    double stubbornnessMax = (Double)params.getValue("stubbornnessMax");
    double stubbornnessMin = (Double)params.getValue("stubbornnessMin");
    int numAgents = (Integer)params.getValue("initialNumAgents");
    double stubbornness = RandomHelper.nextDoubleFromTo(stubbornnessMin, stubbornnessMax);
    
    for (int i = 0; i < numAgents; i++) {
      int idealTemp = RandomHelper.nextIntFromTo(maxICTolerance, minICTolerance);
      GridPoint destination;
      if (i % 2 == 0) { //puts 1/2 on one side of the board, 1/2 on the other
    	  destination = new GridPoint(new int[]{0, RandomHelper.nextIntFromTo(0, boardYDim)});
      } else {
    	  destination = new GridPoint(new int[]{boardXDim - 1, RandomHelper.nextIntFromTo(0, boardYDim)});
      }
      
      Heatbug bug = new Heatbug(idealTemp, emissionRate, stubbornness, destination, context);
      context.add(bug);
    }
    
    //for each heatbug, find their destination coordinates, put them on opposite side of map
    for (Heatbug obj : context) {
    	GridPoint pt = obj.getDestination();
    	int xmove, ymove;
    	xmove = (pt.getCoord(0) == 0) ? boardXDim - 1: 0;
    	ymove = RandomHelper.nextIntFromTo(0, boardYDim);
		grid.moveTo(obj, xmove, ymove);
	}
 
    return context;
  }
  
  @ScheduledMethod(start = 1, interval = 1, priority = -1)
  public void swapHeatBuffers() {
    System.out.println("swap");
    //System.out.println("swapping");
    //heat.swap();
  }
}
