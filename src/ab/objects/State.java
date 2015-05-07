package ab.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class State implements GraphNode {
	
	private int stateId;
	private int originShotId;
	
	private int birdIndex;
	
	private int times = 0;
	private int score = 0;
	private int totalScore = 0;
	
	private boolean finalState;
	
	private transient MapState mapState;

	@Expose(serialize = true, deserialize = false)
	private boolean active = false;
	@Expose(serialize = true, deserialize = false)
	private int unvisitedChildren;
	@Expose(serialize = true, deserialize = false)
	private float miniMaxValue;
	
	private transient List<MyShot> possibleShots;
	private transient BufferedImage shotImage;

	public State() {
		super();
		
		possibleShots = new ArrayList<MyShot>();
	}
	
	public MapState getMapState() {
		return mapState;
	}

	public void setMapState(MapState mapState) {
		this.mapState = mapState;
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

	public boolean isFinalState() {
		return finalState;
	}

	public void setFinalState(boolean finalState) {
		this.finalState = finalState;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
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

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}
	
	public void setTimesPlusOne() {
		times++;
	}

	public BufferedImage getShotImage() {
		return shotImage;
	}

	public void setShotImage(BufferedImage shotImage) {
		this.shotImage = shotImage;
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

	public float getMiniMaxValue() {
		return miniMaxValue;
	}

	public void setMiniMaxValue(float miniMaxValue) {
		this.miniMaxValue = miniMaxValue;
	}
	
	
}
