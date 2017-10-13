package com.attentec.coffeepanic;

import java.util.Map;

public final class EnvironmentConfiguration implements Configuration {
    private final Map<String, String> values;

    public EnvironmentConfiguration() {
        values = System.getenv();
    }

    public String getServerUrl() throws ConfigurationException {
        return get("SERVER_URL");
    }

    public Credentials getCredentials() throws ConfigurationException {
        String username = get("USERNAME");
        String password = get("PASSWORD");

        return new Credentials() {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }
        };
    }

    private String get(String key) throws ConfigurationException {
        String value = values.get(key);

        if (value == null) {
            throw new ConfigurationException("Environment variable \"" + key + "\" not set");
        }

        return value;
    }
}
