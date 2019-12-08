package ch.frankel.kubernetes.extend;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListPods {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPods.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("*** JVM Operator v1.2 ***");
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        while (true) {
            print();
            Thread.sleep(1000);
        }
    }

    private static void print() throws ApiException {
        CoreV1Api core = new CoreV1Api();
        V1PodList pods = core.listPodForAllNamespaces(null, null, null, null, null, null, null, null);
        pods.getItems()
                .stream()
                .map(it -> "* " + it.getMetadata().getName())
                .forEach(LOGGER::info);
    }
}
