package com.attentec.coffeepanic;

public interface Configuration {
    public String getServerUrl() throws ConfigurationException;

    public Credentials getCredentials() throws ConfigurationException;
}
