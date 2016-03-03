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
