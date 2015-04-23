package ab.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.ABType;

public class MyShot {
	
	private int shotId;
	private int originStateId;
	
	private int birdIndex;
	private ABType birdType;

	private List<State> possibleStates;
	
	private Point target;
	private Shot shot;
	private ABObject aim;
	
	private ABObject closestPig;
	private double distanceOfClosestPig;
	
	private boolean shotTested = false;
	
	public MyShot() {
		super();

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

	public boolean isShotTested() {
		return shotTested;
	}

	public void setShotTested(boolean shotTested) {
		this.shotTested = shotTested;
	}

	
	
}
