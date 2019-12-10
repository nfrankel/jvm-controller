package ch.frankel.kubernetes.extend;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

public class SidecarEventHandler implements ResourceEventHandler<V1Pod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SidecarEventHandler.class);
    private static final String SIDECAR_IMAGE_NAME = "hazelcast/hazelcast:5.0-BETA-1-slim";
    private static final String SIDECAR_POD_NAME = "hazelcast";
    private static final String NAMESPACE = "jvmoperator";

    @Override
    public void onAdd(V1Pod pod) {
        var namespace = pod.getMetadata().getNamespace();
        if (NAMESPACE.equals(namespace) && !isSidecar(pod)) {
            try {
                if (!alreadyHasSidecar(pod)) {
                    createSidecar(pod);
                } else {
                    LOGGER.info("Sidecar already existing for pod {}", pod.getMetadata().getName());
                }
            } catch (ApiException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
        // NOTHING TO DO
    }

    @Override
    public void onDelete(V1Pod pod, boolean stateUnknown) {
    }

    private boolean isSidecar(V1Pod pod) {
        var labels = Optional.ofNullable(pod.getMetadata().getLabels());
        return "true".equals(labels
                .orElse(new HashMap<>())
                .getOrDefault("sidecar", "false"));
    }

    private void createSidecar(V1Pod pod) throws ApiException {
        var namespace = pod.getMetadata().getNamespace();
        var podName = pod.getMetadata().getName();
        var name = SIDECAR_POD_NAME + "-" + podName;
        var container = new V1Container()
                .name(name)
                .image(SIDECAR_IMAGE_NAME);
        var spec = new V1PodSpec().addContainersItem(container);
        var labels = new HashMap<String, String>();
        labels.put("sidecar", "true");
        var owner = new V1OwnerReference()
                .apiVersion("v1")
                .uid(pod.getMetadata().getUid())
                .name(podName)
                .kind("Pod");
        var metadata = new V1ObjectMeta()
                .name(name)
                .namespace(namespace)
                .addOwnerReferencesItem(owner)
                .labels(labels);
        var body = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(metadata)
                .spec(spec);
        LOGGER.info("No sidecar found for pod {}. Creating one", podName);
        var api = new CoreV1Api();
        api.createNamespacedPod(namespace, body, "true", null, null);
    }

    private boolean alreadyHasSidecar(V1Pod pod) throws ApiException {
        return findSidecar(pod).isPresent();
    }

    private Optional<V1Pod> findSidecar(V1Pod pod) throws ApiException {
        var api = new CoreV1Api();
        var pods = api.listNamespacedPod(NAMESPACE, null, null, null, null, null, null, null, null, null, null);
        return pods.getItems()
                .stream()
                .filter(it -> it.getMetadata().getName().equals(SIDECAR_POD_NAME + "-" + pod.getMetadata().getName()))
                .findAny();
    }
}