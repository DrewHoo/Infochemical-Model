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
import repast.simphony.space.grid.StickyBorders;
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
			new GridBuilderParameters<Heatbug>(new StrictBorders(), 
			new RandomGridAdder<Heatbug>(), false, new int[]{boardXDim, boardYDim}, new int[]{0, 0}));
    BufferedGridValueLayer heat = new BufferedGridValueLayer("Heat Layer", 0, true, new StrictBorders(), 
    		new int[]{boardXDim, boardYDim}, new int[]{0,0});
    ((HeatbugContext) context).addParameters();
    context.addValueLayer(heat);
    return context;
  }
  
  @ScheduledMethod(start = 1, interval = 1, priority = -1)
  public void swapHeatBuffers() {
    System.out.println("swap");
    //System.out.println("swapping");
    //heat.swap();
  }
}
