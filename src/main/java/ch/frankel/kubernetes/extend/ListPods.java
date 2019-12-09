package ch.frankel.kubernetes.extend;

import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListPods {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPods.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("*** JVM Operator v1.4 ***");
        var client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        print();
    }

    private static void print() {
        var api = new CoreV1Api();
        var factory = new SharedInformerFactory();
        var informer = factory.sharedIndexInformerFor(
                it -> api.listPodForAllNamespacesCall(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        it.resourceVersion,
                        null,
                        it.timeoutSeconds,
                        it.watch,
                        null),
                V1Pod.class,
                V1PodList.class);
        informer.addEventHandler(new PodLogEventHandler());
        factory.startAllRegisteredInformers();
    }
}