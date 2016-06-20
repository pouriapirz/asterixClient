/*
 * Copyright by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import configuration.ClientConfig;
import configuration.Constants;
import structure.Query;

public class AsterixDBClient {

    private final ClientConfig config;
    private int iterations;
    private URIBuilder roBuilder;
    private DefaultHttpClient httpclient;
    private HttpGet httpGet;
    private PrintWriter pw; //stats writer
    private PrintWriter rw; //results writer (if requested)
    private HashMap<String, Query> idToQuery;
    private ArrayList<String> qSeq;

    public AsterixDBClient(ClientConfig config) {
        this.config = config;
    }

    public void execute() {
        initialize();
        for (int i = 0; i < iterations; i++) {
            System.out.println("\niteration " + i); //progress trace message
            for (String nextQ : qSeq) {
                Query q = idToQuery.get(nextQ);
                long rspt = executeQuery(q);
                pw.println(i + "\t" + q.getName() + "\t" + rspt);
                System.out.println("Query " + q.getName() + "\t" + rspt + " ms"); //progress trace message
            }
            pw.flush();
        }
        terminate();
    }

    private long executeQuery(Query q) {
        String content = null;
        long rspTime = Constants.INVALID_TIME; //initial value
        try {
            roBuilder.setParameter(Constants.QUERY_PARAMETER, q.getBody());
            URI uri = roBuilder.build();
            httpGet.setURI(uri);

            long s = System.currentTimeMillis(); //Start the timer
            HttpResponse response = httpclient.execute(httpGet); //Actual execution against the server
            HttpEntity entity = response.getEntity();
            // TODO find a way to process large results.
            // content = EntityUtils.toString(entity);
            EntityUtils.consume(entity); //Make sure to consume the results
            long e = System.currentTimeMillis(); //Stop the timer
            rspTime = (e - s); //Total duration

            if (rw != null) { //Dump returned results (if requested)
                rw.println("\n" + q.getName() + "\n" + content);
                rw.flush();
            }
        } catch (Exception ex) {
            System.err.println("Problem in read-only query execution against Asterix\n" + content);
            ex.printStackTrace();
            return Constants.INVALID_TIME; //invalid time (query was not successful)
        }
        return rspTime;
    }

    private void initialize() {
        try {
            String cc = (String) config.getParamValue(Constants.CC_URL);
            int port = Constants.DEFAULT_PORT;
            if (config.isParamSet(Constants.PORT)) {
                port = (int) config.getParamValue(Constants.PORT);
            }
            String qLang = (String) config.getParamValue(Constants.QUERY_LANG);
            switch (qLang) {
                case Constants.AQL:
                    roBuilder = new URIBuilder("http://" + cc + ":" + port + Constants.AQL_URL_SUFFIX);
                    break;
                case Constants.SQLPP:
                    roBuilder = new URIBuilder("http://" + cc + ":" + port + Constants.SQLPP_URL_SUFFIX);
                    break;
                default:
                    System.err.println("Invalid Query Language: " + qLang + " (Valid values are " + Constants.AQL
                            + " and " + Constants.SQLPP + " ).");
                    return;
            }

            httpclient = new DefaultHttpClient();
            httpGet = new HttpGet();

            iterations = (int) config.getParamValue(Constants.ITERATIONS);

            String workloadFilePath = config.getHomePath() + "/" + Constants.WORKLOAD_FILE;
            loadWorkload(workloadFilePath);

            String statsFile = config.getHomePath() + "/" + Constants.DEFAULT_STATS_FILE;
            if (config.isParamSet(Constants.STATS_FILE)) {
                statsFile = (String) config.getParamValue(Constants.STATS_FILE);
            }
            pw = new PrintWriter(statsFile);
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            Date dateobj = new Date();
            String currentTime = df.format(dateobj);
            pw.println(currentTime); //Add the test time as the header
            pw.println("\nIteration\tQName\tTime"); //TSV header

            rw = null;
            if (config.isParamSet(Constants.DUMP_RESULTS) && (boolean) config.getParamValue(Constants.DUMP_RESULTS)) {
                String resultsFile = config.getHomePath() + "/" + Constants.DEFAULT_RESULTS_FILE;
                if (config.isParamSet(Constants.RESULTS_FILE)) {
                    resultsFile = (String) config.getParamValue(Constants.RESULTS_FILE);
                }
                rw = new PrintWriter(resultsFile);
            }
        } catch (URISyntaxException e) {
            System.err.println("Issue(s) in initializing the HTTP client");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("Issue in initializing printWriter(s)");
            e.printStackTrace();
        }
    }

    private void terminate() {
        if (pw != null) {
            pw.flush();
            pw.close();
        }
        if (rw != null) {
            rw.flush();
            rw.close();
        }
        if (httpclient != null) {
            httpclient.getConnectionManager().shutdown();
        }
    }

    /**
     * @param workloadFile:
     *            Each line is the full path to a file, containing one valid query.
     *            The order of lines in this file specifies the order of queries during execution.
     *            Lines starting with the COMMENT_TAG are ignored.
     */
    private void loadWorkload(String workloadFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(workloadFile));
            String line;
            if (qSeq == null) {
                qSeq = new ArrayList<>();
            }
            qSeq.clear();
            if (idToQuery == null) {
                idToQuery = new HashMap<>();
            }
            idToQuery.clear();
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(Constants.COMMENT_TAG)) {
                    continue;
                }
                File f = new File(line.trim());
                loadQuery(f);
                qSeq.add(new String(f.getName()));
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error in loading workload from " + workloadFile);
            e.printStackTrace();
        }
    }

    private void loadQuery(File f) {
        try {
            String qPath = f.getAbsolutePath();
            String qName = f.getName();
            if (idToQuery.containsKey(qName)) {
                return;
            }
            BufferedReader in = new BufferedReader(new FileReader(qPath));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str).append("\n");
            }
            idToQuery.put(qName, new Query(f.getName(), sb.toString()));
            in.close();

        } catch (Exception e) {
            System.err.println("Error in reading query from file " + f.getAbsolutePath());
            e.printStackTrace();
        }
    }

}
