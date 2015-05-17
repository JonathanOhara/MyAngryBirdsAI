package ab.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import ab.objects.GraphNode;
import ab.objects.MyShot;
import ab.objects.State;

import com.google.gson.Gson;

public class Graph {
	public State rootState;
	
	public Map<Integer, MyShot> allShots;
	public Map<Integer, State> allStates;
	
	private List<MyShot> shotsWithoutLink;
	private List<State> statesWithoutLink;
	
	private int lastStateId = 1;
	private int lastShotId = 1;
	
	private ZipFile allPossibleShotsFile;
	private ZipFile allPossibleStateFile;
	
	private boolean WRITE_UNCOMPRESSED_FILES = false;
	
	public Graph() {
		allShots = new HashMap<Integer, MyShot>(1024);
		allStates = new HashMap<Integer, State>(256);
		
		lastStateId = 1;
		lastShotId  = 1;
	}
	
	public void buildScenarioGraph() {
		System.out.println("Graph.buildScenarioGraph()");
		MyShot shotBeforeState;
		State stateBeforeShot;
		
		shotsWithoutLink = new ArrayList<MyShot>();
		statesWithoutLink = new ArrayList<State>();

		if( !allStates.isEmpty() && !allShots.isEmpty() ){
			for( State state : allStates.values() ){
				if( state.getOriginShotId() == -1 ) continue;
				shotBeforeState = allShots.get( state.getOriginShotId() );
				
				if( shotBeforeState == null ){
					System.err.println("[ERROR] There is no Shot with id = "+state.getOriginShotId()+"(from State id:"+state.getStateId()+")");
					statesWithoutLink.add(state);
					continue;
				}
				shotBeforeState.getPossibleStates().add(state);
			}
			
			for( MyShot shot : allShots.values() ){
				stateBeforeShot = allStates.get( shot.getOriginStateId() );				
				
				if( stateBeforeShot == null ){
					System.err.println("[ERROR] There is no State with = "+shot.getOriginStateId()+"(from Shot id:"+shot.getShotId()+")");
					shotsWithoutLink.add(shot);
					
					continue;
				}
				stateBeforeShot.getPossibleShots().add(shot);
			}
			
			rootState = allStates.get(1);
		}
		
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------

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
					
					if( !myshot.getPossibleStates().isEmpty() ){
						allStates.remove( myshot.getPossibleStates().get(0).getStateId() );
					}
				}
			}
		}
		
	}
	
	public void cutNodesWithLessPoints(GraphNode node, int points) {
		
		if( node instanceof State ){
			State state = (State) node;
			
			for( MyShot myShot: state.getPossibleShots() ){
//				System.out.println("Ms: "+myShot.getShotId());
				cutNodesWithLessPoints(myShot, points);
			}
			
			if( state.getPossibleShots().isEmpty() ){
				allStates.remove(state.getStateId() );
			}
		}else if( node instanceof MyShot ){
			MyShot myshot = (MyShot) node;
			
			if( !myshot.getPossibleStates().isEmpty() ){
				for( State st : myshot.getPossibleStates() ){		
					if( st.getScore() <= points ){
						System.out.println("Removing state: "+st.getStateId()+ " with score: "+st.getScore());
						removeNodesFromMap(st);
					}	
				}
				
				if( myshot.getPossibleStates().isEmpty() ){
					allShots.remove(myshot.getShotId());
				}
			}
		}
			
	}
	
	public void removeUnlinkedNodes(){
		for( State st : statesWithoutLink ){
			removeNodesFromMap(st);
		}
		
		for( MyShot ms: shotsWithoutLink ){
			removeNodesFromMap(ms);
		}
	}
	
	public void removeNodesFromMap(GraphNode node) {
		if( node instanceof State ){
			State state = (State) node;
				
			for( MyShot myshot : state.getPossibleShots() ){
				removeNodesFromMap(myshot);
			}
			
			System.out.println("State id: "+state.getStateId()+" removed from graph. Points: "+ state.getScore());
			allStates.remove(state.getStateId());
		}else if( node instanceof MyShot ){
			MyShot myshot = (MyShot) node;
			
			if( !myshot.getPossibleStates().isEmpty() ){
				for( State state: myshot.getPossibleStates() ){
					removeNodesFromMap(state);
				}
				
				System.out.println("Shot id: "+myshot.getShotId()+" removed from graph. Minimax: "+myshot.getMiniMaxValue() );
				allShots.remove(myshot.getShotId());
			}
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

		allPossibleShotsFile = getAllPossibleShotsFile( currentLevel );
		allPossibleStateFile = getAllPossibleStateFile( currentLevel );
		
		System.out.println("Loading shots and states from file.");
		
		allShots = readAllPossibleShotsFromFile(  );
		allStates = readAllPossibleStatesFromFile(  );
		
		buildScenarioGraph();
	}
	
	private String getReportsPath(int currentLevel) throws IOException {
		File reportFile = new File("./reports/" + currentLevel );
		if( !reportFile.exists() ){
			reportFile.mkdir();
		}
		
		String reportsPath = reportFile.getCanonicalPath();
		return reportsPath;
	}
	
	private ZipFile getAllPossibleShotsFile(int currentLevel) throws IOException {
		String reportsPath = getReportsPath(currentLevel);
		
	    ZipFile zipFile = new ZipFile(reportsPath + "/shots.zip");

		return zipFile;
	}
	
	private ZipFile getAllPossibleStateFile(int currentLevel) throws IOException {
		String reportsPath = getReportsPath(currentLevel);
		
		ZipFile zipFile = new ZipFile(reportsPath + "/states.zip");

		return zipFile;
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
		StringBuilder json = new StringBuilder();
		
		try {
			Gson gson = new Gson();
			for( MyShot myShot: allShots.values() ){
				myShot.setDistanceOfClosestPig( (int) myShot.getDistanceOfClosestPig() );
				json.append( gson.toJson( myShot ) ).append( "\n" );
			}
			
			if( WRITE_UNCOMPRESSED_FILES ){
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter( new File(getReportsPath(currentLevel) + "/shots.json"), false)));
				
				out.write( json.toString() );
				out.flush();
				out.close();
			}
			//Zip File
			
			File zipFile = new File( getReportsPath(currentLevel) + "/shots.zip" );
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
			ZipEntry e = new ZipEntry("shots.json");
			zout.putNextEntry(e);
			
			byte[] data = json.toString().getBytes();
			zout.write(data, 0, data.length);
			zout.closeEntry();

			zout.flush();
			zout.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	    	
	}


	public void writeStatesInFile(int currentLevel) {
		StringBuilder json = new StringBuilder();

		try {
			Gson gson = new Gson();
			for( State state: allStates.values() ){
				json.append( gson.toJson( state ) ).append( "\n" );
			}
			
			if( WRITE_UNCOMPRESSED_FILES ){
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter( new File(getReportsPath(currentLevel) + "/states.json") , false)));
				
				out.write( json.toString() );
				out.flush();
				out.close();
			}
			//Zip File
			
			File zipFile = new File( getReportsPath(currentLevel) + "/states.zip" );
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
			ZipEntry e = new ZipEntry("states.json");
			zout.putNextEntry(e);
			
			byte[] data = json.toString().getBytes();
			zout.write(data, 0, data.length);
			zout.closeEntry();

			zout.flush();
			zout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------

}