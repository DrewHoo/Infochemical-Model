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
import repast.simphony.space.SpatialException;
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
@SuppressWarnings("unused")
public class Heatbug {

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
    GridPoint pt = grid.getLocation(this), desiredPt, coolestPt;
    //if at destination, do something
    if (pt.getX() == destination.getX() && pt.getY() == destination.getY()) {
    	Context<Object> context = ContextUtils.getContext(this);
    	System.out.println("destination found");
    	context.remove(this);
    	return;
    }
    double angleToDestination = getAngle(pt.getX(), pt.getY(), destination.getX(), destination.getY());
    double heatHere = heat.get(pt.getX(), pt.getY());
    if (heatHere > tolerance) { //calculate desiredPt based on coolestPt & destination
    	desiredPt = findClosestAcceptablePoint(angleToDestination);
    } else {
    	desiredPt = pickSpotBasedOnAngle(angleToDestination);
    }
    heat.set(outputHeat + heat.get(pt.getX(), pt.getY()), pt.getX(), pt.getY());
    try {
    	grid.moveTo(this, desiredPt.getX(), desiredPt.getY());
    } catch (SpatialException e) {}
  }

	private GridPoint findClosestAcceptablePoint(double angle) {
		double compromiseAngle = angle, angInc = Math.PI/4, lowestHeat = Integer.MAX_VALUE, heatHere;
		GridPoint bestCompromise, pointOfLowestHeat = grid.getLocation(this);
		for(int i = 1; i <= 8; i++) {
			angInc *= -1;
			compromiseAngle += angInc;
			bestCompromise = pickSpotBasedOnAngle(compromiseAngle);
			try {heatHere = heat.get(bestCompromise.getX(), bestCompromise.getY());}
			catch (SpatialException e) {continue;}
			if (heatHere < lowestHeat) {
				pointOfLowestHeat = bestCompromise;
				lowestHeat = heatHere;
			}
			if (heatHere <= tolerance) {return bestCompromise;}
			angInc = (i % 2 == 0) ? angInc + Math.PI/4 : angInc;
		}
		return pointOfLowestHeat;
}

	public GridPoint getDestination() {
		return destination;
	}
	
	public void setDestination(GridPoint destination) {
		this.destination = destination;
	}
	/*
	 * x1/y1 is always current location ; x2/y2 is always destination
	 * compensates for atan2() by returning only positive values between 0, 2pi
	 */
	public double getAngle(int x1, int y1, int x2, int y2) {
		if (x1 == x2) {
			return (y1 > y2) ? 3*Math.PI/2 : Math.PI/2;
		}
		if (y1 == y2) {
			return (x1 > x2) ? Math.PI : 0;
		}
		double angle = Math.atan2(y1 - y2, x1 - x2);
		return (angle < 0) ? angle + Math.PI*2 : angle;
	}
	/*
	 * angle should be in Radians
	 */
	public GridPoint pickSpotBasedOnAngle(double angle) {
//		angle = (angle < 0) ? angle += Math.PI*2 : angle;
		int spot = Math.round((float)(angle/Math.PI)*4);
		int x = grid.getLocation(this).getX();
		int y = grid.getLocation(this).getY();
		switch (spot) {
			case 0:
			case 8:
				return new GridPoint(x + 1, y);
			case 1:
				return new GridPoint(x + 1, y + 1);
			case 2:
				return new GridPoint(x, y + 1);
			case 3:
				return new GridPoint(x - 1, y + 1);
			case 4:
				return new GridPoint(x - 1, y);
			case 5:
				return new GridPoint(x - 1, y - 1);
			case 6:
				return new GridPoint(x, y - 1);
			case 7:
				return new GridPoint(x + 1, y - 1);
			default:
				return new GridPoint(x, y);
		}		
	}
}
