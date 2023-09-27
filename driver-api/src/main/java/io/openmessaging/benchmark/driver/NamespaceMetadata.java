package io.openmessaging.benchmark.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NamespaceMetadata implements Serializable {
    public String namespaceName;
    public String subscriptionId;
    public String resourceGroup;
    public String sasKeyName = "RootManageSharedAccessKey";
    @ToString.Exclude
    public String sasKeyValue;
    public String region;
}
