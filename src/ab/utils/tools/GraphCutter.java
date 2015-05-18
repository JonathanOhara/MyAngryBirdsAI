package ab.utils.tools;

import java.awt.Point;
import java.io.IOException;

import ab.objects.MyShot;
import ab.utils.Graph;

public class GraphCutter {
	private static int LEVEL = -1;
	private static int MAX_LEVEL = 21;
	
	public static void main(String[] args) throws IOException {

		if( LEVEL > 0 ){
			cut(LEVEL);
		}else{
			for( int i = 1; i <= MAX_LEVEL; i++ ){
				cut(i);
			}
		}
	}
	
	public static void cut(int lv) throws IOException{
		System.out.println("............:: LEVEL: "+lv+" ::............");
		Graph graph = new Graph();
//		List<Integer> idsToDelete = new ArrayList<Integer>();

		graph.buildGraph(lv);
		
		System.out.println("Level: "+lv);
		int times = 0;
		for( MyShot ms : graph.rootState.getPossibleShots() ){
			if( ms.getPossibleStates().size() == 0 ){
				ms.setTimes(0);
			}
			if( ms.getPossibleStates().size() == 0 || ms.getTimes() == 0){
				times++;
			}
		}
		
		//graph.cutNodesWithLessPoints(graph.rootState, 2500);
		graph.removeUnlinkedNodes();
		graph.writeShotsAandStatesInFile(lv);
		System.out.println("-----------------------------------------------------Not tested = "+times);
		/*
		System.out.println(idsToDelete.size());
		for(Integer id: idsToDelete){
			graph.removeNodesFromMap( graph.allShots.get(id) );
		}
		
		graph.writeShotsAandStatesInFile(LEVEL);
		*/
	}
	
	private static double distance(Point p1, Point p2) {
		return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
	}
}
