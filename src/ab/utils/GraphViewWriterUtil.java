package ab.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class GraphViewWriterUtil {
	private static int LEVEL = 4;
	
	private static String reportsPath = "./reports/";
	
	private static String graphViewFileName = "graphView.html";
	private static String shotsJsonFileName = "shots.json";
	private static String statesJsonFileName = "states.json";
	
	public static void main(String[] args) throws IOException {
		String htmlShots = "";
		String htmlStates = "";
		String htmlFinal = "";
		
		String shotsPath, statesPath, graphViewPath;
		List<String> graphViewString, shotsString, statesString;
		PrintWriter out;
		
		
		
		if( LEVEL > 0 ){
			shotsPath = reportsPath + LEVEL + "/" + shotsJsonFileName;
			statesPath = reportsPath + LEVEL + "/" + statesJsonFileName;
			
			File shotsJsonFile  = new File( shotsPath );
			File statesJsonFile = new File( statesPath );
			 
			shotsString = FileUtil.read(shotsJsonFile);
			statesString = FileUtil.read(statesJsonFile);
			
			for( String st : shotsString ){
				htmlShots += st + "\n";
			}
			
			for( String st : statesString ){
				htmlStates += st + "\n";
			}
		}
		
		graphViewPath = reportsPath + graphViewFileName;
		File graphViewFile  = new File( graphViewPath );
		graphViewString = FileUtil.read(graphViewFile);
		
		boolean waitForFinalTag= false;
		for( String st : graphViewString ){
			
			if( st.contains("/*LEVEL_CHOICE*/ ") ){
				int levelSelected = LEVEL > 0 ? LEVEL - 1: 1;
				st = "/*LEVEL_CHOICE*/ $('#level').children().eq("+(levelSelected)+").attr('selected', 'selected');\n";
			}
			
			if( !waitForFinalTag ){
				htmlFinal += st + "\n";
			}
					
			
			if( st.contains("id='statesTextArea'>") ){
				htmlFinal += "\n" + htmlStates;
				waitForFinalTag= true;
			}else if( st.contains("id='shotsTextArea'>") ){
				htmlFinal += "\n" + htmlShots;
				waitForFinalTag= true;
			}else if( st.contains("</textarea>") ){
				htmlFinal += st + "\n\n";
				waitForFinalTag= false;
			}
		}
		

		out = new PrintWriter(new BufferedWriter(new FileWriter( graphViewPath , false)));
		out.write(htmlFinal);
		out.flush();
		out.close();		
	}
}
