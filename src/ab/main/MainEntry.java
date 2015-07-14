package ab.main;

import java.util.ArrayList;
import java.util.List;

import ab.demo.MyAgent;
import ab.demo.MyClientAgent;
import ab.objects.LearnType;
import ab.vision.ShowSeg;

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Jochen Renz,Stephen Gould,
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

public class MainEntry {

	//Args:
	//-level=N
	//-showMbr or -showReal
	//-learn=roundRobin||allshots||random
	//-recalculatePossibleShots
	public static void main(String args[]){
		int level = 1;
		boolean recalculatePossibleShots = false;
		
		boolean clientMode = false;
		String ip = null;
		int teamId = 0;
		
		LearnType learnMode = LearnType.None;
		
		if( args == null || args.length == 0 ){
			args = new String[2];
			args[0] = "-level=1";
			args[1] = "-showMBR";
		}
		
		List<String> argsList = new ArrayList<String>();
		
		for( String arg: args ){
			argsList.add(arg);
			
			if( arg.startsWith("-level") ){
				level = Integer.parseInt( arg.split("=")[1] );
			}else if( arg.startsWith("-learn") ){
				learnMode = LearnType.fromString( arg.split("=")[1] );
			}
		}

		if( !argsList.isEmpty() ){
			if( argsList.get(0).equals("-nasc") ){
				clientMode = true;
				
				if( argsList.size() > 1 ){
					ip = argsList.get(1);
				}
				if( argsList.size() > 2 ){
					teamId = Integer.parseInt( argsList.get(2) );
				}
				
			}
			
			recalculatePossibleShots = argsList.indexOf("-recalculatePossibleShots") > -1;
		}
		
		if( clientMode ){
			System.out.println("Client Mode");
			MyClientAgent na = new MyClientAgent(LearnType.None, recalculatePossibleShots, ip );
			
			na.id = teamId;
			na.run();
		}else{
			MyAgent na = new MyAgent( learnMode, recalculatePossibleShots );
			
			na.currentLevel = level;
			Thread nathre = new Thread(na);
			nathre.start();
			if( argsList.contains("-showReal") ){
				ShowSeg.useRealshape = true;
			}
			Thread thre = new Thread(new ShowSeg());
			thre.start();
		}

		

	}
}
