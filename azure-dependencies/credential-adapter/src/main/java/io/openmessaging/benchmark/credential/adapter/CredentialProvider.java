package io.openmessaging.benchmark.credential.adapter;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

/**
 * Adapter class over Azure AppConfig to provide Configuration for running tests.
 */
public class CredentialProvider {

    private final SecretClient secretClient;
    private static final Object lockObject = new Object();
    private static CredentialProvider provider;

    private CredentialProvider() {
        String keyVaultName = System.getenv("PerfKeyVaultName");
        if(keyVaultName == null){
            throw new RuntimeException("No KeyVault specified in environment variables");
        }
        String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";

        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    public static CredentialProvider getInstance() {
        if(provider == null) {
            synchronized (lockObject) {
                provider = new CredentialProvider();
            }
        }
        return provider;
    }

    public String getCredential(String secretName){
       return secretClient.getSecret(secretName).getValue();
    }
}