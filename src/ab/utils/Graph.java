package ab.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import ab.objects.GraphNode;
import ab.objects.MyShot;
import ab.objects.State;

public class Graph {
	public State rootState;
	
	public Map<Integer, MyShot> allShots;
	public Map<Integer, State> allStates;
	
	private int lastStateId = 1;
	private int lastShotId = 1;
	
	private File allPossibleShotsFile;
	private File allPossibleStateFile;
	
	public Graph() {
		allShots = new HashMap<Integer, MyShot>(1024);
		allStates = new HashMap<Integer, State>(256);
		
		lastStateId = 1;
		lastShotId  = 1;
	}
	
	public void buildScenarioGraph() {
		MyShot shotBeforeState;
		State stateBeforeShot;
		System.out.println("MyAgent.buildScenarioGraph()");
		if( !allStates.isEmpty() && !allShots.isEmpty() ){
			for( State state : allStates.values() ){
				if( state.getOriginShotId() == -1 ) continue;
				shotBeforeState = allShots.get( state.getOriginShotId() );
				
				if( shotBeforeState == null ){
					System.err.println("[ERROR] There is no Shot with id = "+state.getOriginShotId());
					continue;
				}
				shotBeforeState.getPossibleStates().add(state);
			}
			
			for( MyShot shot : allShots.values() ){
				stateBeforeShot = allStates.get( shot.getOriginStateId() );				
				
				if( stateBeforeShot == null ){
					System.err.println("[ERROR] There is no State with = "+shot.getOriginStateId());
					continue;
				}
				stateBeforeShot.getPossibleShots().add(shot);
			}
			
			rootState = allStates.get(1);
		}
		
	}

	public void cutNodesWithZeroPoints(GraphNode node) {
		
		if( node instanceof State ){
			State state = (State) node;
			
			for( MyShot myShot: state.getPossibleShots() ){
				cutNodesWithZeroPoints(myShot);
			}
		}else if( node instanceof MyShot ){
			MyShot myshot = (MyShot) node;
			if( myshot.getPossibleStates().size() == 1 ){
				if( myshot.getPossibleStates().get(0).getScore() == 0 ){
					removeNodesFromMap(myshot);
					myshot.getPossibleStates().remove(0);
				}
			}
		}
		
	}
	
	public void removeNodesFromMap(GraphNode node) {
		if( node instanceof State ){
			State state = (State) node;
				
			for( MyShot myshot : state.getPossibleShots() ){
				removeNodesFromMap(myshot);
			}
			
			System.out.println("State id: "+state.getStateId()+" removed from graph.");
			allStates.remove(state.getStateId());
		}else if( node instanceof MyShot ){
			MyShot myshot = (MyShot) node;
			
			for( State state: myshot.getPossibleStates() ){
				removeNodesFromMap(state);
			}
			
			System.out.println("Shot id: "+myshot.getShotId()+" removed from graph.");
			allShots.remove(myshot.getShotId());
		}
		
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------
	
	public int countUnvisitedChildren(GraphNode node) {
		if( node.isFinalState() ){
			return 0;
		}
		
		int returnValue = 0;
		if( node instanceof State ){
			State state = (State) node;
			if( state.getPossibleShots().isEmpty() ){
				return 1;
			}else{
				
				for( MyShot shot: state.getPossibleShots() ){
					returnValue += countUnvisitedChildren( shot );
				}
			}
			
			state.setUnvisitedChildren(returnValue);
		}else if( node instanceof MyShot ){
			MyShot shot = (MyShot) node;
			if( shot.getPossibleStates().isEmpty() ){
				return 1;
			}else{
				
				for( State state: shot.getPossibleStates() ){
					returnValue += countUnvisitedChildren( state );
				}
			}
			
			shot.setUnvisitedChildren(returnValue);
		}
		
		return returnValue;
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------
	
	public synchronized int getStateId(){
		return lastStateId;
	}
	
	public synchronized int getShotId(){
		return lastShotId;
	}
	
	public synchronized int getNewStateId(){
		return lastStateId++;
	}
	
	public synchronized int getNewShotId(){
		return lastShotId++;
	}

	//------------------------------------------------------------------------------------------------------------------------------------------------
	public void buildGraph(int currentLevel) throws IOException {
		System.out.println("Graph.buildGraph("+currentLevel+")");

		allPossibleShotsFile = getAllPossibleShots( currentLevel );
		allPossibleStateFile = getAllPossibleState( currentLevel );
		
		System.out.println("Loading shots and states from file.");
		
		allShots = readAllPossibleShotsFromFile(  );
		allStates = readAllPossibleStatesFromFile(  );
		
		buildScenarioGraph();
	}
	
	private File getAllPossibleShots(int currentLevel) throws IOException {
		File reportFile = new File("./reports/" + currentLevel );
		if( !reportFile.exists() ){
			reportFile.mkdir();
		}
		
		String reportsPath = reportFile.getCanonicalPath();
		
		File file = new File(reportsPath + "/shots.json");
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	private File getAllPossibleState(int currentLevel) throws IOException {
		File reportFile = new File("./reports/" + currentLevel );
		if( !reportFile.exists() ){
			reportFile.mkdir();
		}
		
		String reportsPath = reportFile.getCanonicalPath();
		
		File file = new File(reportsPath + "/states.json");
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	private Map<Integer, State> readAllPossibleStatesFromFile() {
		Map<Integer, State> map = new HashMap<Integer, State>(128);

		try {
			List<String> lines = FileUtil.read( allPossibleStateFile );
			
			Gson gson = new Gson();
			
			for(String line : lines){
				State state = gson.fromJson(line, State.class);
				
				state.setActive(false);
				
				map.put(state.getStateId(), state);
				
				if( state.getStateId() >= lastStateId ){
					lastStateId = state.getStateId() + 1; 
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
	}

	
	private Map<Integer, MyShot> readAllPossibleShotsFromFile() {
		Map<Integer, MyShot> map = new HashMap<Integer, MyShot>(1024);

		try {
			List<String> lines = FileUtil.read( allPossibleShotsFile );
			
			Gson gson = new Gson();
			
			for(String line : lines){
				MyShot shot = gson.fromJson(line, MyShot.class);
				
				shot.setActive(false);
				
				map.put(shot.getShotId(), shot);
				
				if( shot.getShotId() >= lastShotId ){
					lastShotId = shot.getShotId() + 1; 
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	public void writeShotsAandStatesInFile(int currentLevel){
		System.out.println("MyAgent.writeShotsAandStatesInFile()");
		
		
		writeShotsInFile(currentLevel);
		writeStatesInFile(currentLevel);
	}
	
	public void writeShotsInFile(int currentLevel) {
		PrintWriter out;
		StringBuilder json = new StringBuilder();
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter( getAllPossibleShots(currentLevel) , false)));
			
			Gson gson = new Gson();
			for( MyShot myShot: allShots.values() ){
				myShot.setDistanceOfClosestPig( (int) myShot.getDistanceOfClosestPig() );
				json.append( gson.toJson( myShot ) ).append( "\n" );
			}
			
			out.write( json.toString() );
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    	
	}


	public void writeStatesInFile(int currentLevel) {
		PrintWriter out;
		StringBuilder json = new StringBuilder();
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter( getAllPossibleState(currentLevel) , false)));
			
			Gson gson = new Gson();
			for( State state: allStates.values() ){
				json.append( gson.toJson( state ) ).append( "\n" );
			}
			
			out.write( json.toString() );
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------

}