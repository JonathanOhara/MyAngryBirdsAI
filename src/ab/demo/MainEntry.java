package ab.demo;

import java.util.ArrayList;
import java.util.List;

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
	// the entry of the software.
	public static void main(String args[]){
		int level = 1;
		boolean learnMode = false;
		boolean roundRoubinLearn = false;
		
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
			}
		}
		
		if( argsList.contains("-learn") ){
			learnMode = true;
		}
		
		if( argsList.contains("-roundRobin") ){
			roundRoubinLearn = true;
		}

		MyAgent na = new MyAgent( learnMode, roundRoubinLearn );

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
