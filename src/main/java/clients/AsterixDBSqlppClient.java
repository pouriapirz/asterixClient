package clients;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import structures.Query;

public class AsterixDBSqlppClient extends AbstractAsterixDBClient {
	private final static int ASTX_QUERY_PORT = 19002;
	private final static String ASTX_SQLPP_QUERY_URL_SUFFIX = "/query/sqlpp";
	
	static PrintWriter pw;		//stats writer
	static PrintWriter rw;		//results writer (if requested)

	static URIBuilder roBuilder; 
	static DefaultHttpClient httpclient;
	static HttpGet httpGet;

	public static void main(String args[]){
		if(args.length < 4){
			System.out.println("Wrong/Insufficient set of arguments.\nCorrect Usage:\nargs[0]: Number of iterations\nargs[1]: CC Url\nargs[2]: Path to the file contaiing the workload\nargs[3]: Path to the file to dump response times\n(Optional) args[4]: Path to the file to dump results");
			return;
		}
		int iterations = Integer.parseInt( args[0] );
		String ccUrl = args[1];
		String workloadFilePath = args[2];
		String statsFile = args[3];
		String resultsFile = null;	//default: do not dump returned results
		if(args.length == 5){		
			resultsFile = args[4];	//dump returned results to this file
		}

		init(ccUrl, statsFile, resultsFile);
		loadWorkload(workloadFilePath);
		
		pw.println("Iteration\tQName\tTime");	//TSV header for the stats file 

		for(int i=0; i<iterations; i++){
			System.out.println("\nStarting iteration "+i);
			for(String nextQ : qSeq){
				Query q = idToQuery.get(nextQ);
				long rspt = executeQuery(q);
				pw.println(i+"\t"+q.getName()+"\t"+rspt);
				System.out.println("Query "+q.getName()+" took "+rspt+" ms");
			}
		}

		terminate();
		System.out.println("\nAll Done successfully ! ");
	}

	private static long executeQuery(Query q) {
		String content = null;
		long rspTime = -1L;						//initial value
		try {
			roBuilder.setParameter("query", q.getBody());
			URI uri = roBuilder.build();
			httpGet.setURI(uri);

			long s = System.currentTimeMillis();					//Start the timer right before sending the query
			HttpResponse response = httpclient.execute(httpGet);	//Actual execution against the server
			HttpEntity entity = response.getEntity();				//Extract response
			content = EntityUtils.toString(entity);					//We make sure we extract the results from response
			long e = System.currentTimeMillis();					//Stop the timer
			EntityUtils.consume(entity);

			rspTime = (e-s);					//Total duration

			if(rw != null){						//Dump returned results (if requested)
				rw.println("\n"+q.getName()+"\n"+content);
			}
		} catch (Exception ex) {
			System.err.println("Problem in read-only query execution against Asterix\n"+content);
			ex.printStackTrace();
			return -1L;		//invalid time (as the query crashed)
		}
		return rspTime;
	}

	private static void init(String ccUrl, String statsFile, String resultsFile){
		try {
			roBuilder = new URIBuilder("http://"+ccUrl+":"+ASTX_QUERY_PORT+ASTX_SQLPP_QUERY_URL_SUFFIX);	//This URI format is for **READ-ONLY** queries
			httpclient = new DefaultHttpClient();
			httpGet = new HttpGet();
			pw = new PrintWriter(statsFile);
			if(resultsFile != null) { rw = new PrintWriter(resultsFile); }
		} catch (URISyntaxException e) {
			System.err.println("Issue(s) in initializing the HTTP client"); 
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("Issue in initializing printWriter(s)"); 
			e.printStackTrace();
		}
	}

	private static void terminate() {
		if(pw != null) { pw.close(); }
		if(rw != null) { rw.close(); }
		if(httpclient != null) { httpclient.getConnectionManager().shutdown(); }
	}
}