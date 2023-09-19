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
    public String SASKeyName = "RootManageSharedAccessKey";
    public String SASKeyValue;
    public String Region;

    @Override
    public String toString() {
        return "NamespaceMetadata{" +
                "NamespaceName='" + NamespaceName + '\'' +
                ", SubscriptionId='" + SubscriptionId + '\'' +
                ", ResourceGroup='" + ResourceGroup + '\'' +
                ", SASKeyName='" + SASKeyName + '\'' +
                ", Region='" + Region + '\'' +
                '}'; //Not display SASKeyValue secret in any logging statement.
    }
}
