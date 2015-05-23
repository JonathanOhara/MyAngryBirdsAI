package ab.utils.tools;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ab.objects.MyShot;
import ab.objects.State;
import ab.utils.Graph;

public class GraphCutter {
	private static int LEVEL = 0;
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
		List<Integer> idsToDelete = new ArrayList<Integer>();

		graph.buildGraph(lv);
		
		System.out.println("Level: "+lv);
		
		int cutValue = -1;
		int times = 0;
		int weakShots = 0;
		
		
		for( MyShot ms : graph.rootState.getPossibleShots() ){
			if( ms.getShotId() == 138 ){
//				System.out.println("\tpossible states 0: "+ms.getPossibleStates().get(0).getScore()+ " times: "+ms.getPossibleStates().get(0).getTimes());
//				System.out.println("\tpossible states 1: "+ms.getPossibleStates().get(1).getScore()+ " times: "+ms.getPossibleStates().get(1).getTimes());
			}
			
			if( ms.getPossibleStates().size() == 0 || ms.getTimes() == 0){
				ms.setTimes(0);
				times++;
			}else{
				int score = 0;
				int number = 0;
				for( State st : ms.getPossibleStates() ){
					score += st.getScore();
					number++;
				}
				
				if( number > 0 ){
					if( score <= cutValue ){
						weakShots++;
						idsToDelete.add(ms.getShotId());
					}
					
//					System.out.println("Final: "+(score / number)+ " id: "+ms.getShotId());
				}
			}
		}
		
		System.out.println("Number of Weak Shots = "+weakShots );
		System.out.println("::::::::::::::::::::::::::::::::::::::::::: Not tested = "+times);
		
		graph.removeUnlinkedNodes();
		
		for(Integer id: idsToDelete){
			System.out.println("Removing..............................");
			graph.removeNodesFromMap( graph.allShots.get(id), true );
		}
		
		graph.writeShotsAandStatesInFile(lv);
	}
	
	private static double distance(Point p1, Point p2) {
		return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
	}
}
