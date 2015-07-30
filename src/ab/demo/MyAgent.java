package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.objects.GraphNode;
import ab.objects.LearnType;
import ab.objects.MapState;
import ab.objects.MyShot;
import ab.objects.State;
import ab.planner.TrajectoryPlanner;
import ab.utils.ABPrintStream;
import ab.utils.ABUtil;
import ab.utils.Graph;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.ShowSeg;
import ab.vision.Vision;

//http://www.angrybirdsnest.com/leaderboard/angry-birds/episode/poached-eggs/
public class MyAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;	
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	
	Rectangle sling;
	ABPrintStream stream = null;
	
	//---------------------------------------------------------------------------------

	private LearnType LEARN_TYPE = LearnType.None;
	private boolean recalculatePossibleShots = false;
	
	private int MAX_LEVEL = 22;
	
	private int TIMES_IN_EACH_STAGE = Integer.MAX_VALUE;
	private int timesInThisStage = 1;
	
	//---------------------------------------------------------------------------------
	
	private Graph graph;
	
	private State actualState;
	private MyShot actualShot;
	private State lastState;
	private MyShot lastShot;
	private boolean forceShots = false;
	
	private final int BIRDS_SIZE = 10;
	
	private int previousScore = 0;
	private int numberOfbirds = -1;
	private int birdsIndex = 0;
	
	
	public MyAgent(LearnType learnType, boolean recalculatePossibleShots) {
		System.out.println("Execution starts: "+getDatetimeFormated());
		
		if( learnType.equals(LearnType.None) ){
			System.out.println("..:: EXECUTION MODE ::..");
		}else{
			System.out.println("..:: LEARNING MODE ::..");
			System.out.println("..:: TYPE "+learnType+" ::..");
			if( recalculatePossibleShots ){
				System.out.println("Recalculating possible shots");
			}
		}
		
		LEARN_TYPE = learnType;
		this.recalculatePossibleShots = recalculatePossibleShots;
		
		createReportsDir();
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		graph = new Graph();
//		firstShot = true;
		randomGenerator = new Random();
		lastShot = null;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
	}

	// run the client
	public void run() {

		logConfiguration();
		loadLevel();
		
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				numberOfbirds = -1;
				calculateShotStats(true);
				
				int totalScore = 0;
				
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				
				for(Integer key: scores.keySet()){
					totalScore += scores.get(key);
					System.out.println(" Level " + key	+ " Score: " + scores.get(key) + " ");
				}
				
				System.out.println("Total Score: " + totalScore);

				changeLevelIfNecessary();
				
				loadLevel();
				tp = new TrajectoryPlanner();
				
			} else if (state == GameState.LOST) {
				System.out.println("LOST.");
				
				numberOfbirds = -1;
				calculateShotStats(true);

				System.out.println("\n-------------------- Times in this level: "+timesInThisStage+" of "+TIMES_IN_EACH_STAGE+ "--------------------\n");

				previousScore = 0;
				timesInThisStage++;

				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out.println("Unexpected level selection page, go to the last current level : "+ currentLevel);

				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				loadLevel();
				
			} else if (state == GameState.MAIN_MENU) {
				System.out.println("Unexpected main menu page, go to the last current level : "	+ currentLevel);
				
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				ActionRobot.GoFromMainMenuToLevelSelection();
				
				loadLevel();
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println("Unexpected episode menu page, go to the last current level : "+ currentLevel);
				
				numberOfbirds = -1;
				changeLevelIfNecessary();
				
				ActionRobot.GoFromMainMenuToLevelSelection();
				loadLevel();
			} else if (state == GameState.UNKNOWN) {
				System.out.println("Unknow Game state, may the game ends in last shot. CurrentLevel: "+ currentLevel);
				numberOfbirds = -1;
				
				if( aRobot.getState() == GameState.WON || aRobot.getState() == GameState.LOST ){
					System.out.println("Updating last Shot Status");
					
					scores.put( currentLevel, StateUtil.getScore(ActionRobot.proxy) );
					
					reCalculateShotStats(true);
					
					if( aRobot.getState() == GameState.WON ){
						changeLevelIfNecessary();
						loadLevel();
						tp = new TrajectoryPlanner();
					}else{
						timesInThisStage++;
						aRobot.restartLevel();
					}
				}else{
					System.out.println("[ERROR] Unknow error. Restart Level.");
					timesInThisStage++;
					aRobot.restartLevel();
				}

			}

		}

	}

	private void loadLevel() {
		int levelLoaded = 0;
		levelLoaded = aRobot.loadLevel(currentLevel);
		if( levelLoaded != currentLevel ){
			System.out.println("[ERROR]Unble to change level.");
			currentLevel = levelLoaded;
			changeLevel( currentLevel );
		}
	}
	
	private void changeLevel( ){
		changeLevel( currentLevel );
	}
	
	private void changeLevel( int level ){
		currentLevel = level;
		previousScore = 0;
		logConfiguration();
	}

	private void changeLevelIfNecessary() {
		clearDebugsPoints();
		
		if( LEARN_TYPE.equals(LearnType.ConfirmBestResults) ){
			System.out.println("Changing Level "+getDatetimeFormated());
			
			currentLevel++;
			previousScore = 0;
			
			if( currentLevel == MAX_LEVEL ){
				System.out.println("Rebooting From start");
				currentLevel = 1;
			}
			changeLevel();
			
		}else if( isLearningMode() ){
			System.out.println("\n-------------------- Times in this level: "+timesInThisStage+" of "+TIMES_IN_EACH_STAGE+ "--------------------\n");
			
			if( timesInThisStage++ >= TIMES_IN_EACH_STAGE ){
				System.out.println("Changing Level "+getDatetimeFormated());
				
				TIMES_IN_EACH_STAGE = Integer.MAX_VALUE;
				previousScore = 0;
				timesInThisStage = 0;
				currentLevel++;
				
				if( currentLevel == MAX_LEVEL ){
					System.out.println("Rebooting From start");
					currentLevel = 1;
				}
				changeLevel();
			}
		}else{
			System.out.println("Changing Level "+getDatetimeFormated());
			
			currentLevel++;
			changeLevel();
		}
	}

	private void clearDebugsPoints() {
		if( ShowSeg.instance != null ){
			ShowSeg.debugGreenPoint.clear();
			ShowSeg.debugBluePoint.clear();
			ShowSeg.debugCyanPoint.clear();
			ShowSeg.debugRedPoint.clear();
		}
	}

	private double distance(Point p1, Point p2) {
		return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
	}
	
	public GameState solve(){
 		List<ABObject> pigs;
 		List<ABObject> birds;
 		
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

		pigs = vision.findPigsMBR();
 		birds = vision.findBirdsMBR();

 		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty() && !birds.isEmpty()) {
				
				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;

				if( numberOfbirds == -1 ){
					graph = new Graph();
					actualShot = null;
					lastState = null;
					actualState = null;
					lastShot = null;
					
					previousScore = 0;
					birdsIndex = 0;
					
					try {
						graph.buildGraph( currentLevel );
						
						if( isLearningMode() ){
							System.out.println("Cutting Nodes that scores 0 points... ");
//							graph.cutNodesWithZeroPoints( graph.rootState );
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}

					clearDebugsPoints();
					numberOfbirds = birds.size();
					
					lastState = actualState = graph.rootState;
				}else{
					lastShot = actualShot;
					calculateShotStats(false);
				}
				
				clearDebugsPoints();
				
				if( actualShot == null && actualState == null ){
					System.out.println("Creating Root...");
					actualState = graph.rootState = new State();
					actualState.setOriginShotId(-1);
					actualState.setStateId( graph.getNewStateId() );
					actualState.setBirdIndex( 0 );
					
					actualState.setPossibleShots( findPossibleShots(new ArrayList<MyShot>()) );
					
					actualState.setShotImage( ActionRobot.doScreenShot() );
					
					graph.allStates.put(actualState.getStateId(), actualState);
					
					writeImageState(1);
				}
				
				if( aRobot.getState() != GameState.PLAYING ){
					System.out.println("Game State change before find possible shots.");
					return GameState.UNKNOWN;				
				}
				
				if( actualState.getPossibleShots().isEmpty() || recalculatePossibleShots ){
					actualState.setPossibleShots( findPossibleShots( actualState.getPossibleShots() ) );
				}else{
					for( MyShot ms : actualState.getPossibleShots() ){
						Point pt = ms.getTarget();
//						System.out.println("id: "+ms.getShotId()+ " x: "+ms.getTarget().x+ " y: "+ms.getTarget().y);
						if( ShowSeg.instance != null ){
							if( ms.getTimes() > 0 ){
								ShowSeg.debugBluePoint.add( new Point( pt.x, pt.y + 2) );
							}else{
								ShowSeg.debugRedPoint.add( new Point( pt.x, pt.y - 2) );
							}
						}
					}
				}
				
				if( TIMES_IN_EACH_STAGE == Integer.MAX_VALUE ){
					TIMES_IN_EACH_STAGE = actualState.getPossibleShots().size() * 2;
				}
				
				if( aRobot.getState() != GameState.PLAYING ){
					System.out.println("Game State change before choose best shot.");
					return GameState.UNKNOWN;				
				}
				
				actualShot = chooseOneShot();

//				forceShots= true;
				if( forceShots ){
					System.out.println("...............::::::::::::::::::::::: Forcing Shots :::::::::::::::::::::::...............");
//					try{Thread.sleep(10000000);}catch(Exception e){}
					switch( birdsIndex ){
					case 0:
						/*
						sortPossibleShotsByClosesetPoint(443, 319, 90);
						actualShot = actualState.getPossibleShots().get(0);
						 */
						chooseShotById(2691, actualState.getPossibleShots());
						break;
					case 1:
						sortPossibleShotsByClosesetPoint(506, 310, 0);
						/*
						actualShot = actualState.getPossibleShots().get(0);
						if( actualState.getStateId() == 182 ){
							chooseShotById(14411, actualState.getPossibleShots());
						}else if( actualState.getStateId() == 358 ){
							idForced = 24669;
							chooseShotById(24669, actualState.getPossibleShots());
						}
						*/
						break;
					case 2:/*
						idForced = -1;
						
						sortPossibleShotsByClosesetPoint(519, 319, 80);
						actualShot = actualState.getPossibleShots().get(0);
						*/
						break;
					case 3:
						/*
						idForced = -1;
						
						sortPossibleShotsByClosesetPoint(589, 298, 75);
						actualShot = actualState.getPossibleShots().get(0);
						*/
						break;
					case 4:
						/*
						idForced = -1;
						
						sortPossibleShotsByClosesetPoint(508, 329, 0);
						actualShot = actualState.getPossibleShots().get(0);
						*/
						break;
					case 5:
						/*
						idForced = -1;
						
						sortPossibleShotsByClosesetPoint(591, 297, 80);
						actualShot = actualState.getPossibleShots().get(0);
						*/
						break;
					}

				}else{
					if( ShowSeg.instance != null ){
						ShowSeg.debugRedPoint.add(actualShot.getClosestPig().getCenter());
					}
				}
				
				if( ShowSeg.instance != null ){
					ShowSeg.debugGreenPoint.add(actualShot.getTarget());
				}
				
				shot = actualShot.getShot();
				releasePoint = actualShot.getReleasePoint();
				dx = actualShot.getShot().getDx();
				dy = actualShot.getShot().getDy();
				actualShot.setBirdType(aRobot.getBirdTypeOnSling());
				
				
				System.out.println("Shooting Bird("+birdsIndex+"): "+actualShot.getBirdType()+" at Point x: "+actualShot.getTarget().getX()+ " y: "+actualShot.getTarget().getY()+" dx: " +dx+ " dy: " +dy+ " Tap: " +actualShot.getTapInterval()+  " -> "+actualShot.getAim().getType() );
				
				//tp.getReferencePoint(sling);
				//tp.getReleaseAngle(sling, releasePoint);
				
				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					
					if(_sling != null){
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)	{
							if(dx < 0) {
								aRobot.cshoot(shot);
								
								lastState = actualState;
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
//									firstShot = false;
								}
							}
						}
						else{
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
						}
					}else{
						state = GameState.UNKNOWN;
						System.out.println("[ERROR]no sling detected, can not execute the shot, will re-segement the image. State = "+aRobot.getState());
					}
				}
				System.out.println("Solve return state: "+state);
			}
		}
		
		return state;
	}
	
	private void chooseShotById(int idForced, List<MyShot> possibleShots) {
		for( MyShot ms : actualState.getPossibleShots() ){
			if( ms.getShotId() == idForced ){
				System.out.println("\t\tFound forced id "+idForced);
				actualShot = ms;	
				break;
			}
		}
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
//			PrintStream fileStream = new PrintStream( new FileOutputStream( file, true ) );
			
			if( stream != null)	stream.close();
			
			stream = new ABPrintStream(new FileOutputStream( file, true ), System.out);
		    
			System.setOut(stream);
			System.setErr(stream);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void createReportsDir() {
		File reportFile = new File("./reports" );
		if( !reportFile.exists() ){
			reportFile.mkdir();
		}
	}

	private void reCalculateShotStats( boolean finalShot ){
		System.out.println("MyAgent.reCalculateShotStats()");
		
		int score = 0;
		clearDebugsPoints();
		
		if( finalShot ){
			if( aRobot.getState() == GameState.LOST ){
				score = previousScore;
			}else{
				score = aRobot.getScore();
			}
		}else{
			score = aRobot.getScoreInGame(); 
		}
		
		for( MyShot myShot : actualState.getPossibleShots() ){
			graph.allShots.remove( myShot.getShotId() );
		}
		actualState.getPossibleShots().clear();
		
		actualState.setTotalScore( score );
		actualState.setScore( score - previousScore );
		
		previousScore = score;
		actualState.setFinalState(finalShot);
		
		if( isLearningMode() ){
			graph.writeShotsAandStatesInFile(currentLevel);
		}
	}
	
	private void calculateShotStats(boolean finalShot) {
		System.out.println("MyAgent.calculateShotStats()");
		
		int score = 0;
		clearDebugsPoints();
		
		actualShot.setBirdIndex( birdsIndex );
		
		if( finalShot ){
			if( aRobot.getState() == GameState.LOST ){
				score = previousScore;
			}else{
				score = aRobot.getScore();
			}
		}else{
			score = aRobot.getScoreInGame(); 
		}
		
		actualState.setTotalScore( score );
		actualState.setScore( score - previousScore );
		
		previousScore = score;
		
		actualState.setFinalState(finalShot);
		
		actualShot.setTimesPlusOne();
		
		actualState = getStateIfAlreadyTested( actualState, actualShot.getPossibleStates() ); 
		
		actualState.setActive(true);
		actualShot.setActive(true);
		actualState.setBirdIndex( actualShot.getBirdIndex() );
		
		if( isLearningMode() ){
			actualState.setTimesPlusOne();
			
			graph.writeShotsAandStatesInFile(currentLevel);
		}
	}


	private MapState getMapState(Vision vision) {
		MapState mapState = new MapState();
		mapState.setBlocks( vision.findBlocksMBR() );
		mapState.setPigs( vision.findPigsMBR() );
		mapState.setTnts( vision.findTNTs() );
		return mapState;
	}

	private State getStateIfAlreadyTested(State state, List<State> possibleStates) {
		State returnState = null;
		State otherState;
		
		int tollerancePoints = 1750;
		
		if( isLearningMode() ){
			for( int i = 0; i < possibleStates.size(); i++ ){
				otherState = possibleStates.get(i);
				if( otherState.getOriginShotId() != state.getOriginShotId() ){
					System.out.println("[ERROR]Estado com parent invalido.  Actual State "+ otherState.getStateId()+ " Other "+otherState.getStateId());
					possibleStates.remove(i--);
				}
			}
		}else{
			tollerancePoints = 4000;
		}
		
		final State originState = state;
		Collections.sort( possibleStates, new Comparator<State>() {
			@Override
			public int compare(State o1, State o2) {
				int o1Abs = Math.abs( originState.getScore() - o1.getScore() );
				int o2Abs = Math.abs( originState.getScore() - o2.getScore() );
				int comp = Integer.compare(o1Abs, o2Abs);
				
//				System.out.println("o1 abs = "+o1Abs+ " o2 abs = "+o2Abs+ " comp: "+comp);
				return comp;
			}
		});
		
		if( !possibleStates.isEmpty() ){
			otherState = possibleStates.get(0);
			
			int scoreDiffenrece = ( Math.abs( state.getScore() - otherState.getScore() ) );
			
			if( scoreDiffenrece <= tollerancePoints ){
				System.out.println("State previously reached. Reloading state: "+otherState.getStateId());
				returnState = otherState;
				
				int newScore = (otherState.getScore() + state.getScore() ) / 2;
				int newTotalScore = ( otherState.getTotalScore() + state.getTotalScore() ) / 2;
				
				if( newScore != otherState.getScore() ){
					System.out.println("Setting the Score with average of "+otherState.getScore()+" and "+state.getScore()+ " = "+newScore);
				}
				
				if( newTotalScore != otherState.getTotalScore() ){
					System.out.println("Setting the Total Score with average of "+otherState.getTotalScore()+" and "+state.getTotalScore()+ " = "+newTotalScore);
				}
				
				returnState.setTotalScore( newTotalScore );
				returnState.setScore( newScore );
			}else{
				System.out.println("Estado nao encontrado, diferença mais próxima: "+scoreDiffenrece);
			}
		}
		
		if( returnState == null ){
			returnState = state;
			
			if( state.getStateId() == 0 ){
				state.setStateId( graph.getNewStateId() );
				System.out.println("Generate new state with id = "+state.getStateId());
			}else{
				System.err.println("[ERROR] Something Wrong... The state "+state.getStateId()+" was tried to be overwritten by "+(graph.getStateId()+1));
			}

			graph.allStates.put(state.getStateId(), state);
			
			writeImageState( state.getStateId() );
		}
		
		return returnState;
	}


	private void writeImageState(int stateId) {
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

	private float expectMiniMax(GraphNode node){
		float alpha = 0;
		if( node.isFinalState() ){
			State st = (State) node;
			st.setMiniMaxValue(st.getScore());
			return ((State)node).getScore();
		}
		
		if( node instanceof State ){
			State st = (State) node;
			
			for( MyShot myShot: st.getPossibleShots() ){
				if( myShot.getTimes() > 0){
					alpha = Math.max( alpha, 
							expectMiniMax(myShot) );
				}
			}
			st.setMiniMaxValue(alpha);
		}else if( node instanceof MyShot ){
			MyShot ms = (MyShot) node;
			
			float totalTimes = 0;
			for( State state: ms.getPossibleStates() ){
				totalTimes += state.getTimes();
			}
			
			if( totalTimes > 0){
				for( State state: ms.getPossibleStates() ){
					alpha = alpha + ( state.getTimes() / totalTimes *  expectMiniMax(state) );
				}
				ms.setMiniMaxValue(alpha);
			} 
		}
			
		return alpha;
	}

	private float itemTypeDistanceMultiplier(ABObject obj){
		float multi = 0;
		switch( obj.getType() ){
		case TNT:
			multi = 0.25f;
			break;
		case Pig:
			multi = 1f;
			break;
		case Ice:
			multi = 1;
			break; 	
		case Wood:
			multi = 2.0f;
		case Stone:
			multi = 3.0f;
		break;
			default:
				multi = 1;
			break;
		}
		
		return multi;
		
	}
	
	private MyShot chooseOneShot() {
		System.out.println("MyAgent.chooseOneShot("+LEARN_TYPE+")");
		MyShot theShot = null;

		System.out.println("\tCounting Unvisited Children...");
		graph.rootState.setUnvisitedChildren( graph.countUnvisitedChildren( graph.rootState ) );
		
		System.out.println("\tCalculating Minimax...");
		for( MyShot evalShot: actualState.getPossibleShots() ){
			float miniMaxValue = 0;
			
			if( evalShot.getTimes() > 0 ){
				miniMaxValue = expectMiniMax( evalShot );
			}
			
//			System.out.println("Id: "+evalShot.getShotId()+" minmax = "+evalShot.getMiniMaxValue());
			evalShot.setMiniMaxValue(miniMaxValue);
		}

		switch( LEARN_TYPE ){
		case None:
			sortPossibleShotsMyMiniMax();
			
			theShot = actualState.getPossibleShots().get(0);
			
			if( lastShot != null && lastShot.equals(theShot) ){
				theShot = actualState.getPossibleShots().get(1);
			}
			System.out.println("ExpectMiniMax Algorithm choose shot with id: "+theShot.getShotId()+ " with value "+theShot.getMiniMaxValue());
			break;
		case ConfirmBestResults:
			sortPossibleShotsMyMiniMax();
			
			theShot = actualState.getPossibleShots().get(0);
			
			if( lastShot != null && lastShot.equals(theShot) ){
				theShot = actualState.getPossibleShots().get(1);
			}
			System.out.println("ExpectMiniMax Algorithm choose shot with id: "+theShot.getShotId()+ " with value "+theShot.getMiniMaxValue());
			break;
		case RounRobin:
			sortPossibleShotsByDistanceOfClosestPig();
			
			theShot = actualState.getPossibleShots().get(0);
		break;
		case AllShots:
			sortPossibleShotsByNumberOfUnvisitedChildrenDesc();
			
			theShot = actualState.getPossibleShots().get(0);
			break;
		case Random:
			sortPossibleShotsByPseudoRandom();
			
			theShot = actualState.getPossibleShots().get(0);
			break;
		default:
			if( actualState.getPossibleShots().isEmpty() ){
				System.err.println("[ERROR] Possible shots empty.");
			}
			theShot = actualState.getPossibleShots().get(0);
			break;
		}
			
		
		calcReleasePoint( theShot.getTarget() );
		return theShot;
	}
	
	private void sortPossibleShotsByClosesetPoint( final int x, final int y, final int tap) {
		System.out.println("Ordering by -> x: "+x+" y: "+y+" t: "+tap);
		
		Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				int compare = 0;

				compare = 
						Double.compare( 
							distance(o1.getTarget(), new Point(x, y)),
							distance(o2.getTarget(), new Point(x, y))
						);
				
				if( compare == 0 ){
					compare = Integer.compare( Math.abs( tap - o1.getTapInterval() ), Math.abs(tap - o2.getTapInterval()) );
				}
				
				return compare;
			}
		});
	}

	private void sortPossibleShotsByPseudoRandom() {
		for( MyShot shot: actualState.getPossibleShots() ){
			shot.setRandomInt( randomGenerator.nextInt( actualState.getPossibleShots().size() * 3 ));
		}

		Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				int compare = Integer.compare( 	o1.getRandomInt() * (o1.getTimes() + 1) + (int) o1.getDistanceOfClosestPig(), 
												o2.getRandomInt() * (o2.getTimes() + 1) + (int) o2.getDistanceOfClosestPig() 
											 );
				
				return compare;
			}
		});
	}

	private void sortPossibleShotsByNumberOfUnvisitedChildrenDesc() {
		Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				int compare = Double.compare(o1.getUnvisitedChildren(), o2.getUnvisitedChildren()) * -1;
				if( compare == 0 ){
					compare = Double.compare(o1.getTimes(), o2.getTimes());
				}
				if( compare == 0 ){
					compare = Double.compare(o1.getDistanceOfClosestPig(), o2.getDistanceOfClosestPig());
				}
				return compare;
			}
		});
	}

	private void sortPossibleShotsByDistanceOfClosestPig() {
		Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				int compare = Double.compare(o1.getTimes(), o2.getTimes());
				if( compare == 0 ){
					compare = Double.compare(o1.getDistanceOfClosestPig() * itemTypeDistanceMultiplier(o1.getAim() ), 
											 o2.getDistanceOfClosestPig() * itemTypeDistanceMultiplier(o2.getAim() ) ) ;
				}
				return compare;
			}
		});
	}

	private void sortPossibleShotsMyMiniMax() {
		Collections.sort(actualState.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				int compare = Double.compare(o1.getMiniMaxValue(), o2.getMiniMaxValue()) * -1;
				
				if( compare == 0){
					compare = Double.compare(o1.getDistanceOfClosestPig(), o2.getDistanceOfClosestPig());
				}
				
				if( compare == 0 ){
					compare = Integer.compare(o1.getTapInterval(), o2.getTapInterval()) * -1;
				}
				
				return compare;
			}
		});
	}


	private List<MyShot> findPossibleShots(List<MyShot> oldPossibleShots) {
		System.out.println("MyAgent.findPossibleShots()");
		long time = System.currentTimeMillis();
		int discardedShots = 0;
		
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		
		MapState mapState = getMapState(vision);
		
		List<ABObject> pigs = mapState.getPigs();
		
		ABType birdType = aRobot.getBirdTypeOnSling();
		
		List<MyShot> possibleShots = new ArrayList<MyShot>();
		List<Point> releasePoints;
		
		ABObject closestPig = pigs.get(0);
		double closestPigDistance = distance(closestPig.getCenter(), new Point(0,0));
		
		for( ABObject pig: pigs ){
			double distance = distance(pig.getCenter(), new Point(0,0));
			
			if( distance < closestPigDistance ){
				closestPigDistance = distance;
				closestPig = pig;
			}
		}
		
		for( ABObject object: mapState.getAllObjects() ){
			
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
				
				if( !findCloserShot( _tpt, possibleShots ) ){
					releasePoints = calcReleasePoint(_tpt);
					for( Point releasePoint: releasePoints ){
					
						Point refPoint = tp.getReferencePoint(sling);
						
						int dx = (int)releasePoint.getX() - refPoint.x;
						int dy = (int)releasePoint.getY() - refPoint.y;
						Shot shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, 0);
	
						if( ABUtil.isReachable(vision, _tpt, shot) ){
							List<Integer> tapIntervalList = new ArrayList<Integer>();
							
							switch( birdType ){
							case RedBird:
								tapIntervalList.add(0);	break;
							case YellowBird:
								tapIntervalList.add(70);
								tapIntervalList.add(80);
								tapIntervalList.add(90);
								break; // 65-90% of the way
							case WhiteBird:
								tapIntervalList.add(70);
	//							tapIntervalList.add(75);
								tapIntervalList.add(80);
	//							tapIntervalList.add(85);
								tapIntervalList.add(90);
								break; // 70-90% of the way
							case BlackBird:
								tapIntervalList.add(70);
	//							tapIntervalList.add(75);
								tapIntervalList.add(80);
	//							tapIntervalList.add(85);
								tapIntervalList.add(90);
								break; // 70-90% of the way
							case BlueBird:
								tapIntervalList.add(5);
	//							tapIntervalList.add(65);
								tapIntervalList.add(75);
	//							tapIntervalList.add(85);
								tapIntervalList.add(90);
								break; // 65-85% of the way
							default:
								tapIntervalList.add(0);
							}
							
							for( Integer tapIn : tapIntervalList ){
								int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapIn);
								shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
								
								if( actualState.getStateId() == 0 ){
									System.err.println("[ERROR] Something Wrong origin state id = 0");
								}
								
								MyShot myShot = new MyShot();
								
								myShot.setShotId(graph.getNewShotId());
								myShot.setTimes(0);
								myShot.setOriginStateId(actualState.getStateId());
								myShot.setTarget(_tpt);
								myShot.setReleasePoint(releasePoint);
								myShot.setShot(shot);
								myShot.setAim(object);
								myShot.setTapInterval(tapIn);
								
								myShot.setClosestPig(closestPig);
								myShot.setDistanceOfClosestPig( distance(closestPig.getCenter(), _tpt) );
								
								if( !shotInOldPossibleShots(myShot, oldPossibleShots) ){
									possibleShots.add( myShot );
									
									graph.allShots.put(myShot.getShotId(), myShot);
									if( ShowSeg.instance != null ){
										ShowSeg.debugCyanPoint.add(_tpt);
									}
								}else{
									if( ShowSeg.instance != null ){
										ShowSeg.debugBluePoint.add(_tpt);
									}
								}
							}
							
							
						}else{
//							System.out.println("Nao da pra alcancar");
							discardedShots++;
							if( ShowSeg.instance != null ){
								ShowSeg.debugRedPoint.add(_tpt);
							}
						}
					}
				}else{
//					System.out.println("Tiro parecido ja testado");
					if( ShowSeg.instance != null ){
						ShowSeg.debugRedPoint.add(_tpt);
					}
					discardedShots++;
				}
			}
		}
		
		possibleShots.addAll(oldPossibleShots);
		
		System.out.println("Number of: PossibleShots: "+possibleShots.size() + " DiscardedShots: "+discardedShots+ "  calculated in: " + (System.currentTimeMillis() - time) + " miliseconds");
		return possibleShots;
	}


	private boolean shotInOldPossibleShots(MyShot myShot, List<MyShot> oldPossibleShots) {
		boolean shotExists = false;
		
		
		if( !oldPossibleShots.isEmpty() ){
			System.out.println("\tShot Target: "+myShot.getTarget() + " Shot: "+myShot.getShot()+" Interval: "+myShot.getTapInterval());
			
			
			for( MyShot ms: oldPossibleShots ){
				int distanceBetweenTargets 			= (int) distance(myShot.getTarget(), ms.getTarget());
				int distanceBetweenReleaseTarget	= (int) distance(myShot.getReleasePoint(), ms.getReleasePoint());
				boolean sameTapInterval				= myShot.getTapInterval() == ms.getTapInterval();
				
				if( sameTapInterval && distanceBetweenReleaseTarget <= 4 &&  distanceBetweenTargets <= 4){
					System.out.println("\tOther Target: "+ms.getTarget() + " Shot: "+ms.getShot()+" Interval: "+ms.getTapInterval());
					shotExists = true;
					break;
				}
			}
			
			if( !shotExists ){
				System.out.println("\t Nao existe");
			}
		}
		
		return shotExists;
	}

	private boolean findCloserShot(Point _tpt, List<MyShot> possibleShots) {
		boolean returnValue = false;
		
		for( MyShot ms : possibleShots ){
			int distanceBetween = (int) distance( ms.getTarget(), _tpt ); 
			if( distanceBetween < (BIRDS_SIZE / 2)  ){
				return true;
			}
		}
		return returnValue;
	}


	private List<Point> calcReleasePoint(Point _tpt) {
		
		// estimate the trajectory
		ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
		
		if ( pts.isEmpty() ){
			System.out.println("No release point found for the target. Try a shot with 45 degree");
			pts.add( tp.findReleasePoint(sling, Math.PI/4) );
		}
		
		return pts;
	}
	
	private String getDatetimeFormated() {
		String dateTime = "";

		Calendar c = Calendar.getInstance(); 
		dateTime = c.get(Calendar.DAY_OF_MONTH) + "/" + +c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR) + " - " + c.get(Calendar.HOUR_OF_DAY)+ ":" + c.get(Calendar.MINUTE)+ ":" + c.get(Calendar.SECOND);
		
		return dateTime;
	}
	
	private boolean isLearningMode() {
		return !LEARN_TYPE.equals(LearnType.None);
	}

}