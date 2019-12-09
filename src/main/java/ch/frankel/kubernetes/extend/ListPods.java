package ch.frankel.kubernetes.extend;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

public class ListPods {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPods.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("*** JVM Operator v1.2 ***");
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        configureTimeout(client);
        print(client);
    }

    private static void print(ApiClient client) throws ApiException, IOException {
        CoreV1Api api = new CoreV1Api();
        Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, Boolean.TRUE, null, null);
        Type type = new TypeToken<Watch.Response<V1Node>>() {
        }.getType();
        try (Watch<V1Node> watch = Watch.createWatch(client, call, type)) {
            StreamSupport.stream(watch.spliterator(), false)
                    .map(it -> it.type + ": " + it.object.getMetadata().getName())
                    .forEach(LOGGER::info);
        }
    }

    private static void configureTimeout(ApiClient client) {
        OkHttpClient httpClient = client.getHttpClient();
        httpClient.setReadTimeout(0, TimeUnit.SECONDS);
        client.setHttpClient(httpClient);
    }
}