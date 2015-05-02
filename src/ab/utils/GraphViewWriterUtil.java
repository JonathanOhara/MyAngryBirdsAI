package ab.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class GraphViewWriterUtil {
	private static int LEVEL = 6;
	
	private static String reportsPath = "./reports/";
	
	private static String graphViewFileName = "graphView.html";
	private static String shotsJsonFileName = "shots.json";
	private static String statesJsonFileName = "states.json";
	
	public static void main(String[] args) throws IOException {
		
		String shotsPath, statesPath, graphViewPath;
		List<String> graphViewString, shotsString, statesString;
		PrintWriter out;
		
		graphViewPath = reportsPath + graphViewFileName;
		shotsPath = reportsPath + LEVEL + "/" + shotsJsonFileName;
		statesPath = reportsPath + LEVEL + "/" + statesJsonFileName;
		
		File graphViewFile  = new File( graphViewPath );
		File shotsJsonFile  = new File( shotsPath );
		File statesJsonFile = new File( statesPath );
		
		graphViewString = FileUtil.read(graphViewFile); 
		shotsString = FileUtil.read(shotsJsonFile);
		statesString = FileUtil.read(statesJsonFile);
		
		String htmlShots = "";
		for( String st : shotsString ){
			htmlShots += st + "\n";
		}
		
		String htmlStates = "";
		for( String st : statesString ){
			htmlStates += st + "\n";
		}
		
		String htmlFinal = "";
		boolean waitForFinalTag= false;
		for( String st : graphViewString ){
			
			if( st.contains("/*LEVEL_CHOICE*/ ") ){
				st = "/*LEVEL_CHOICE*/ $('#level').children().eq("+(LEVEL-1)+").attr('selected', 'selected');\n";
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
