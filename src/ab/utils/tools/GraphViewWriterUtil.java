package ab.utils.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipFile;

import ab.objects.MyShot;
import ab.objects.State;
import ab.utils.FileUtil;

import com.google.gson.Gson;

public class GraphViewWriterUtil {
	private static int LEVEL = 21;
	
	private static boolean IGNORE_NOT_USED_SHOTS = true;
	
	private static String reportsPath = "./reports/";
	
	private static String graphViewFileName = "graphView.html";
	private static String shotsJsonFileName = "shots.zip";
	private static String statesJsonFileName = "states.zip";
	
	public static void main(String[] args) throws IOException {
		Gson gson = new Gson();
		
		StringBuilder htmlShots = new StringBuilder();
		StringBuilder htmlStates = new StringBuilder();;
		StringBuilder htmlFinal = new StringBuilder();

		String shotsPath, statesPath, graphViewPath;
		List<String> graphViewString, shotsString, statesString;
		PrintWriter out;
			
		if( LEVEL > 0 ){
			shotsPath = reportsPath + LEVEL + "/" + shotsJsonFileName;
			statesPath = reportsPath + LEVEL + "/" + statesJsonFileName;
			
			ZipFile shotsJsonFile  = new ZipFile( shotsPath );
			ZipFile statesJsonFile = new ZipFile( statesPath );
			 
			System.out.println("Reading States... ");
			statesString = FileUtil.read(statesJsonFile);
			
			System.out.println("Reading Shots... ");
			shotsString = FileUtil.read(shotsJsonFile);
			
			
			System.out.println("Parsing Strings... ");
			for( String st : shotsString ){
				if( IGNORE_NOT_USED_SHOTS ){
					MyShot shot = gson.fromJson(st, MyShot.class);
					if(shot.getTimes() > 0){
						htmlShots.append( st ).append( "\n" );	
					}
				}else{
					htmlShots.append( st ).append( "\n" );
				}
			}
			
			for( String st : statesString ){
				if( IGNORE_NOT_USED_SHOTS ){
					State state = gson.fromJson(st, State.class);
					if( state.getTimes() > 0 || state.getStateId() == 1 ){
						htmlStates.append( st ).append( "\n" );
					}
				}else{
					htmlStates.append( st ).append( "\n" );
				}
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
			
			if( st.contains("id='statesTextArea'") ){
				htmlFinal.append( "\n" ).append( htmlStates.toString() );
				waitForFinalTag= true;
			}else if( st.contains("id='shotsTextArea'") ){
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
