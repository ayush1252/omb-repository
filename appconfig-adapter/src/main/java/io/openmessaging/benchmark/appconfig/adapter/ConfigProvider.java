package io.openmessaging.benchmark.appconfig.adapter;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Adapter class over Azure AppConfig to provide Configuration for running tests.
 */
public class ConfigProvider {
    private static final Logger log = LoggerFactory.getLogger(ConfigProvider.class);

    private static volatile ConfigProvider provider = null;
    private static String labelName = null;
    private static final Object lockObject = new Object();
    private final ConfigurationClient configurationClient;

    private ConfigProvider() {
        String connectionString = System.getenv("AppConfigConnectionString");
        this.configurationClient = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public static ConfigProvider getInstance(String environmentName) {
        if (provider == null) {
            synchronized (lockObject){
                if(provider == null){
                    labelName = environmentName;
                    provider = new ConfigProvider();
                }
            }
        }

        if(!Objects.equals(labelName, environmentName))
            throw new RuntimeException("Environment Mismatch detected");

        return provider;
    }

    public String getConfigurationValue(ConfigurationKey configKey){
        return configurationClient.getConfigurationSetting(configKey.toString(), labelName).getValue();
    }

    public NamespaceMetadata getNamespaceMetaData(String configName) throws JsonProcessingException {
        try{
            final ConfigurationSetting configurationSetting = configurationClient.getConfigurationSetting(configName, labelName);
            return new ObjectMapper().readValue(configurationSetting.getValue(),
                    NamespaceMetadata.class);
        } catch(Exception e){
            log.error(String.valueOf(e));
            throw e;
        }
    }
}