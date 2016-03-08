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
