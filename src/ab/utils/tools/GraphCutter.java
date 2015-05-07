package ab.utils.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ab.objects.MyShot;
import ab.utils.Graph;

public class GraphCutter {
	private static int LEVEL = 10;
	
	public static void main(String[] args) throws IOException {
		Graph graph = new Graph();

		graph.buildGraph(LEVEL);
		
		List<Integer> idsToDelete = new ArrayList<Integer>();
		
//		idsToDelete.add(39);
		
		System.out.println("Possible Shots: "+graph.rootState.getPossibleShots().size() );
		
		for(Integer id: idsToDelete){
			graph.removeNodesFromMap( graph.allShots.get(id) );
		}
		
		graph.writeShotsAandStatesInFile(LEVEL);
		
		for( MyShot ms : graph.rootState.getPossibleShots() ){
			System.out.println("ID: "+ms.getShotId()+" MM: "+ms.getMiniMaxValue()+ " UC: "+ms.getUnvisitedChildren()+ " Tap: "+ms.getTapInterval());
		}
	}
}
