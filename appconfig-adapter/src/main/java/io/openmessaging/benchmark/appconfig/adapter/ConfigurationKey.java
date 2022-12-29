package io.openmessaging.benchmark.appconfig.adapter;

public enum ConfigurationKey {
    ApplicationTenantID,
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
