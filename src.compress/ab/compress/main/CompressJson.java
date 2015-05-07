package ab.compress.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import ab.compress.objects.CompressedMyShot;
import ab.objects.MyShot;
import ab.utils.FileUtil;

import com.google.gson.Gson;

public class CompressJson {

	private static int LEVEL = 1;
	
	private static String reportsPath = "./reports/";
	
	private static String shotsJsonFileName = "shots.json";
	private static String statesJsonFileName = "states.json";
	
	public static void main(String[] args) throws IOException {
		String shotsPath, statesPath;
		List<String> shotsString, statesString;
		PrintWriter out;
		
		shotsPath = reportsPath + LEVEL + "/" + shotsJsonFileName;
		statesPath = reportsPath + LEVEL + "/" + statesJsonFileName;
		
		File shotsJsonFile  = new File( shotsPath );
		File statesJsonFile = new File( statesPath );
		 
		shotsString = FileUtil.read(shotsJsonFile);
		statesString = FileUtil.read(statesJsonFile);
		
		Gson gson = new Gson();
		
		CompressedMyShot cShot;
		for(String line : shotsString){
			MyShot shot = gson.fromJson(line, MyShot.class);
			
			cShot = new CompressedMyShot();
			
			//16
			cShot.setAim( shot.getAim() );
			cShot.setBirdIndex( shot.getBirdIndex() );
			cShot.setBirdType( shot.getBirdType() );
			cShot.setClosestPig( shot.getClosestPig() );
			cShot.setDistanceOfClosestPig( shot.getDistanceOfClosestPig() );
			cShot.setMiniMaxValue(  shot.getMiniMaxValue() );
			cShot.setNumberofUnvisitedChildren( shot.getNumberofUnvisitedChildren() );
			cShot.setOriginStateId( shot.getOriginStateId() );
			cShot.setReleasePoint( shot.getReleasePoint() );
			cShot.setShot( shot.getShot() );
			cShot.setShotId( shot.getShotId() );
			cShot.setShotTested( shot.isShotTested() );
			cShot.setTapInterval( shot.getTapInterval() );
			cShot.setTarget( shot.getTarget() );
			cShot.setTimes( shot.getTimes() );
			cShot.setVisitedInLastRun( shot.isVisitedInLastRun() );
			
			System.out.println(gson.toJson(cShot));
		}
		
	}

}
