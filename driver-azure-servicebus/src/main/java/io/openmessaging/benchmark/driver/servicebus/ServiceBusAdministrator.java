package io.openmessaging.benchmark.driver.servicebus;

import com.azure.core.management.AzureEnvironment;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import io.openmessaging.benchmark.driver.NamespaceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static io.openmessaging.benchmark.appconfig.adapter.EnvironmentName.Production;

public class ServiceBusAdministrator {
    private static final Logger log = LoggerFactory.getLogger(ServiceBusAdministrator.class);

    static NamespaceMetadata metadata;
    static ConfigProvider configProvider;
    static CredentialProvider credentialProvider;
    static AzureEnvironment azureEnvironment;

    public ServiceBusAdministrator(NamespaceMetadata namespaceMetadata) {
        credentialProvider = CredentialProvider.getInstance();
        metadata = namespaceMetadata;
        configProvider = ConfigProvider.getInstance();

        if (configProvider.getEnvironmentStage().equalsIgnoreCase(Production.name())) {
            azureEnvironment = AzureEnvironment.AZURE;
        } else {
            //Configure non Azure Prod Endpoint in Azure Config.
            azureEnvironment = new AzureEnvironment(new HashMap<String, String>() {{
                put("managementEndpointUrl", "https://management.core.windows.net/");
                put("resourceManagerEndpointUrl", configProvider.getConfigurationValue(ConfigurationKey.ResourceManagementURL));
                put("activeDirectoryEndpointUrl", configProvider.getConfigurationValue(ConfigurationKey.AuthorityHost));
            }});
        }
    }

    public void createTopic(String topic, int partitions) {
        //No-OP
        //Assuming that the topicName provided is already created.
    }

    public void deleteExistingEntities(String resourceGroup, String namespaceName) {
        log.info("Deleting existing entities");
        //NO-OP
    }
}
