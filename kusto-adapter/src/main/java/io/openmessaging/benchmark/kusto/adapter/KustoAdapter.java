package io.openmessaging.benchmark.kusto.adapter;

import java.net.URI;
import java.util.concurrent.*;

import com.microsoft.azure.kusto.data.auth.ConnectionStringBuilder;
import com.microsoft.azure.kusto.ingest.IngestClient;
import com.microsoft.azure.kusto.ingest.IngestClientFactory;
import com.microsoft.azure.kusto.ingest.IngestionProperties;
import com.microsoft.azure.kusto.ingest.IngestionMapping.IngestionMappingKind;
import com.microsoft.azure.kusto.ingest.IngestionProperties.IngestionReportLevel;
import com.microsoft.azure.kusto.ingest.IngestionProperties.IngestionReportMethod;
import com.microsoft.azure.kusto.ingest.result.IngestionResult;
import com.microsoft.azure.kusto.ingest.result.IngestionStatus;
import com.microsoft.azure.kusto.ingest.result.OperationStatus;
import com.microsoft.azure.kusto.ingest.source.FileSourceInfo;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.appconfig.adapter.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to publish data in Kusto Cluster
 */
public class KustoAdapter {

    private static final Logger log = LoggerFactory.getLogger(KustoAdapter.class);

    static ExecutorService service = Executors.newFixedThreadPool(3);
    static CountDownLatch latch = new CountDownLatch(3);
    private final ConfigProvider configProvider;

    public String endpoint;
    public String database;
    public final IngestClient ingestionClient;

    public KustoAdapter(String endpoint, String database) throws Exception {
        this.endpoint = endpoint;
        this.database = database;
        configProvider = ConfigProvider.getInstance(EnvironmentName.Production.toString());
        ingestionClient = getIngestionClient(endpoint);
    }

    private IngestClient getIngestionClient(String endpoint) throws Exception {
        String ingestionEndpoint = "https://ingest-" + URI.create(endpoint).getHost();
        ConnectionStringBuilder csb = ConnectionStringBuilder.createWithAadApplicationCredentials(ingestionEndpoint,
                configProvider.getConfigurationValue(ConfigurationKey.KustoClientID),
                configProvider.getConfigurationValue(ConfigurationKey.KustoClientSecret),
                configProvider.getConfigurationValue(ConfigurationKey.ApplicationTenantID));

        return IngestClientFactory.createClient(csb);
    }

    public void uploadDataToKustoCluster(String fileNamePrefix) throws InterruptedException {
        service.execute(ingestFile(database, fileNamePrefix + "-details.json", "PerformanceRunDetails"));
        service.execute(ingestFile(database, fileNamePrefix + "-snapshot.json", "PerformanceRunIndividualSnapshots"));
        service.execute(ingestFile(database, fileNamePrefix + "-aggregate.json", "PerformanceRunAggregates"));

        try{
            latch.await(10, TimeUnit.MINUTES);
        } catch (InterruptedException e){
           log.error("Caught Interrupted Exception while awaiting ingestion completion. Check Kusto Logs for more details" + e.getMessage());
        }
    }


    /**
     * queues ingestion to Azure Data Explorer and waits for it to complete or fail
     *
     * @param database name of the kusto database
     * @param fileName file to read the data from
     * @param tableName table to publish the data in
     * @throws InterruptedException
     * @return
     */
    private Runnable ingestFile(String database, String fileName, String tableName) throws InterruptedException {
        FileSourceInfo fileSourceInfo = new FileSourceInfo(fileName,1000000);

        IngestionProperties ingestionProperties = new IngestionProperties(database, tableName);
        ingestionProperties.setDataFormat(IngestionProperties.DataFormat.MULTIJSON);
        ingestionProperties.setIngestionMapping(tableName + "_mapping", IngestionMappingKind.JSON);
        ingestionProperties.setReportLevel(IngestionReportLevel.FAILURES_AND_SUCCESSES);
        ingestionProperties.setReportMethod(IngestionReportMethod.QUEUE_AND_TABLE);

        log.info("Trying to Ingest Data " + fileName + " into table " + tableName);
        return new WorkerThread(fileSourceInfo, ingestionProperties);
    }

    public class WorkerThread implements Runnable {

        private final FileSourceInfo fileSourceInfo;
        private final IngestionProperties ingestionProperties;

        public WorkerThread(FileSourceInfo fileSourceInfo, IngestionProperties ingestionProperties) {
            this.fileSourceInfo = fileSourceInfo;
            this.ingestionProperties = ingestionProperties;
        }

        @Override
        public void run() {
            IngestionResult result = null;
            try {
                result = ingestionClient.ingestFromFile(fileSourceInfo, ingestionProperties);
            } catch (Exception e) {
               log.error("Failed to initiate ingestion: " ,e);
                latch.countDown();
                Thread.currentThread().interrupt();
            }
            try {
                IngestionStatus status = result.getIngestionStatusCollection().get(0);
                while (status.status == OperationStatus.Pending) {
                    Thread.sleep(5000);
                    status = result.getIngestionStatusCollection().get(0);
                }
                log.info("Ingestion completed for " + fileSourceInfo.getFilePath());
                log.info("Final status: " + status.status);
                latch.countDown();
            } catch (Exception e) {
                log.error("Failed to get ingestion status: ", e);
                latch.countDown();
                Thread.currentThread().interrupt();
            }
        }
    }
}