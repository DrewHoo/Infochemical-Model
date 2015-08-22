/**
 * 
 */
package digital_Infochemical;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.AbstractGridFunction;
import repast.simphony.valueLayer.BufferedGridValueLayer;
import repast.simphony.valueLayer.BufferedGridValueLayer.Buffer;
import repast.simphony.valueLayer.MinGridFunction;

/**
 * @author DrewHoo
 *
 */
public class Mover {
	private GridPoint finalDestination;
	public Mover() {}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 0)
	public void step() {
		
	}
	
	private boolean isAtDestination() {
		return false;
	}
	private double getMooreRadianIncrement() {
		return -1;
	}
	private void seekGreaterConcentration() {
		
	}
	private void seekLesserConcentraction() {
		
	}
	private boolean isItTooConcentrated() {
		return false;
	}
	private boolean isItConcentratedEnough() {
		return false;
	}
	private GridPoint getFinalDestination() {
		return finalDestination;
	}
	private void setDestination() {
		
	}
	
	
}
