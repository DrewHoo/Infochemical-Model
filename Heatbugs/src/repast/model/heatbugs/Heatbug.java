/**
 * 
 */
package repast.model.heatbugs;

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
 * 
 * 
 * @author Drew Hoover
 */
public class Heatbug {
  private int tolerance, emissionStrength, lifetime, initialDistance, travelDistance;
  private long cumulativeTemperature;
  private BufferedGridValueLayer heat;
  private Grid<Heatbug> grid;
  private GridPoint destination;
  private int[][] offsets = new int[8][2];
  private int collisions;

  @SuppressWarnings("unchecked")
  public Heatbug(int tolerance, int emissionStrength, GridPoint destination, Context<Heatbug> context) {
    this.tolerance = tolerance;
    this.emissionStrength = emissionStrength;
    this.destination = destination;
    lifetime = 0;
    travelDistance = 0;
    cumulativeTemperature = 0;
    offsets = new int[][]{{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
    collisions = 0;
    heat = (BufferedGridValueLayer) context.getValueLayer("Heat Layer");
    grid = (Grid<Heatbug>) context.getProjection("Bug Grid");
  }

  @ScheduledMethod(start = 1, interval = 0, priority = 0)
  public void recordStartInfo() {
	  initialDistance = getDistance(grid.getLocation(this), destination);
  }
  
  
  /**
   * In order to make testing easier, I'm trying to move logic that involves
   * getting information directly from the Context, Grid, or GridValueLayer in here.
   * That way we can run unit tests on individual Heatbug methods without relying on a particular
   * instance of a context.
   * 
   */
  @ScheduledMethod(start = 1, interval = 1, priority = 0)
  public void step() {
	lifetime++;
	HeatbugContext context = (HeatbugContext) ContextUtils.getContext(this);
    GridPoint pt = grid.getLocation(this);
    if (arrivedAtDestination(pt)) {
    	report();
    	context.remove(this);
    	return;
    }
    cumulativeTemperature += heat.get(pt.getX(), pt.getY());
    //find congestion of current block
    //move based on odds adjusted by congestion amt
    double congestion = context.getCongestion(pt.getX(), pt.getY());
    //the order of setting the heat and moveToClosestAcceptablePoint is probably important.
    if (Double.compare(congestion, Math.random()) < 0) {
		if (isItTooHot()) getOutOfTheHeat();
		else moveToBestMoorePoint();
		GridPoint newPt = grid.getLocation(this);
	    heat.set(emissionStrength + heat.get(newPt.getX(), newPt.getY()), newPt.getX(), newPt.getY());
	    context.updateBlock(this, pt);
    }
  }

  
	/**
	 * @param GridPoint pt is the current point to check against
	 * the destination point, which is the attribute of heatbug
	 */
	private boolean arrivedAtDestination(GridPoint pt) {
		return (pt.getX() == destination.getX() && pt.getY() == destination.getY()) ? true : false;
	}
	
	/**
	 * Sends information about this Heatbug to Logger
	 * @param int collisions
	 * @param double average heat
	 * @param int lifespan
	 * @param 
	 */
	private void report() {
		DefaultContext<Heatbug> context = (HeatbugContext) ContextUtils.getContext(this);
		//format is lifetime, initial distance to goal, distance traveled, collisions, avg temp, tolerance
		String s = "" + lifetime + "," + initialDistance + "," + travelDistance + "," + collisions
				+ "," + (cumulativeTemperature/lifetime) + "," + tolerance + "\n"; 
		((HeatbugContext) context).feedLogger(s);
		return;
	}
	/*
	 * mooreRadianInc should be negative if angleToDestination is less than angleToMooreNeighbor
	 * to ensure that the forloop checks closest moore neighbors first
	 * however, if agent needs to change direction and angleToMoorePt & angle are equal
	 * mooreRadianIncrement should be negative
	 * @param
	 * @returns moore radian increment
	 */
	private double getMooreRadianIncrement(double angleToMoorePt) {
		double angleToDest = getAngle(grid.getLocation(this), destination);
		if (Math.abs(angleToDest - angleToMoorePt) < 0.01) {
			return (Math.random() > 0.5) ? Math.PI/4 : -Math.PI/4;
		} else {
			return (angleToDest > angleToMoorePt) ? Math.PI/4 : -Math.PI/4;
		}
	}
	
	private void getOutOfTheHeat() {
		GridPoint 	current = grid.getLocation(this), 
					lowestHeatPt = new GridPoint(current.getX(), current.getY());
		double heatHere, lowestHeat = Integer.MAX_VALUE;
		int x, y;
		for (int i = 0; i < 8; i++) {
			try {
				x = current.getX() + offsets[i][0];
				y = current.getY() + offsets[i][1];
				heatHere = heat.get(x,y);
				if (heatHere < lowestHeat) {
					lowestHeat = heatHere;
					lowestHeatPt = new GridPoint(x, y);
				}
			} catch (SpatialException e) {continue;}
		}
		if(!grid.moveTo(this, lowestHeatPt.getX(), lowestHeatPt.getY())) {
			collisions++;
		} else {travelDistance++;}
	}
	/**
	 * 
	 * @return true if every neighbor is hotter than tolerance.
	 * 
	 */
	private boolean isItTooHot() {
		AbstractGridFunction func = new MinGridFunction();
	    heat.forEach(func, grid.getLocation(this), Buffer.READ, 1, 1);
	    GridPoint lowestHeatPoint = func.getResults().get(0).getLocation();
	    if (heat.get(lowestHeatPoint.getX(), lowestHeatPoint.getY()) > tolerance) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
/**
 * 			
 * @param 	angle is the ideal angle at which agent would like to travel.
 * 			angle should be in radians. 
 * @return	the GridPoint in the Moore neighborhood that is closest to being on
 * 			the path that the angle represents.
 */
	private void moveToBestMoorePoint() {
		double		angleToDest = getAngle(grid.getLocation(this), destination);
		GridPoint	nextPt = pickSpotBasedOnAngle(angleToDest);
		double 		angleToNextPt = getAngle(grid.getLocation(this), nextPt),
					mooreRadianIncrement = getMooreRadianIncrement(angleToNextPt),
					heatHere;
		boolean 	moved = false;
		for (int i = 0; i < 8; i++) {
			mooreRadianIncrement = -mooreRadianIncrement;
			angleToDest += (mooreRadianIncrement * i);
			nextPt = pickSpotBasedOnAngle(angleToDest);
			try {
				heatHere = heat.get(nextPt.getX(), nextPt.getY());
				if (heatHere <= tolerance) {
					if (!grid.moveTo(this, nextPt.getX(), nextPt.getY()))
						collisions++;
					else {travelDistance++;}
					moved = true;
				}
			} catch (SpatialException e) {}
			if (moved) break;
		}
	}
	public void printPathInfo(GridPoint current, GridPoint nextPt, double angle) {
		System.out.print("Current: " + current.getX() + " " + current.getY() + "\tDest: "
				+ nextPt.getX() + " " + nextPt.getY());
		System.out.printf("\tangle: %2.2f", angle);
		System.out.print("\tfDest: " + destination.getX() + " " + destination.getY());
		double dif = getAngle(current, nextPt)
				- getAngle(current, destination);
		System.out.printf("\tDifference from first choice: %2.2f", dif);
		System.out.print("\tSuccessful: ");
	}
	public GridPoint getDestination() {
		return destination;
	}
	
	public void setDestination(GridPoint destination) {
		this.destination = destination;
	}
	
	public int getDistance(GridPoint current, GridPoint dest) {
		int x = current.getX() - dest.getX();
		int y = current.getY() - dest.getY();
		return (int) Math.round(Math.sqrt(x*x + y*y));
	}
	/*
	 * x1/y1 is always current location ; x2/y2 is always destination
	 * compensates for atan2() by returning only positive values between 0, 2pi
	 */
	public double getAngle(GridPoint current, GridPoint dest) {
		int x1 = current.getX(), y1 = current.getY(), x2 = dest.getX(), y2 = dest.getY();
		if (x1 == x2) {
			return (y2 > y1) ? Math.PI/2.0 : 3.0*Math.PI/2.0 ;
		}
		if (y1 == y2) {
			return (x2 > x1) ? 0 : Math.PI;
		}
		double angle = Math.atan2(y2 - y1, x2 - x1);
		while (angle < 0) {angle += Math.PI*2;}
		while (angle > Math.PI*2) {angle -= Math.PI*2;}
		if (angle < 0 || angle > 2*Math.PI)
			System.out.printf("angle: %2.2f%n", angle);
		return angle;
	}
	/**
	 * @param 	angle should be in radians
	 * @return 	GridPoint that a line extending from current point at the
	 * 			indicated angle would intersect (said differently: 
	 * 			translates angle into a point in the moore neighborhood)			
	 */
	public GridPoint pickSpotBasedOnAngle(double angle) {
		double fourOverPi = 4.0 / Math.PI;
		while (angle < 0) {angle += Math.PI*2;}
		while (angle > Math.PI*2) {angle -= Math.PI*2;}
		int spot = (int) Math.round(angle*fourOverPi);
		int x = grid.getLocation(this).getX();
		int y = grid.getLocation(this).getY();
		switch (spot) {
			case 0:
			case 8:
				return new GridPoint(x + 1	, y);
			case 1:
				return new GridPoint(x + 1	, y + 1);
			case 2:
				return new GridPoint(x		, y + 1);
			case 3:
				return new GridPoint(x - 1	, y + 1);
			case 4:
				return new GridPoint(x - 1	, y);
			case 5:
				return new GridPoint(x - 1	, y - 1);
			case 6:
				return new GridPoint(x		, y - 1);
			case 7:
				return new GridPoint(x + 1	, y - 1);
			default:
				return new GridPoint(x, y);
		}		
	}
}