package ch.frankel.kubernetes.extend;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sidecar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sidecar.class);

    public static void main(String[] args) {
        LOGGER.info("*** JVM Operator v1.8 ***");
        ExecutorService service = Executors.newSingleThreadExecutor();
        DefaultKubernetesClient client = new DefaultKubernetesClient();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        service.submit(() -> print(client));
    }

    private static void print(KubernetesClient client) {
        SharedInformerFactory factory = client.informers();
        SharedIndexInformer<Pod> informer = factory.sharedIndexInformerFor(
                Pod.class, PodList.class, 10 * 60 * 1000);
        informer.addEventHandler(new SidecarEventHandler(client));
    }
}