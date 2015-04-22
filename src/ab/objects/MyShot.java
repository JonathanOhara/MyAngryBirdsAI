package ab.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.ABType;

public class MyShot {
	
	private int shotId;
	private int originShotId;
	
	private int birdIndex;
	private ABType birdType;
	
	private MapState mapState;
	
	private int times = 0;
	
	private int score = 0;
	private int totalScore = 0;
	
	private List<MyShot> possibleShots;
	
	private Point target;
	private Shot shot;
	private ABObject aim;
	
	private ABObject closestPig;
	private double distanceOfClosestPig;
	
	private boolean nodeTested = false;
	
	public MyShot() {
		super();
		
		possibleShots = new ArrayList<MyShot>();
	}
	
	public void rootShot(){
		birdIndex = 1;
		shotId = 1;
		originShotId = -1;
		
		nodeTested = false;
		target = new Point(0,0);
		
		shot = null;
		aim = null;
		
	}

	public int getShotId() {
		return shotId;
	}

	public void setShotId(int shotId) {
		this.shotId = shotId;
	}

	public int getOriginShotId() {
		return originShotId;
	}

	public void setOriginShotId(int originShotId) {
		this.originShotId = originShotId;
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

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}
	
	public void setTimesPlusOne() {
		times++;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public List<MyShot> getPossibleShots() {
		return possibleShots;
	}

	public void setPossibleShots(List<MyShot> possibleShots) {
		this.possibleShots = possibleShots;
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

	public MapState getMapState() {
		return mapState;
	}

	public void setMapState(MapState mapState) {
		this.mapState = mapState;
	}

	public boolean isNodeTested() {
		return nodeTested;
	}

	public void setNodeTested(boolean nodeTested) {
		this.nodeTested = nodeTested;
	}

	
	
}
