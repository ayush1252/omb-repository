package io.openmessaging.benchmark.appconfig.adapter;

public class NamespaceMetadata {
    public String NamespaceName;
    public String SubscriptionId;
    public String ResourceGroup;

    @Override
    public String toString() {
        return "NamespaceMetadata{" +
                "NamespaceName='" + NamespaceName + '\'' +
                ", SubscriptionId='" + SubscriptionId + '\'' +
                ", ResourceGroup='" + ResourceGroup + '\'' +
                '}';
    }
}
