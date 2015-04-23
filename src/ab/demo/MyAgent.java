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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.objects.MapState;
import ab.objects.MyShot;
import ab.planner.TrajectoryPlanner;
import ab.utils.ABUtil;
import ab.utils.StateUtil;
import ab.vision.ABObject;
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

	private Map<Integer, MyShot> allShots;
	
	private File allShotFile;
	
	private MyShot previousShot = null;
	private MyShot actualShot = null;
	
	private final int BIRDS_SIZE = 10;
	
	private int previousScore = 0;
	private int numberOfbirds = -1;
	private int shotId = 1;
	
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

		aRobot.loadLevel(currentLevel);
		
		allShots = new HashMap<Integer, MyShot>(128);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				calculateShotStats(true);
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				System.out.println("Score = "+score);
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
				numberOfbirds = -1;
			} else if (state == GameState.LOST) {
				calculateShotStats(true);
				System.out.println("Restart");
				aRobot.restartLevel();
				numberOfbirds = -1;
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
				numberOfbirds = -1;
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				numberOfbirds = -1;
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
				numberOfbirds = -1;
			}

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
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
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
				
				long time = System.currentTimeMillis();
				
				if( numberOfbirds == -1 ){
					ShowSeg.debugBluePoint.clear();
					numberOfbirds = vision.findBirdsMBR().size();
					
					//ler Arquivo linha a linha e colocar no Map
				}
				
				try {
					File allShotFile = getCenarioShots();
				} catch (IOException e) {
					e.printStackTrace();
				}
/*				    
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false)));
				    out.close();
*/
					
				//------------------------------------------------------------------------------------------------------------------------------------------
				
				if( firstShot ){
					System.out.println("First shoots");
					previousShot = null;
					
					actualShot = new MyShot();
					actualShot.rootShot();
					
					actualShot.setPossibleShots( findPossibleShots(vision, pigs) );
				}else{
					previousShot.setTotalScore( aRobot.getScore() );
					previousShot.setScore( aRobot.getScore() - previousScore );
				}
				
				calculateShotStats(false);
				
				//------------------------------------------------------------------------------------------------------------------------------------------
			
				System.out.println("Tempo: "+(System.currentTimeMillis() - time));
					
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

				previousShot = actualShot;
				
				actualShot = chooseOneShot(actualShot);
				
				ShowSeg.debugBluePoint.add(actualShot.getTarget());
				
				shot = actualShot.getShot();
				dx = actualShot.getShot().getDx();
				dy = actualShot.getShot().getDy();
					
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


	private File getCenarioShots() throws IOException {
		File reportFile = new File("./reports/" + currentLevel );
		if( !reportFile.exists() ){
			reportFile.mkdir();
		}
		
		String reportsPath = reportFile.getCanonicalPath();
		
		File file = new File(reportsPath + "/cenario.json");
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}


	private void calculateShotStats(boolean finalShot) {
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);
		
		MapState mapState = new MapState();
		mapState.setBlocks( vision.findBirdsMBR() );
		mapState.setPigs( vision.findPigsMBR() );
		mapState.setTnts( vision.findTNTs() );
		
		actualShot.setBirdIndex( numberOfbirds - vision.findBirdsMBR().size() + 1 );
		actualShot.setBirdType(aRobot.getBirdTypeOnSling());
		actualShot.setTimesPlusOne();
		actualShot.setFinalShot(finalShot);

		previousScore = aRobot.getScore();
	}

	private MyShot chooseOneShot(MyShot actualShot2) {
		MyShot theShot = null;
		
		Collections.sort(actualShot.getPossibleShots(), new Comparator<MyShot>() {
			@Override
			public int compare(MyShot o1, MyShot o2) {
				return Double.compare(o1.getDistanceOfClosestPig(), o2.getDistanceOfClosestPig());
			}
		});
		
		theShot = actualShot.getPossibleShots().get(0);
		
		return theShot;
	}


	private List<MyShot> findPossibleShots(Vision vision, List<ABObject> pigs) {
		List<MyShot> possibleShots = new ArrayList<MyShot>();
		Point releasePoint;
		
		ABObject closestPig = pigs.get(0);
		double closestPigDistance = distance(closestPig.getCenter(), new Point(0,0));
		
		for( ABObject pig: pigs ){
			double distance = distance(pig.getCenter(), new Point(0,0));
			
			System.out.println("distance = "+distance);
			
			if( distance < closestPigDistance ){
				closestPigDistance = distance;
				closestPig = pig;
				System.out.println("minor");
			}
		}
		
		ShowSeg.debugRedPoint.add(closestPig.getCenter());
		
		for( ABObject object: vision.findBlocksMBR() ){
			if( object.width > 200 && object.height > 200 ){
				//ERRO ele achou que o menu da direita eh um objeto pulando...
				continue;
			}
//			System.out.println(object);
			
			double targetX;
			double targetY;
			
			List<Point> pointsToTry = new ArrayList<Point>();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
				
				if( ABUtil.isReachable(vision, _tpt, shot) ){
					MyShot myShot = new MyShot();
					
					myShot.setShotId(++shotId);
					myShot.setNodeTested(false);
					myShot.setOriginShotId(actualShot.getShotId());
					myShot.setTarget(_tpt);
					myShot.setShot(shot);
					myShot.setAim(object);
					
					myShot.setClosestPig(closestPig);
					myShot.setDistanceOfClosestPig( distance(closestPig.getCenter(), _tpt) );
					
					possibleShots.add( myShot );
				}

			}	
		}
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
