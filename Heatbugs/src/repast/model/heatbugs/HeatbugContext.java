/**
 * 
 */
package repast.model.heatbugs;

import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
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
  
  /* (non-Javadoc)
   * @see repast.simphony.context.AbstractContext#addValueLayer(repast.simphony.valueLayer.ValueLayer)
   */
  @Override
  public void addValueLayer(ValueLayer valueLayer) {
    // TODO Auto-generated method stub
    super.addValueLayer(valueLayer);
    
    Parameters params = RunEnvironment.getInstance().getParameters();
    float diffusionConstant = (float) params.getValue("diffusionConstant");
    float evaporationConstant = (float) params.getValue("evaporationConstant");
    diffuser = new ValueLayerDiffuser((IGridValueLayer) valueLayer, evaporationConstant, diffusionConstant);
    //= new ValueLayerDiffuser((IGridValueLayer)valueLayer, .99, 1.0, true);
  }

  /**
   * Swaps the buffered heat layers.
   */
  // priority = -1 so that the heatbugs action occurs first
  @ScheduledMethod(start = 1, interval = 1, priority = -1)
  public void swap() {
    BufferedGridValueLayer grid = (BufferedGridValueLayer)getValueLayer("Heat Layer");
    grid.swap();
    diffuser.diffuse();
  }
  
  public void setDiffuser(ValueLayerDiffuser diffuser) {
	  this.diffuser = diffuser;
  }
  

}
