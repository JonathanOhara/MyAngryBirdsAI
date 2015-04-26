package ab.objects;

import java.util.ArrayList;
import java.util.List;

import ab.vision.ABObject;

public class MapState {
	List<ABObject> blocks;
	List<ABObject> pigs;
	List<ABObject> tnts;

	public List<ABObject> getAllObjects(){
		List<ABObject> all = new ArrayList<ABObject>( blocks.size() + pigs.size() + tnts.size() );
		
		all.addAll(blocks);
		all.addAll(pigs);
		all.addAll(tnts);
		
		return all;
	}
	
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