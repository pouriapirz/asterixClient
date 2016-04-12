# AsterixDB Client

## Prerequisites
* A suitable *nix environment (Linux, OSX)
* JDK 1.8+
* Maven 3.1.1 or greater

## Steps
1. Check out the project in a directory via git. Assuming that the path to the directory is $HOME/asterixClient (or any other directory based on your choice), we will refer to this directory as **CLIENT_HOME** in the rest of this document.
2. Set CLIENT_HOME as an environment variable on the machine you are running the client from (replace the path with the directory you checked out the project into in the previous step):

  ```
  > export CLIENT_HOME=$HOME/asterixClient
  ```
3. Go to CLIENT_HOME and build the project's artifacts by executing the following commands:

  ```
  > cd $CLIENT_HOME
  > mvn clean package 
  ```
Upon a successful build, a new directory named 'target' will be created under CLIENT_HOME that contains the jar file for the client with its dependencies.
4. The AsterixDB client needs two configuration files under the 'conf' directory to run:

 * _conf.json_: This is a json file that contains the settings for various parameters that client uses to run. A template for this file should already be available under '$CLIENT_HOME/conf' directory. Here is the list of parameters and their description:
 
     Parameter |Type |Description |
      --- | --- | --- |
      cc |string |Hostname/IP of the machine running the AsterixDB instance's cluster controller  |
      port |integer |HTTP api port for queries in the AsterixDB instance |
      query.language |string |The language of the queries in the workload: 'aql' or 'sqlpp' |
      iterations |integer |Number or rounds the full workload needs to be executed by the client |
      stats.file |string |Full path to the file that will contain the test's statistics by the end of the test |
      dump.results |boolean |Whether to dump the results per query after its execution: 'true' or 'false' |
      results.file |string |Full path to the file that will contain the dump of the queries' results  |

     As an alternative (e.g. when connecting to another system than AsterixDB) it is also possible to replace the
     single `query.language` parameter by 2 more generic parameters:

     Parameter |Type |Description |
      --- | --- | --- |
      path |string |The path of the URI that will receive the query |
      parameter |string |The name of the HTTP parameter for the query |

     So when setting `cc` to "127.0.0.1", `port` to "19002", `path` to "query", and `parameter` to "statement", the URI
    for a query "QUERY" will be `http://127.0.0.1:19002/query/parameter=QUERY`.

 * _workload.txt_: This file defines a specific read-only workload which is a sequence of queries that will be run, in
   order, by the client in each iteration of a test. Each line of the file should be a full, absolute path to either a
   readable file that contains a complete query in 'aql' or 'sqlpp' or a directory that will be recursively visited.
   (By a complete query, we basically mean it should be exactly the same as the statements that you would type inside
   the query-box when using the Web-API of AsterixDB).

5. Once you modified and saved the configuration and workload files (with 'conf.json' and 'workload.txt' under the '$CLIENT_HOME/conf' directory respectively) you can run the AsterixDB client by invoking the 'run.sh' script under the '$CLIENT_HOME/scripts' directory:

  ```
  > $CLIENT_HOME/scripts/run.sh
  ```
As client runs, it shows messages (for tracing its progress) on the screen and once it finishes successfully, the summary report on the test's statistics can be found in the file specified as 'stats.file' in '$CLIENT_HOME/conf.json' (if 'stats.file' is not set by the user, the client writes it into its default location under the '$CLIENT_HOME/output' directory).
