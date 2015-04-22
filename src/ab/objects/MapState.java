package ab.objects;

import java.util.List;

import ab.vision.ABObject;

public class MapState {
	List<ABObject> blocks;
	List<ABObject> pigs;
	List<ABObject> tnts;

	public List<ABObject> getBlocks() {
		return blocks;
	}
	public void setBlocks(List<ABObject> blocks) {
		this.blocks = blocks;
	}
	public List<ABObject> getPigs() {
		return pigs;
	}
	public void setPigs(List<ABObject> pigs) {
		this.pigs = pigs;
	}
	public List<ABObject> getTnts() {
		return tnts;
	}
	public void setTnts(List<ABObject> tnts) {
		this.tnts = tnts;
	}
}