package io.openmessaging.benchmark.driver.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;

import java.util.Properties;

public class EventHubAdministrator {

    private final Properties eventHubProperties;
    TokenCredential sharedCSC;
    AzureProfile sharedAzureProfile;

    public EventHubsManager getManager() {
        return manager;
    }

    EventHubsManager manager;

    public EventHubAdministrator(Properties eventHubProperties) {
        this.eventHubProperties = eventHubProperties;
        sharedCSC = createClientSecretCredential(eventHubProperties);
        sharedAzureProfile = createAzureProfile(eventHubProperties);
        manager =  EventHubsManager.configure()
                .authenticate(sharedCSC, sharedAzureProfile);
    }


    private static AzureProfile createAzureProfile(Properties commonProperties) {
        return new AzureProfile(commonProperties.getProperty("tenant.id"), commonProperties.getProperty("subscription.id"),
                AzureEnvironment.AZURE);
    }

    private static TokenCredential createClientSecretCredential(Properties properties) {
        String tenantId = properties.getProperty("tenant.id");

        return new DefaultAzureCredentialBuilder()
                .tenantId(tenantId)
                .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                .build();
    }

    public void createTopic(String topic, int partitions) {
        System.out.println(" Topic Req: " + topic);
        manager.namespaces()
                .eventHubs()
                .define(topic)
                .withExistingNamespace(eventHubProperties.getProperty("resource.group"), eventHubProperties.getProperty("namespace"))
                .withPartitionCount(partitions)
                .create();
    }
}
