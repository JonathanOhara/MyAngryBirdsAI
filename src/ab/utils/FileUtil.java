package ab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
	public static List<String> read(File file) throws IOException {
		List<String> stringList = new ArrayList<String>();
		
		if( file != null && file.exists() ){
			BufferedReader buffRead = new BufferedReader(new FileReader(file));
			String linha = "";
			
			while (true) {
				if (linha == null) break;
				linha = buffRead.readLine();
				if (linha != null && !linha.trim().isEmpty()) stringList.add(linha);
			}
			
			buffRead.close();
		}
		
		return stringList;
	}

	public static List<String> read(ZipFile file) throws IOException {
		List<String> stringList = new ArrayList<String>();

		if( file != null ){
			Enumeration<? extends ZipEntry> entries = file.entries();
			InputStream input = file.getInputStream(entries.nextElement());
			
			BufferedReader buffRead = new BufferedReader( new InputStreamReader( input, "UTF-8" ) );
			String linha = "";
			
			while (true) {
				if (linha == null) break;
				linha = buffRead.readLine();
				if (linha != null && !linha.trim().isEmpty()) stringList.add(linha);
			}
			
			buffRead.close();
		}
		
		return stringList;
	}
}
