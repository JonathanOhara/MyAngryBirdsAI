package ab.demo.other;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.ABType;

public class MyShot {
	
	private int shotId;
	
	private int birdIndex;
	private ABType birdType;
	
	private int times = 0;
	
	private int score = 0;
	private int totalScore = 0;
	
	private List<MyShot> possibleShots;
	
	//mapState
	//originshot
	
	private Point target;
	private Shot shot;
	private ABObject aim;
	
	private boolean complete = false;
	
	private ABObject closestPig;
	private double distanceOfClosestPig;
	
	public MyShot() {
		super();
		
		possibleShots = new ArrayList<MyShot>();
	}
	
	public void serRootShot(){
		birdIndex = 1;
		
		complete = false;
		target = new Point(0,0);
		
		shot = null;
		aim = null;
		
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
	
}
