package ab.utils.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import ab.utils.FileUtil;

public class GraphViewWriterUtil {
	private static int LEVEL = 17;
	
	private static String reportsPath = "./reports/";
	
	private static String graphViewFileName = "graphView.html";
	private static String shotsJsonFileName = "shots.json";
	private static String statesJsonFileName = "states.json";
	
	public static void main(String[] args) throws IOException {
		StringBuilder htmlShots = new StringBuilder();
		StringBuilder htmlStates = new StringBuilder();;
		StringBuilder htmlFinal = new StringBuilder();
		
		String shotsPath, statesPath, graphViewPath;
		List<String> graphViewString, shotsString, statesString;
		PrintWriter out;
		
		if( LEVEL > 0 ){
			shotsPath = reportsPath + LEVEL + "/" + shotsJsonFileName;
			statesPath = reportsPath + LEVEL + "/" + statesJsonFileName;
			
			File shotsJsonFile  = new File( shotsPath );
			File statesJsonFile = new File( statesPath );
			 
			System.out.println("Reading States... ");
			statesString = FileUtil.read(statesJsonFile);
			
			System.out.println("Reading Shots... ");
			shotsString = FileUtil.read(shotsJsonFile);
			
			
			System.out.println("Parsing Strings... ");
			for( String st : shotsString ){
				htmlShots.append( st ).append( "\n" );
			}
			
			for( String st : statesString ){
				htmlStates.append( st ).append( "\n" );
			}
		}
		
		graphViewPath = reportsPath + graphViewFileName;
		File graphViewFile  = new File( graphViewPath );
		graphViewString = FileUtil.read(graphViewFile);
		
		
		System.out.println("Processing File... ");
		boolean waitForFinalTag= false;
		for( String st : graphViewString ){
			
			if( st.contains("/*LEVEL_CHOICE*/ ") ){
				int levelSelected = LEVEL;
				st = "/*LEVEL_CHOICE*/ $('#level').children().eq("+(levelSelected)+").attr('selected', 'selected');\n";
			}
			
			if( !waitForFinalTag ){
				htmlFinal.append( st ).append( "\n" );
			}
			
			if( st.contains("id='statesTextArea'>") ){
				htmlFinal.append( "\n" ).append( htmlStates.toString() );
				waitForFinalTag= true;
			}else if( st.contains("id='shotsTextArea'>") ){
				htmlFinal.append( "\n" ).append( htmlShots.toString() );
				waitForFinalTag= true;
			}else if( st.contains("</textarea>") ){
				htmlFinal.append( st ).append( "\n\n" );
				waitForFinalTag= false;
			}
		}
		

		System.out.println("Wrinting File... ");
		out = new PrintWriter(new BufferedWriter(new FileWriter( graphViewPath , false)));
		out.write(htmlFinal.toString());
		out.flush();
		out.close();
		
		System.out.println("Done. ");
	}
}
