package io.openmessaging.benchmark.driver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.AccessRights;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static io.openmessaging.benchmark.appconfig.adapter.EnvironmentName.Production;

public class EventHubAdministrator {
    private static final Logger log = LoggerFactory.getLogger(EventHubAdministrator.class);
    static NamespaceMetadata metadata;
    static ConfigProvider configProvider;
    static CredentialProvider credentialProvider;
    static AzureEnvironment azureEnvironment;
    TokenCredential sharedCSC;
    AzureProfile sharedAzureProfile;
    EventHubsManager manager;

    public EventHubAdministrator(NamespaceMetadata namespaceMetadata) {
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

        sharedCSC = createClientSecretCredential();
        sharedAzureProfile = createAzureProfile(namespaceMetadata);
        manager = EventHubsManager.configure()
                .authenticate(sharedCSC, sharedAzureProfile);
    }

    private static AzureProfile createAzureProfile(NamespaceMetadata metadata) {
        return new AzureProfile(configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID),
                metadata.subscriptionId, azureEnvironment);
    }

    private static TokenCredential createClientSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientSecret(credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientSecret"))
                .clientId(credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientId"))
                .tenantId(configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID))
                .authorityHost(configProvider.getConfigurationValue(ConfigurationKey.AuthorityHost))
                .build();
    }

    public EventHubsManager getManager() {
        return manager;
    }

    public void createTopic(String topic, int partitions) {
        try {
            final EventHub eventHub = manager.namespaces().eventHubs().getByName(metadata.resourceGroup, metadata.namespaceName, topic);
            log.info("Reusing the existing topic as it exists - " + eventHub.name() + " with partition counts " + (long) eventHub.partitionIds().size());
        } catch (Exception e) {
            log.info(" Creating new topic with Topic Name: " + topic);
            manager.namespaces()
                    .eventHubs()
                    .define(topic)
                    .withExistingNamespace(metadata.resourceGroup, metadata.namespaceName)
                    .withPartitionCount(partitions)
                    .create();
        }
    }

    public EventHubNamespaceAuthorizationRule getAuthorizationRule() {
        final PagedIterable<EventHubNamespaceAuthorizationRule> eventHubNamespaceAuthorizationRules = manager.namespaceAuthorizationRules().listByNamespace(metadata.resourceGroup, metadata.namespaceName);
        return eventHubNamespaceAuthorizationRules.stream().filter(authRule -> authRule.rights().contains(AccessRights.MANAGE)).findFirst().orElseThrow(RuntimeException::new);
    }

    public void createConsumerGroupIfNotPresent(String topicName, String subscriptionName) {
        manager
                .consumerGroups()
                .define(subscriptionName)
                .withExistingEventHub(metadata.resourceGroup, metadata.namespaceName, topicName)
                .create();
    }
}
