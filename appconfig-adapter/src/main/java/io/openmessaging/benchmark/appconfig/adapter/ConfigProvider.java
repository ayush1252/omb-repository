package io.openmessaging.benchmark.appconfig.adapter;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;

/**
 * Adapter class over Azure AppConfig to provide Configuration for running tests.
 */
public class ConfigProvider {

    String connectionString;
    ConfigurationClient configurationClient;
    String labelName;

    public ConfigProvider(String conString, String labelName) {
        this.connectionString = conString;
        this.labelName = labelName;
        configurationClient = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String getConfigurationValue(ConfigurationKey configKey){
        return configurationClient.getConfigurationSetting(configKey.toString(), labelName).getValue();
    }
}