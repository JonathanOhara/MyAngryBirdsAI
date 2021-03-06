package ab.compress.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.management.DescriptorKey;

import com.google.gson.annotations.SerializedName;

import ab.demo.other.Shot;
import ab.objects.GraphNode;
import ab.objects.State;
import ab.vision.ABObject;
import ab.vision.ABType;

public class CompressedMyShot implements GraphNode {
	
	@SerializedName("sId")
	private int shotId;
	
	@SerializedName("oSId")
	private int originStateId;
	
	@SerializedName("bI")
	private int birdIndex;

	@SerializedName("bT")
	private ABType birdType;
	
	@SerializedName("t")
	private int times = 0;
	
	@SerializedName("nUvC")
	private int numberofUnvisitedChildren;
	
	@SerializedName("vLR")
	private boolean visitedInLastRun = false;
	
	@SerializedName("sH")
	private boolean shotTested = false;

	@SerializedName("tI")
	private int tapInterval;
	
	@SerializedName("tg")
	private Point target;
	
	@SerializedName("rP")
	private Point releasePoint;
	
	@SerializedName("s")
	private Shot shot;
	
	@SerializedName("a")
	private ABObject aim;
	
	@SerializedName("cP")
	private ABObject closestPig;
	
	@SerializedName("dCP")
	private double distanceOfClosestPig;
	
	@SerializedName("mMV")
	private float miniMaxValue;
	
	private transient List<State> possibleStates;
	private transient int randomInt;
	
	public int getRandomInt() {
		return randomInt;
	}

	public void setRandomInt(int randomInt) {
		this.randomInt = randomInt;
	}

	public CompressedMyShot() {
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

	public boolean isShotTested() {
		return shotTested;
	}

	public void setShotTested(boolean shotTested) {
		this.shotTested = shotTested;
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

	public int getNumberofUnvisitedChildren() {
		return numberofUnvisitedChildren;
	}

	public void setNumberofUnvisitedChildren(int numberofUnvisitedChildren) {
		this.numberofUnvisitedChildren = numberofUnvisitedChildren;
	}

	public boolean isVisitedInLastRun() {
		return visitedInLastRun;
	}

	public void setVisitedInLastRun(boolean visitedInLastRun) {
		this.visitedInLastRun = visitedInLastRun;
	}	
	
}
