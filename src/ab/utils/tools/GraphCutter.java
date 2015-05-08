package ab.utils.tools;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ab.objects.MyShot;
import ab.utils.Graph;

public class GraphCutter {
	private static int LEVEL = 13;
	
	public static void main(String[] args) throws IOException {
		Graph graph = new Graph();
		List<Integer> idsToDelete = new ArrayList<Integer>();

		graph.buildGraph(LEVEL);
		
		graph.removeUnlinkedNodes();
		graph.writeShotsAandStatesInFile(LEVEL);
		
		
		for( MyShot ms : graph.rootState.getPossibleShots() ){

		}
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
