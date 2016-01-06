package clients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import structures.Query;

public abstract class AbstractAsterixDBClient {
	
	final static String COMMENT_TAG = "#";
	static HashMap<String, Query> idToQuery;
	static ArrayList<String> qSeq;
	
	/**
	 * @param workloadFile: Each line is the full path to a file, containing one valid query.
	 * 						The order of lines in this file specifies the order of queries during execution.
	 * 						Lines starting with the COMMENT_TAG are ignored.
	 */
	public static void loadWorkload(String workloadFile){
		try {
			BufferedReader br = new BufferedReader(new FileReader(workloadFile));
			String line;
			if(qSeq == null){
				qSeq = new ArrayList<>();
			}
			qSeq.clear();
			if(idToQuery == null){
				idToQuery = new HashMap<String, Query>();
			}
			idToQuery.clear();
			while ((line = br.readLine()) != null) {
				if(line.trim().startsWith(COMMENT_TAG)){
					continue;
				}
				File f = new File(line.trim());
				loadQuery(f);
				qSeq.add( new String(f.getName()) );
			}
			br.close();
		} catch (Exception e) {
			System.err.println("Error in loading queries sequence from file "+workloadFile);
			e.printStackTrace();
		} 
	}
	
	private static void loadQuery(File f){
		try {
			String qPath = f.getAbsolutePath();
			String qName = f.getName();
			if(idToQuery.containsKey(qName)){
				return;
			}
			BufferedReader in = new BufferedReader(new FileReader(qPath));
			StringBuffer sb = new StringBuffer();
			String str;
			while ((str = in.readLine()) != null) {
				sb.append(str).append("\n");
			}
			idToQuery.put(qName, new Query(f.getName(), sb.toString()));
			in.close();
			
		} catch (Exception e) {
			System.err.println("Error in reading query from file "+f.getAbsolutePath());
			e.printStackTrace();
		}
	}

}
