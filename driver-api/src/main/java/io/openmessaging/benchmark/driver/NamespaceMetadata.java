package io.openmessaging.benchmark.driver;

import lombok.Builder;

import java.io.Serializable;

@Builder
public class NamespaceMetadata implements Serializable {
    public String NamespaceName;
    public String SubscriptionId;
    public String ResourceGroup;
}
