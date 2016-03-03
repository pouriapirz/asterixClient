package configuration;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientConfig {

    private final String homePath;
    private Map<String, Object> params;

    public ClientConfig(String configFile, String homePath) {
        this.homePath = homePath;
        parseConfigFile(configFile);
    }

    public boolean isParamSet(String paramName) {
        return params.containsKey(paramName);
    }

    public Object getParamValue(String paramName) {
        return params.get(paramName);
    }

    public String getHomePath() {
        return homePath;
    }

    private void parseConfigFile(String file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            params = mapper.readValue(new File(file), Map.class);
        } catch (Exception e) {
            System.err.println("Problem in parsing the JSON config file.");
            e.printStackTrace();
        }
    }

}
