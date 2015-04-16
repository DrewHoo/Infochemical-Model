/**
 * 
 */
package repast.model.heatbugs;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.BufferedGridValueLayer;

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
  private int[][] offsets = new int[8][2];

  @SuppressWarnings("unchecked")
  public Heatbug(int tolerance, int outputHeat, double stubbornness, GridPoint destination, Context<Heatbug> context) {
    this.tolerance = tolerance;
    this.outputHeat = outputHeat;
    this.destination = destination;
    this.stubbornness = stubbornness;
    offsets[0] = new int[]{1,0};
    offsets[1] = new int[]{1,1};
    offsets[2] = new int[]{0,1};
    offsets[3] = new int[]{-1,1};
    offsets[4] = new int[]{-1,0};
    offsets[5] = new int[]{-1,-1};
    offsets[6] = new int[]{0,-1};
    offsets[7] = new int[]{1,-1};
    
    heat = (BufferedGridValueLayer) context.getValueLayer("Heat Layer");
    grid = (Grid<Heatbug>) context.getProjection("Bug Grid");
  }

  @ScheduledMethod(start = 1, interval = 1, priority = 0)
  /**
   * 
   * @throws SpatialException if an out-of-bounds point on grid or gridValueLayer is accessed
   */
  public void step() {
    GridPoint pt = grid.getLocation(this);
    if (arrivedAtDestination(pt)) return;
    //the order of these two is probably important.
    heat.set(outputHeat + heat.get(pt.getX(), pt.getY()), pt.getX(), pt.getY());
    double angle = getAngle(pt.getX(), pt.getY(), destination.getX(), destination.getY());
    moveToClosestAcceptablePoint(angle);
  }

	/**
	 * @param pt
	 */
	private boolean arrivedAtDestination(GridPoint pt) {
		if (pt.getX() == destination.getX() && pt.getY() == destination.getY()) {
	    	Context<Object> context = ContextUtils.getContext(this);
	    	context.remove(this);
	    	return true;
	    }
		return false;
	}
/**
 * 			
 * @param 	angle is the ideal angle at which agent would like to travel.
 * 			angle should be in radians. 
 * @return	the GridPoint in the Moore neighborhood that is closest to being on
 * 			the path that the angle represents.
 */
	private void moveToClosestAcceptablePoint(double angle) {
		double mooreRadianIncrement = Math.PI/4, lowestHeatAmount = Integer.MAX_VALUE, heatHere, heatTest;
		GridPoint nextPt, lowestHeatPoint = grid.getLocation(this), current = grid.getLocation(this);
		boolean moved = false, neighborsTooHot = true;
		// mooreRadianInc should be negative if angleToDestination is less than angleToMooreNeighbor
		// to ensure that the forloop checks closest moore neighbor first
		double angleToMoore = getAngle(current.getX(), current.getY(), 
				pickSpotBasedOnAngle(angle).getX(), pickSpotBasedOnAngle(angle).getY());
		if (Math.abs(angle - angleToMoore) < 0.01) {
			mooreRadianIncrement *= (Math.random() > 0.5) ? 1 : -1;
		} else {
			mooreRadianIncrement *= ( angle > angleToMoore) ? 1 : -1;
		}
		//check to see that agent is not surrounded by neighbors with heat > threshold
		//if this is true, I'm still not sure of appropriate action for agent;
		//this current algorithm, of allowing agent to ignore heat causes strange behavior
		for (int i = 0; i < 8; i++) {
			try { heatTest = heat.get(current.getX() + offsets[i][0], current.getY() + offsets[i][1]); }
			catch (SpatialException e) {continue;}
			if (heatTest <= tolerance) {
				neighborsTooHot = false;
				break;
			}
		}
		for (int i = 0; i < 8; i++) {
			mooreRadianIncrement = -mooreRadianIncrement;
			angle += (mooreRadianIncrement * i);
			nextPt = pickSpotBasedOnAngle(angle);
			try {heatHere = heat.get(nextPt.getX(), nextPt.getY());}
			catch (SpatialException e) {
	//			System.out.println("Spatial Exception " + i + " " + bestCompromise.getX() + " " + bestCompromise.getY());
				continue;
			}
			
	//		if (grid.getRandomObjectAt(nextPt.getX(), nextPt.getY()) != null) {
	//			System.out.println("Current: " + current.getX() + " " + current.getY() + "\tDest: " + nextPt.getX() + " " + nextPt.getY() + " is occupied.");
	//			continue;
	//		}
			
			//if you find a spot with less heat than tolerance threshold, go there
			try {
				if (heatHere <= tolerance || neighborsTooHot) {
	//				printPathInfo(current, nextPt, angle);
					if (grid.moveTo(this, nextPt.getX(), nextPt.getY())) {
						moved = true;
					}
	//					System.out.println(moved);
				} else {
					System.out.println("heatHere > tolerance");
				}
			} catch (SpatialException e) {} //{System.out.println(e.toString());}
			if (moved) break;
			//if every spot is above tolerance, go to spot with least heat
			if (heatHere < lowestHeatAmount) {
				lowestHeatPoint = nextPt;
				lowestHeatAmount = heatHere;
			}
		}
		return;
	}
	public void printPathInfo(GridPoint current, GridPoint nextPt, double angle) {
		System.out.print("Current: " + current.getX() + " " + current.getY() + "\tDest: "
				+ nextPt.getX() + " " + nextPt.getY());
		System.out.printf("\tangle: %2.2f", angle);
		System.out.print("\tfDest: " + destination.getX() + " " + destination.getY());
		double dif = getAngle(current.getX(), current.getY(), nextPt.getX(), nextPt.getY())
				- getAngle(current.getX(), current.getY(), destination.getX(), destination.getY());
		System.out.printf("\tDifference from first choice: %2.2f", dif);
		System.out.print("\tSuccessful: ");
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
				System.out.println("default: " + spot);
				return new GridPoint(x, y);
		}		
	}
}