package io.openmessaging.benchmark.driver.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.appconfig.adapter.NamespaceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static io.openmessaging.benchmark.appconfig.adapter.EnvironmentName.Production;

public class EventHubAdministrator {
    private static final Logger log = LoggerFactory.getLogger(EventHubAdministrator.class);

    TokenCredential sharedCSC;
    AzureProfile sharedAzureProfile;
    NamespaceMetadata metadata;
    static ConfigProvider provider;
    public EventHubsManager getManager() {
        return manager;
    }

    EventHubsManager manager;

    public EventHubAdministrator(NamespaceMetadata namespaceMetadata) {
        this.metadata = namespaceMetadata;
        provider = ConfigProvider.getInstance(Production.toString());
        sharedCSC = createClientSecretCredential();
        sharedAzureProfile = createAzureProfile(namespaceMetadata);
        manager =  EventHubsManager.configure()
                .authenticate(sharedCSC, sharedAzureProfile);
    }


    private static AzureProfile createAzureProfile(NamespaceMetadata metadata) {
        return new AzureProfile(provider.getConfigurationValue(ConfigurationKey.ApplicationTenantID),
                metadata.SubscriptionId,
                AzureEnvironment.AZURE);
    }

    private static TokenCredential createClientSecretCredential() {
        return new DefaultAzureCredentialBuilder()
                .tenantId(provider.getConfigurationValue(ConfigurationKey.ApplicationTenantID))
                .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                .build();
    }

    public void createTopic(String topic, int partitions) {
        log.info(" Topic Name: " + topic);
        manager.namespaces()
                .eventHubs()
                .define(topic)
                .withExistingNamespace(metadata.ResourceGroup, metadata.NamespaceName)
                .withPartitionCount(partitions)
                .create();
    }
}
