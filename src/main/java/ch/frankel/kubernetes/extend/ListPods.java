package ch.frankel.kubernetes.extend;

import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListPods {

    private static Logger LOGGER = LoggerFactory.getLogger(ListPods.class);

    public static void main(String[] args) throws Exception {
        var client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        var core = new CoreV1Api();
        var pods = core.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
        pods.getItems()
                .stream()
                .map(it -> it.getMetadata().getName())
                .forEach(LOGGER::info);
    }
}