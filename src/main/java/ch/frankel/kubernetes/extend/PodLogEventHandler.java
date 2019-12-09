package ch.frankel.kubernetes.extend;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodLogEventHandler implements ResourceEventHandler<V1Pod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodLogEventHandler.class);

    @Override
    public void onAdd(V1Pod pod) {
        LOGGER.info("ADDED: {}", pod.getMetadata().getName());
    }

    @Override
    public void onUpdate(V1Pod oldPod, V1Pod newPod) {
        LOGGER.info("UPDATED: {}", oldPod.getMetadata().getName());
    }

    @Override
    public void onDelete(V1Pod pod, boolean stateUnknown) {
        LOGGER.info("DELETED: {}", pod.getMetadata().getName());
    }
}