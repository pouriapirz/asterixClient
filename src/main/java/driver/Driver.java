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
package driver;

import client.AsterixDBClient;
import configuration.ClientConfig;
import configuration.Constants;

public class Driver {
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Correct Usage:\n");
            System.out.println("\t[0]: CLIENT_HOME home has to be set to a valid path.");
            return;
        }
        String clientHome = args[0];
        clientHome = clientHome.replaceAll("/$", "");
        String clientConfigFile = clientHome + "/" + Constants.CONFIG_FILE;
        ClientConfig config = new ClientConfig(clientConfigFile, clientHome);
        AsterixDBClient client = new AsterixDBClient(config);
        client.execute();

        System.out.println("\nAsterixDB client finished.");
    }
}
