package ab.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.ABType;

import com.google.gson.annotations.Expose;

public class MyShot implements GraphNode {
	
	private int shotId;
	private int originStateId;
	
	private int birdIndex;
	private ABType birdType;
	
	private int times = 0;
	
	private int tapInterval;
	
	private Point target;
	private Point releasePoint;
	private Shot shot;
	private ABObject aim;
	
	private ABObject closestPig;
	private double distanceOfClosestPig;
	
	@Expose(serialize = true, deserialize = false)
	private boolean active = false;
	@Expose(serialize = true, deserialize = false)
	private int unvisitedChildren;
	@Expose(serialize = true, deserialize = false)
	private float miniMaxValue;
	
	
	private transient List<State> possibleStates;
	private transient int randomInt;
	
	public int getRandomInt() {
		return randomInt;
	}

	public void setRandomInt(int randomInt) {
		this.randomInt = randomInt;
	}

	public MyShot() {
		super();
		
		miniMaxValue = 0;

		possibleStates = new ArrayList<State>();
	}

	public int getShotId() {
		return shotId;
	}

	public void setShotId(int shotId) {
		this.shotId = shotId;
	}

	public int getOriginStateId() {
		return originStateId;
	}

	public void setOriginStateId(int originStateId) {
		this.originStateId = originStateId;
	}

	public int getBirdIndex() {
		return birdIndex;
	}

	public void setBirdIndex(int birdIndex) {
		this.birdIndex = birdIndex;
	}

	public ABType getBirdType() {
		return birdType;
	}

	public void setBirdType(ABType birdType) {
		this.birdType = birdType;
	}

	public Point getTarget() {
		return target;
	}

	public void setTarget(Point target) {
		this.target = target;
	}

	public Shot getShot() {
		return shot;
	}

	public void setShot(Shot shot) {
		this.shot = shot;
	}

	public ABObject getAim() {
		return aim;
	}

	public void setAim(ABObject aim) {
		this.aim = aim;
	}

	public ABObject getClosestPig() {
		return closestPig;
	}

	public void setClosestPig(ABObject closestPig) {
		this.closestPig = closestPig;
	}

	public double getDistanceOfClosestPig() {
		return distanceOfClosestPig;
	}

	public void setDistanceOfClosestPig(double distanceOfClosestPig) {
		this.distanceOfClosestPig = distanceOfClosestPig;
	}

	public List<State> getPossibleStates() {
		return possibleStates;
	}

	public void setPossibleStates(List<State> possibleStates) {
		this.possibleStates = possibleStates;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}
	
	public void setTimesPlusOne() {
		times++;
	}

	public Point getReleasePoint() {
		return releasePoint;
	}

	public void setReleasePoint(Point releasePoint) {
		this.releasePoint = releasePoint;
	}

	public int getTapInterval() {
		return tapInterval;
	}

	public void setTapInterval(int tapInterval) {
		this.tapInterval = tapInterval;
	}

	public boolean isFinalState() {
		return false;
	}

	public float getMiniMaxValue() {
		return miniMaxValue;
	}

	public void setMiniMaxValue(float miniMaxValue) {
		this.miniMaxValue = miniMaxValue;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getUnvisitedChildren() {
		return unvisitedChildren;
	}

	public void setUnvisitedChildren(int unvisitedChildren) {
		this.unvisitedChildren = unvisitedChildren;
	}	
	
}
