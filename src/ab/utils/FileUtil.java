package ab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	public static List<String> read(File file) throws IOException { 
		BufferedReader buffRead = new BufferedReader(new FileReader(file));
		String linha = "";
		List<String> stringList = new ArrayList<String>();
		
		while (true) {
			if (linha == null) break;
			linha = buffRead.readLine();
			if (linha != null && !linha.trim().isEmpty()) stringList.add(linha);
		}
		
		buffRead.close();
		
		return stringList;
	}
}
