package ab.demo.other;

import java.awt.Point;

import ab.vision.ABObject;

public class MyShot {
	private Point target;
	private Shot shot;
	private ABObject aim;
	
	public MyShot(Point target, Shot shot, ABObject aim) {
		super();
		this.target = target;
		this.shot = shot;
		this.aim = aim;
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
	
	
	
}
