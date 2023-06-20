package io.openmessaging.benchmark.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NamespaceMetadata implements Serializable {
    public String NamespaceName;
    public String SubscriptionId;
    public String ResourceGroup;
}
