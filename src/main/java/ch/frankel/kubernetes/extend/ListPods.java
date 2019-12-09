package ch.frankel.kubernetes.extend;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListPods {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPods.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("*** JVM Operator v1.3 ***");
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        print();
    }

    private static void print() {
        CoreV1Api api = new CoreV1Api();
        SharedInformerFactory factory = new SharedInformerFactory();
        SharedIndexInformer<V1Pod> informer = factory.sharedIndexInformerFor(
                it -> api.listPodForAllNamespacesCall(
                        null,
                        null,
                        null,
                        null,
                        null,
                        it.resourceVersion,
                        it.timeoutSeconds,
                        it.watch,
                        null,
                        null),
                V1Pod.class,
                V1PodList.class);
        informer.addEventHandler(new PodLogEventHandler());
        factory.startAllRegisteredInformers();
    }
}