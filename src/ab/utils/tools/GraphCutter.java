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
		List<Integer> idsToDelete = new ArrayList<Integer>();

		graph.buildGraph(LEVEL);
		
		/*
		graph.removeUnlinkedNodes();
		graph.writeShotsAandStatesInFile(LEVEL);
		*/
		
		
		
		
		System.out.println("Possible Shots: "+graph.rootState.getPossibleShots().size() );
		
		//2
		for( MyShot ms : graph.rootState.getPossibleShots() ){
			MyShot delete = ms;
			System.out.println("ID: "+delete.getShotId()+" MM: "+delete.getMiniMaxValue()+ " UC: "+delete.getUnvisitedChildren()+ " Tap: "+delete.getTapInterval());
			
			if(ms.getMiniMaxValue() > 0 && ms.getMiniMaxValue() < 20000){
				
			}
		}
		/*
		System.out.println(idsToDelete.size());
		for(Integer id: idsToDelete){
			graph.removeNodesFromMap( graph.allShots.get(id) );
		}
		
		graph.writeShotsAandStatesInFile(LEVEL);
		*/
	}
}
