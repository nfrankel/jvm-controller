package ch.frankel.kubernetes.extend;

import java.util.concurrent.Executors;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sidecar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sidecar.class);

    public static void main(String[] args) {
        LOGGER.info("*** JVM Operator v1.8 ***");
        var service = Executors.newSingleThreadExecutor();
        var client = new DefaultKubernetesClient();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        service.submit(() -> print(client));
    }

    private static void print(KubernetesClient client) {
        var factory = client.informers();
        var informer = factory.sharedIndexInformerFor(
                Pod.class, 10 * 60 * 1000);
        informer.addEventHandler(new SidecarEventHandler(client));
    }
}