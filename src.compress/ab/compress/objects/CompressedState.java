package ab.compress.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import ab.objects.GraphNode;
import ab.objects.MapState;
import ab.objects.MyShot;

public class CompressedState implements GraphNode {
	
	@SerializedName("sI")
	private int stateId;
	
	@SerializedName("oSI")
	private int originShotId;
	
	@SerializedName("bI")
	private int birdIndex;
	
	@SerializedName("t")
	private int times = 0;
	
	@SerializedName("nUvC")
	private int numberofUnvisitedChildren;
	
	@SerializedName("vLR")
	private boolean visitedInLastRun = false;
	
	@SerializedName("s")
	private int score = 0;
	
	@SerializedName("tS")
	private int totalScore = 0;
	
	@SerializedName("fS")
	private boolean finalState;
	
	private transient MapState mapState;
	private transient List<MyShot> possibleShots;
	private transient BufferedImage shotImage;

	public CompressedState() {
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
