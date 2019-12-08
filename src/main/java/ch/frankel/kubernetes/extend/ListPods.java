package ch.frankel.kubernetes.extend;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;

public class ListPods {

    public static void main(String[] args) throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api core = new CoreV1Api();
        V1PodList pods = core.listPodForAllNamespaces(null, null, null, null, null, null, null, null);
        pods.getItems()
                .stream()
                .map(it -> "- " + it.getMetadata().getName())
                .forEach(System.out::println);
    }
}
