/**
 * 
 */
package repast.model.heatbugs;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.AbstractGridFunction;
import repast.simphony.valueLayer.BufferedGridValueLayer;
import repast.simphony.valueLayer.MaxGridFunction;
import repast.simphony.valueLayer.MinGridFunction;
import repast.simphony.valueLayer.ValueLayerDiffuser;
import repast.simphony.valueLayer.BufferedGridValueLayer.Buffer;

/**
 * Based on Nick Collier's heatbugs model
 * 
 * @author Drew Hoover
 */
public class Heatbug {

  private double unhappiness = 0;
  private int tolerance, outputHeat;
  private double stubbornness;
  private BufferedGridValueLayer heat;
  private Grid<Heatbug> grid;
  private GridPoint destination;

  @SuppressWarnings("unchecked")
  public Heatbug(int tolerance, int outputHeat, double stubbornness, GridPoint destination, Context<Heatbug> context) {
    this.tolerance = tolerance;
    this.outputHeat = outputHeat;
    this.destination = destination;
    this.stubbornness = stubbornness;
    
    heat = (BufferedGridValueLayer) context.getValueLayer("Heat Layer");
    grid = (Grid<Heatbug>) context.getProjection("Bug Grid");
  }

  @ScheduledMethod(start = 1, interval = 1, priority = 0)
  public void step() {
    GridPoint pt = grid.getLocation(this);
    //if at destination, do something
    if (pt.getCoord(0) == destination.getCoord(0) && pt.getCoord(1) == destination.getCoord(1)) {
    	Context<Object> context = ContextUtils.getContext(this);
    	context.remove(this);
    	return;
    }
    double heatHere = heat.get(pt.getX(), pt.getY());
    if (heatHere < tolerance) {
      unhappiness = 0;
    } else {
      unhappiness = (double) (heatHere - tolerance) / ValueLayerDiffuser.DEFAULT_MAX;
    }

    GridPoint newLocation = new GridPoint(pt.getX(), pt.getY());
    if (unhappiness != 0) {
    	
    	
      AbstractGridFunction func = heatHere < tolerance ? new MaxGridFunction() : new MinGridFunction();
      heat.forEach(func, pt, Buffer.READ, 1, 1);
      GridPoint coolestPt = func.getResults().get(0).getLocation();
      GridPoint thisSpot = grid.getLocation(this);
      double toCoolest = getAngleToDestination(coolestPt.getCoord(0), coolestPt.getCoord(1), 
    		  thisSpot.getCoord(0), thisSpot.getCoord(1));
      double toDestination = getAngleToDestination(destination.getCoord(0), destination.getCoord(1),
    		  thisSpot.getCoord(0), thisSpot.getCoord(1));
      double newDirection = (stubbornness*toDestination + toCoolest*(1 - stubbornness)) / 2;
      GridPoint desiredPt = pickSpotBasedOnAngle(newDirection);
      
      
      // not on the hottest or coldest place at the moment
      // then try to move to the desired point, if that's full then
      // choose one of the 8 neighboring cells at random.
      if (!desiredPt.equals(pt)) {
        int x = desiredPt.getX();
        int y = desiredPt.getY();
        int tries = 0;
        //trying to move to an unoccupied space
        while (grid.getObjectAt(x, y) != null && tries < 10) {
          x = newLocation.getX() + RandomHelper.nextIntFromTo(-1, 1);
          y = newLocation.getY() + RandomHelper.nextIntFromTo(-1, 1);
          tries++;
        }

        if (tries < 10) {
          newLocation = new GridPoint(x, y);
        }
      }
    } else { //move toward destination
    	
    }

    //System.out.printf("current heat val: %f%n", heat.get(pt.getX(), pt.getY()));
    //System.out.println("outputting heat: " + outputHeat);
    heat.set(outputHeat + heat.get(pt.getX(), pt.getY()), pt.getX(), pt.getY());
    grid.moveTo(this, newLocation.getX(), newLocation.getY());
  }

	public GridPoint getDestination() {
		return destination;
	}
	
	public void setDestination(GridPoint destination) {
		this.destination = destination;
	}
	/*
	 * x1/y1 is always destination; x2/y1 is always current location
	 */
	public double getAngleToDestination(int x1, int y1, int x2, int y2) {
		return Math.atan2(x1 - x2, y1 - y2);
	}
	/*
	 * should be in Radians
	 */
	public GridPoint pickSpotBasedOnAngle(double angle) {
		int spot = Math.round((float)(angle/Math.PI)*4);
		int thisX = grid.getLocation(this).getCoord(0);
		int thisY = grid.getLocation(this).getCoord(1);
		switch (spot) {
			case 0:
				return new GridPoint(thisX + 1, thisY);
			case 1:
				return new GridPoint(thisX + 1, thisY + 1);
			case 2:
				return new GridPoint(thisX, thisY + 1);
			case 3:
				return new GridPoint(thisX - 1, thisY + 1);
			case 4:
				return new GridPoint(thisX - 1, thisY);
			case 5:
				return new GridPoint(thisX - 1, thisY - 1);
			case 6:
				return new GridPoint(thisX, thisY - 1);
			default:
				return new GridPoint(thisX + 1, thisY - 1);
		}		
	}
}
