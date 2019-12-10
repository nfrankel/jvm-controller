package ch.frankel.kubernetes.extend;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SidecarEventHandler implements ResourceEventHandler<V1Pod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SidecarEventHandler.class);
    private static final String SIDECAR_IMAGE_NAME = "hazelcast/hazelcast:3.12.5";
    private static final String SIDECAR_POD_NAME = "hazelcast";
    private static final String NAMESPACE = "jvmoperator";

    @Override
    public void onAdd(V1Pod pod) {
        String namespace = pod.getMetadata().getNamespace();
        if (NAMESPACE.equals(namespace) && !isSidecar(pod)) {
            try {
                String podName = pod.getMetadata().getName();
                if (!alreadyHasSidecar(pod)) {
                    createSidecar(namespace, podName);
                } else {
                    LOGGER.info("Sidecar already existing for pod " + podName);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
        // NOTHING TO DO
    }

    @Override
    public void onDelete(V1Pod pod, boolean stateUnknown) {
        CoreV1Api api = new CoreV1Api();
        try {
            if (alreadyHasSidecar(pod)) {
                String podName = pod.getMetadata().getName();
                LOGGER.info("Sidecar found for pod " + podName);
                // https://github.com/kubernetes-client/java/issues/86
                api.deleteNamespacedPod(SIDECAR_POD_NAME + "-" + podName, NAMESPACE, null, null, null, null, null, null);
            }
        } catch (ApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private boolean isSidecar(V1Pod pod) {
        Optional<Map<String, String>> labels = Optional.ofNullable(pod.getMetadata().getLabels());
        return "true".equals(labels
                .orElse(new HashMap<>())
                .getOrDefault("sidecar", "false"));
    }

    private void createSidecar(String namespace, String podName) throws ApiException {
        String name = SIDECAR_POD_NAME + "-" + podName;
        V1Container container = new V1Container()
                .name(name)
                .image(SIDECAR_IMAGE_NAME);
        V1PodSpec spec = new V1PodSpec().addContainersItem(container);
        Map<String, String> labels = new HashMap<>();
        labels.put("sidecar", "true");
        V1ObjectMeta metadata = new V1ObjectMeta()
                .name(name)
                .namespace(namespace)
                .labels(labels);
        V1Pod body = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(metadata)
                .spec(spec);
        LOGGER.info("No sidecar found for pod " + podName + ". Creating one");
        CoreV1Api api = new CoreV1Api();
        api.createNamespacedPod(namespace, body, "true", null, null);
    }

    private boolean alreadyHasSidecar(V1Pod pod) throws ApiException {
        return findSidecar(pod).isPresent();
    }

    private Optional<V1Pod> findSidecar(V1Pod pod) throws ApiException {
        CoreV1Api api = new CoreV1Api();
        V1PodList pods = api.listNamespacedPod(NAMESPACE, null, null, null, null, null, null, null, null);
        return pods.getItems()
                .stream()
                .filter(it -> it.getMetadata().getName().equals(SIDECAR_POD_NAME + "-" + pod.getMetadata().getName()))
                .findAny();
    }
}