/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.objects.MapState;
import ab.objects.MyShot;
import ab.objects.State;
import ab.planner.TrajectoryPlanner;
import ab.utils.ABUtil;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.ShowSeg;
import ab.vision.Vision;

import com.google.gson.Gson;

public class MyAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	
	Rectangle sling;
	
	//---------------------------------------------------------------------------------

	private boolean LEARNING = true;
	
	private int TIMES_IN_EACH_STAGE = 250;
	private int timesInThisStage = 0;
	
	//---------------------------------------------------------------------------------
	
	private File allPossibleShotsFile;
	private File allPossibleStateFile;
	
	private State rootState;
	private State actualState;
	private MyShot actualShot;
	
	private Map<Integer, MyShot> allShots;
	private Map<Integer, State> allStates;
	
	private final int BIRDS_SIZE = 10;
	
	private int previousScore = 0;
	private int numberOfbirds = -1;
	private int birdsIndex = 0;
	private int lastStateId = 1;
	private int lastShotId = 1;
	
	// a standalone implementation of the Naive Agent
	public MyAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		logConfiguration();
		aRobot.loadLevel(currentLevel);
		
		allShots = new HashMap<Integer, MyShot>(128);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				calculateShotStats(true);
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
//				aRobot.loadLevel(++currentLevel);
				aRobot.loadLevel(currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				calculateShotStats(true);
				System.out.println("Restart");
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
				
			} else if (state == GameState.MAIN_MENU) {
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private void changeLevelIfNecessary() {
		System.out.println("Times in this level: "+timesInThisStage+" of "+TIMES_IN_EACH_STAGE);
		if( timesInThisStage++ >= TIMES_IN_EACH_STAGE ){
			System.out.println("Changing Level...");
			timesInThisStage = 0;
			currentLevel++;
			logConfiguration();
		}
	}


	private double distance(Point p1, Point p2) {
		return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
	}
	
	public GameState solve(){

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
        // get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();

 		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {
				
				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;

				//------------------------------------------------------------------------------------------------------------------------------------------
				
				//Entrou no jogo.
				if( numberOfbirds == -1 ){
					System.out.println("...ENTER IN THE GAME...");
					rootState = null;
					actualShot = null;
					actualState = null;
					
					previousScore = 0;
					birdsIndex = 0;
					lastStateId = 1;
					lastShotId  = 1;
					
					try {
						allPossibleShotsFile = getAllPossibleShots();
						allPossibleStateFile = getAllPossibleState();
						
						allShots = readAllPossibleShotsFromFile(  );
						allStates = readAllPossibleStatesFromFile(  );
						
						buildScenarioGraph();
					} catch (IOException e) {
						e.printStackTrace();
					}

					ShowSeg.debugBluePoint.clear();
					numberOfbirds = vision.findBirdsMBR().size();
					
					actualState = rootState;
				}else{
					calculateShotStats(false);
				}
				
				
				if( actualShot == null && actualState == null ){
					actualState = rootState = new State();
					actualState.setOriginShotId(-1);
					actualState.setStateId( lastStateId++ );
					actualState.setBirdIndex( 0 );
					actualState.setMapState(getMapState(vision));
					
					actualState.setPossibleShots( findPossibleShots(vision, pigs) );
					
					actualState.setShotImage( ActionRobot.doScreenShot() );
					
					allStates.put(actualState.getStateId(), actualState);
					
					writeImageState(1);
				}
			
				if( actualState.getPossibleShots().isEmpty() ){
					actualState.setPossibleShots( findPossibleShots(vision, pigs) );
				}
			
//				for( MyShot myshot : possibleShots ){
//					ShowSeg.debugBluePoint.add(myshot.getTarget());
//				}
//				try {
//					System.out.println("W8ing");
//					Thread.sleep(100000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				ShowSeg.debugBluePoint = new ArrayList<Point>();
//				ShowSeg.debugRedPoint = new ArrayList<Point>();

				actualShot = chooseOneShot();
				
				ShowSeg.debugBluePoint.add(actualShot.getTarget());
				
				shot = actualShot.getShot();
				releasePoint = actualShot.getReleasePoint();
				dx = actualShot.getShot().getDx();
				dy = actualShot.getShot().getDy();
				actualShot.setBirdType(aRobot.getBirdTypeOnSling());
				
				System.out.println("Shooting Bird("+birdsIndex+") - "+actualShot.getBirdType()+" at Point x: "+actualShot.getTarget().getX()+ " y: "+actualShot.getTarget().getY()+" -> "+actualShot.getAim().getType() );
					/*
				{
					// random pick up a pig
					ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));
					
					Point _tpt = pig.getCenter();// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = randomGenerator.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					releasePoint = calcReleasePoint(_tpt); 
					
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);


					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println("Release Point: " + releasePoint);
						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						int tapInterval = 0;
						switch (aRobot.getBirdTypeOnSling()) 
						{

						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlackBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlueBird:
							tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
						default:
							tapInterval =  60;
						}

						int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}
				*/

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								
								actualState = new State();
								actualState.setOriginShotId( actualShot.getShotId() );
								birdsIndex++;
								actualState.setShotImage( ActionRobot.doScreenShot() );
								
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}
	
	private void logConfiguration() {
		try{
			File reportFile = new File("./reports/" + currentLevel );
			if( !reportFile.exists() ){
				reportFile.mkdir();
			}
			
			String reportsPath = reportFile.getCanonicalPath();
			
			File file = new File(reportsPath + "/log.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			PrintStream fileStream = new PrintStream( new FileOutputStream( file, true ) );
			
			System.setOut(fileStream);
			System.setErr(fileStream);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void buildScenarioGraph() {
		System.out.println("MyAgent.buildScenarioGraph()");
		if( !allStates.isEmpty() && !allShots.isEmpty() ){
			for( State state : allStates.values() ){
				if( state.getOriginShotId() == -1 ) continue;
				MyShot shotBeforeState = allShots.get( state.getOriginShotId() );
				
				if( shotBeforeState == null ){
					System.err.println("There is no Shot with id = "+state.getOriginShotId());
					continue;
				}
				shotBeforeState.getPossibleStates().add(state);
			}
			
			for( MyShot shot : allShots.values() ){
				State stateBeforeShot = allStates.get( shot.getOriginStateId() );				
				
				if( stateBeforeShot == null ){
					System.err.println("There is no State with = "+shot.getOriginStateId());
					continue;
				}
				stateBeforeShot.getPossibleShots().add(shot);
			}
			
			actualState = rootState = allStates.get(1);
		}
	}

	private List<String> read(File file) throws IOException { 
		BufferedReader buffRead = new BufferedReader(new FileReader(file));
		String linha = "";
		List<String> stringList = new ArrayList<String>();
		
		while (true) {
			if (linha == null) break;
			linha = buffRead.readLine();
			if (linha != null && !linha.trim().isEmpty()) stringList.add(linha);
		}
		
		buffRead.close();
		
		return stringList;
	}

	private Map<Integer, State> readAllPossibleStatesFromFile() {
		System.out.println("MyAgent.readAllPossibleStatesFromFile()");
		Map<Integer, State> map = new HashMap<Integer, State>();

		try {
			List<String> lines = read( allPossibleStateFile );
			
			Gson gson = new Gson();
			
			for(String line : lines){
				State state = gson.fromJson(line, State.class);
				
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
		System.out.println("MyAgent.readAllPossibleShotsFromFile()");
		Map<Integer, MyShot> map = new HashMap<Integer, MyShot>();

		try {
			List<String> lines = read( allPossibleShotsFile );
			
			Gson gson = new Gson();
			
			for(String line : lines){
				MyShot shot = gson.fromJson(line, MyShot.class);
				
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


	private File getAllPossibleShots() throws IOException {
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
	
	private File getAllPossibleState() throws IOException {
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


	private void calculateShotStats(boolean finalShot) {
		System.out.println("MyAgent.calculateShotStats()");
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);
		
		MapState mapState = getMapState(vision);
		
		actualState.setMapState(mapState);
		actualShot.setBirdIndex( birdsIndex );
		
		if( finalShot ){
			actualState.setTotalScore( aRobot.getScore() );
			actualState.setScore( aRobot.getScore() - previousScore );
			
			previousScore = aRobot.getScore();
			
		}else{
			actualState.setTotalScore( aRobot.getScoreInGame() );
			actualState.setScore( aRobot.getScoreInGame() - previousScore );
			
			previousScore = aRobot.getScoreInGame();
		}
		
		actualState.setFinalState(finalShot);
		
		actualShot.setTimesPlusOne();
		actualShot.setShotTested(true);
		
		actualState = getStateIfAlreadyTested( actualState, actualShot.getPossibleStates() ); 
		actualState.setTimesPlusOne();
		
		writeStatesInFile();
		writeShotsInFile();
	}


	private MapState getMapState(Vision vision) {
		MapState mapState = new MapState();
		mapState.setBlocks( vision.findBlocksMBR() );
		mapState.setPigs( vision.findPigsMBR() );
		mapState.setTnts( vision.findTNTs() );
		return mapState;
	}

	private State getStateIfAlreadyTested(State state, List<State> possibleStates) {
		System.out.println("MyAgent.getStateIfAlreadyTested()");
		State returnState = null;
		
		for( State otherState : possibleStates ){
			if( Math.abs( state.getScore() - otherState.getScore()) <= 200 && otherState.getOriginShotId() == state.getOriginShotId() ){
				returnState = state;
				returnState.setTimesPlusOne();
				break;
			}
		}
		
		if( returnState == null ){
			returnState = state;
			
			if( state.getStateId() == 0 ){
				state.setStateId( lastStateId++ );
			}else{
				System.err.println("Something Wrong... The state "+state.getStateId()+" was tried to be overwritten.");
			}

			allStates.put(state.getStateId(), state);
			
			writeImageState( state.getStateId() );
		}
		
		return returnState;
	}


	private void writeImageState(int stateId) {
		System.out.println("MyAgent.writeImageState()");
		try{
			File reportFile = new File("./reports/" + currentLevel );
			if( !reportFile.exists() ){
				reportFile.mkdir();
			}
			String reportsPath = reportFile.getCanonicalPath();
			
			
			File stateDir = new File(reportsPath + "/states");
			if( !stateDir.exists() ){
				stateDir.mkdir();
			}
			String statesPath = stateDir.getCanonicalPath();
			
			File stateIdDir = new File(statesPath + "/" + stateId);
			if( !stateIdDir.exists() ){
				stateIdDir.mkdir();
			}
			String stateIdPath = stateIdDir.getCanonicalPath();
			
			String fileNumber = String.format("%05d", 1 + stateIdDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jpg");
				}
			}).length );
			
			File outputfile = new File( stateIdPath + "/" + fileNumber + ".jpg" );
			ImageIO.write(actualState.getShotImage(), "jpg", outputfile);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}


	private void writeShotsInFile() {
		System.out.println("MyAgent.writeShotsInFile()");
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter( getAllPossibleShots() , false)));
			
			Gson gson = new Gson();
			for( MyShot myShot: allShots.values() ){
				String json = gson.toJson( myShot );
				out.write(json + "\n");
			}
			
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    	
	}


	private void writeStatesInFile() {
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter( getAllPossibleState() , false)));
			
			Gson gson = new Gson();
			for( State state: allStates.values() ){
				String json = gson.toJson( state );
				out.write(json + "\n");
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private MyShot chooseOneShot() {
		System.out.println("MyAgent.chooseOneShot()");
		MyShot theShot = null;

		if( LEARNING ){

			Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
				@Override
				public int compare(MyShot o1, MyShot o2) {
					return Double.compare(o1.getTimes(), o2.getTimes());
				}
			});
			
			theShot = actualState.getPossibleShots().get(0);
			
		}else{
		
			Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
				@Override
				public int compare(MyShot o1, MyShot o2) {
					return Double.compare(o1.getDistanceOfClosestPig(), o2.getDistanceOfClosestPig());
				}
			});
			
			theShot = actualState.getPossibleShots().get(0);
		} 
		
		
		System.out.println("Shot choosed id = "+theShot.getShotId());
		return theShot;
	}


	private List<MyShot> findPossibleShots(Vision vision, List<ABObject> pigs) {
		System.out.println("MyAgent.findPossibleShots()");
		long time = System.currentTimeMillis();
		
		ABType birdType = aRobot.getBirdTypeOnSling();
		
		
		List<MyShot> possibleShots = new ArrayList<MyShot>();
		Point releasePoint;
		
		ABObject closestPig = pigs.get(0);
		double closestPigDistance = distance(closestPig.getCenter(), new Point(0,0));
		
		for( ABObject pig: pigs ){
			double distance = distance(pig.getCenter(), new Point(0,0));
			
			if( distance < closestPigDistance ){
				closestPigDistance = distance;
				closestPig = pig;
			}
		}
		
		ShowSeg.debugRedPoint.add(closestPig.getCenter());
		
		for( ABObject object: actualState.getMapState().getAllObjects() ){
			if( object.width > 200 && object.height > 200 ){
				//ERRO ele achou que o menu da direita eh um objeto pulando...
				continue;
			}
			
			double targetX;
			double targetY;
			
			List<Point> pointsToTry = new ArrayList<Point>();
			
			targetX = object.x;
			targetY = object.y;
			pointsToTry.add(new Point( (int)targetX, (int)targetY ));
			
			targetX += BIRDS_SIZE;
			while( targetX <= object.x + object.width ){
				pointsToTry.add(new Point( (int)targetX, (int)targetY ));
				targetX += BIRDS_SIZE;
			}
			
			targetX = object.x;
			targetY = object.y + BIRDS_SIZE;
			
			while( targetY <= object.y + object.height ){
				pointsToTry.add(new Point( (int)targetX, (int)targetY ));
				targetY += BIRDS_SIZE;
			}
			
			for( Point _tpt : pointsToTry ){
				
				int tapInterval = 0;
				
				releasePoint = calcReleasePoint(_tpt);
				
				Point refPoint = tp.getReferencePoint(sling);
				
				int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
				int dx = (int)releasePoint.getX() - refPoint.x;
				int dy = (int)releasePoint.getY() - refPoint.y;
				Shot shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
				
				
				/**
				 * TODO Logica de tap interval para outros passaros
				 */
				
				if( ABUtil.isReachable(vision, _tpt, shot) ){
					List<Integer> tapIntervalList = new ArrayList<Integer>();
					
					switch( birdType ){
					case RedBird:
						tapIntervalList.add(0);	break;
					case YellowBird:
						tapIntervalList.add(65);
						tapIntervalList.add(70);
						tapIntervalList.add(75);
						tapIntervalList.add(80);
						tapIntervalList.add(85);
						tapIntervalList.add(90);
						break; // 65-90% of the way
					case WhiteBird:
						tapIntervalList.add(70);
						tapIntervalList.add(75);
						tapIntervalList.add(80);
						tapIntervalList.add(85);
						tapIntervalList.add(90);
						break; // 70-90% of the way
					case BlackBird:
						tapIntervalList.add(70);
						tapIntervalList.add(75);
						tapIntervalList.add(80);
						tapIntervalList.add(85);
						tapIntervalList.add(90);
						break; // 70-90% of the way
					case BlueBird:
						tapIntervalList.add(65);
						tapIntervalList.add(70);
						tapIntervalList.add(75);
						tapIntervalList.add(80);
						tapIntervalList.add(85);
						break; // 65-85% of the way
					default:
						tapIntervalList.add(0);
					}
					
					for( Integer tapIn : tapIntervalList ){
						tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapIn);
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
						
						MyShot myShot = new MyShot();
						
						myShot.setShotId(lastShotId++);
						myShot.setShotTested(false);
						myShot.setOriginStateId(actualState.getStateId());
						myShot.setTarget(_tpt);
						myShot.setReleasePoint(releasePoint);
						myShot.setShot(shot);
						myShot.setAim(object);
						myShot.setTapInterval(tapIn);
						
						myShot.setClosestPig(closestPig);
						myShot.setDistanceOfClosestPig( distance(closestPig.getCenter(), _tpt) );
						
						possibleShots.add( myShot );
						
						allShots.put(myShot.getShotId(), myShot);
					}
				}

			}	
		}
		
		System.out.println("MyAgent.findPossibleShots() = "+possibleShots.size());
		System.out.println("Tempo: "+(System.currentTimeMillis() - time));
		return possibleShots;
	}


	private Point calcReleasePoint(Point _tpt) {
		
		// estimate the trajectory
		ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
//		System.out.println("Pts = "+pts.get(0));
//		System.out.println("Pts = "+pts.get(1));
		// do a high shot when entering a level to find an accurate velocity
		
		Point releasePoint = null;
		if (firstShot && pts.size() > 1) 
		{
			releasePoint = pts.get(1);
		}
		else if (pts.size() == 1)
			releasePoint = pts.get(0);
		else if (pts.size() == 2)
		{
			// randomly choose between the trajectories, with a 1 in
			// 6 chance of choosing the high one
			if (randomGenerator.nextInt(6) == 0)
				releasePoint = pts.get(1);
			else
				releasePoint = pts.get(0);
		}
		else
			if(pts.isEmpty())
			{
				System.out.println("No release point found for the target");
				System.out.println("Try a shot with 45 degree");
				releasePoint = tp.findReleasePoint(sling, Math.PI/4);
			}
		
		return releasePoint;
	}


	public static void main(String args[]) {

		MyAgent na = new MyAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
