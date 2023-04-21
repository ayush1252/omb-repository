package io.openmessaging.benchmark.appconfig.adapter;

public enum ConfigurationKey {
    AuthorityHost,
    ResourceManagementURL,
    ApplicationTenantID, //Tenant ID for the resources
    FQDNSuffix,

    //Kusto Configuration
    KustoClientID,
    KustoClientSecret,
    KustoDatabaseName,
    KustoEndpoint,

    //Storage Configuration
    StorageAccountName,
    StorageContainerName
}
