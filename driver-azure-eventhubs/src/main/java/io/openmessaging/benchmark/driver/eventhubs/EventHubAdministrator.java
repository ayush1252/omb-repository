package io.openmessaging.benchmark.driver.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.appconfig.adapter.NamespaceMetadata;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static io.openmessaging.benchmark.appconfig.adapter.EnvironmentName.Production;

public class EventHubAdministrator {
    private static final Logger log = LoggerFactory.getLogger(EventHubAdministrator.class);

    TokenCredential sharedCSC;
    AzureProfile sharedAzureProfile;
    static NamespaceMetadata metadata;
    static ConfigProvider configProvider;
    static CredentialProvider credentialProvider;
    static AzureEnvironment azureEnvironment;
    public EventHubsManager getManager() {
        return manager;
    }

    EventHubsManager manager;

    public EventHubAdministrator(NamespaceMetadata namespaceMetadata) {
        credentialProvider = CredentialProvider.getInstance();
        metadata = namespaceMetadata;
        configProvider = ConfigProvider.getInstance(System.getenv("PerfBenchmarkEnvironmentName"));

        if (configProvider.getEnvironmentStage().equalsIgnoreCase(Production.name())) {
            azureEnvironment = AzureEnvironment.AZURE;
        } else {
            //Configuring DF endpoints for Development setup.
            azureEnvironment = new AzureEnvironment(new HashMap<String, String>() {{
                put("managementEndpointUrl", "https://management.core.windows.net/");
                put("resourceManagerEndpointUrl", "https://api-dogfood.resources.windows-int.net");
                put("activeDirectoryEndpointUrl", "https://login.windows-ppe.net/");
            }});
        }

        sharedCSC = createClientSecretCredential();
        sharedAzureProfile = createAzureProfile(namespaceMetadata);
        manager =  EventHubsManager.configure()
                .authenticate(sharedCSC, sharedAzureProfile);
    }


    private static AzureProfile createAzureProfile(NamespaceMetadata metadata) {
        return new AzureProfile(configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID),
                metadata.SubscriptionId, azureEnvironment);
    }

    private static TokenCredential createClientSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientSecret(credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientSecret"))
                .clientId(credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientId"))
                .tenantId(configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID))
                .authorityHost(configProvider.getConfigurationValue(ConfigurationKey.AuthorityHost))
                .build();
    }

    public void createTopic(String topic, int partitions) {
        try{
            final EventHub eventHub = manager.namespaces().eventHubs().getByName(metadata.ResourceGroup, metadata.NamespaceName, topic);
            log.info("Reusing the existing topic as it exists - " + eventHub.name() + " with partition counts " + (long) eventHub.partitionIds().size());
        } catch (Exception e){
            log.info(" Creating new topic with Topic Name: " + topic);
            manager.namespaces()
                    .eventHubs()
                    .define(topic)
                    .withExistingNamespace(metadata.ResourceGroup, metadata.NamespaceName)
                    .withPartitionCount(partitions)
                    .create();
        }
    }
}
