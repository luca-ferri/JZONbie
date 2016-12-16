package com.jonnymatts.jzonbie.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.ZombieResponse;
import com.jonnymatts.jzonbie.util.Deserializer;

import java.util.List;

import static java.util.Collections.singletonMap;

public class JzonbieClient {

    private final JzonbieHttpClient httpClient;

    public JzonbieClient(JzonbieHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ZombiePriming primeZombie(ZombieRequest request, ZombieResponse response) {
        return httpClient.primeZombie(request, response);
    }

    public List<PrimedMapping> getCurrentPriming() {
        return httpClient.getCurrentPriming();
    }

    public List<ZombiePriming> getHistory() {
        return httpClient.getHistory();
    }

    public void reset() {
        httpClient.reset();
    }

    public static void main(String[] args) {
        JzonbieRequestFactory jzonbieRequestFactory = new JzonbieRequestFactory("http://localhost:8080");
        final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Deserializer deserializer = new Deserializer(objectMapper);
        JzonbieClient client = new JzonbieClient(new ApacheJzonbieHttpClient(jzonbieRequestFactory, deserializer));

        final ZombieRequest zombieRequest = new ZombieRequest();
        zombieRequest.setPath("/blah");
        zombieRequest.setMethod("POST");
        zombieRequest.setBody(singletonMap("one", 1));

        final ZombieResponse zombieResponse = new ZombieResponse();
        zombieResponse.setStatusCode(200);
        zombieResponse.setHeaders(singletonMap("Content-Type", "application/json"));
        zombieResponse.setBody(singletonMap("message", "Well done!"));

        final List<PrimedMapping> currentPriming = client.getCurrentPriming();

        final PrimedMapping mapping = currentPriming.get(0);

        mapping.getZombieRequest();

        currentPriming.clear();
    }
}
