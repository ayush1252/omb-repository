package io.openmessaging.benchmark.driver.kafka;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.AccessController;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import com.microsoft.aad.msal4j.*;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

public class CustomAuthenticateCallbackHandler implements AuthenticateCallbackHandler {

    final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private String authority;
    private String appId;
    private String appSecret;
    private volatile ConfidentialClientApplication aadClient;
    private ClientCredentialParameters aadParameters;

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        String bootstrapServer = Arrays.asList(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).get(0).toString();
        bootstrapServer = bootstrapServer.replaceAll("\\[|\\]", "");
        URI uri = URI.create("https://" + bootstrapServer);
        String sbUri = uri.getScheme() + "://" + uri.getHost();
        this.aadParameters =
                ClientCredentialParameters.builder(Collections.singleton(sbUri + "/.default"))
                        .build();

        ConfigProvider configProvider = ConfigProvider.getInstance();
        CredentialProvider credentialProvider = CredentialProvider.getInstance();
        this.authority =configProvider.getConfigurationValue(ConfigurationKey.AuthorityHost)+
                configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID) + "/";
        this.appId = credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientId");
        this.appSecret = credentialProvider.getCredential(configProvider.getEnvironmentStage() + "AAD" + "ClientSecret");
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                try {
                    OAuthBearerToken token = getOAuthBearerToken();
                    OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                    oauthCallback.token(token);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    OAuthBearerToken getOAuthBearerToken() throws MalformedURLException, InterruptedException, ExecutionException, TimeoutException
    {
        if (this.aadClient == null) {
            synchronized(this) {
                if (this.aadClient == null) {
                    IClientCredential credential = ClientCredentialFactory.createFromSecret(this.appSecret);
                    this.aadClient = ConfidentialClientApplication.builder(this.appId, credential)
                            .authority(this.authority)
                            .build();
                }
            }
        }

        IAuthenticationResult authResult = this.aadClient.acquireToken(this.aadParameters).get();
        System.out.println("TOKEN ACQUIRED");
        return new OAuthBearerTokenImp(authResult.accessToken(), authResult.expiresOnDate());
    }

    public void close() throws KafkaException {
        // NOOP
    }
}