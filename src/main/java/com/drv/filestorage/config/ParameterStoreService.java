package com.drv.filestorage.config;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParameterStoreService {

    private final SsmClient ssmClient;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public ParameterStoreService(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    public String getParameter(String name) {
        return cache.computeIfAbsent(name, this::fetchParameter);
    }

    public Map<String, String> getParameters(List<String> names) {
        Map<String, String> result = new HashMap<>();
        List<String> namesToFetch = new ArrayList<>();

        for (String name : names) {
            if (cache.containsKey(name)) {
                result.put(name, cache.get(name));
            } else {
                namesToFetch.add(name);
            }
        }

        if (!namesToFetch.isEmpty()) {
            Map<String, String> fetched = fetchParameters(namesToFetch);
            result.putAll(fetched);
            cache.putAll(fetched);
        }

        return result;
    }

    private String fetchParameter(String name) {
        return ssmClient.getParameter(GetParameterRequest.builder()
                .name(name)
                .withDecryption(true)
                .build()).parameter().value();
    }

    private Map<String, String> fetchParameters(List<String> names) {
        var response = ssmClient.getParameters(GetParametersRequest.builder()
                .names(names)
                .withDecryption(true)
                .build());

        Map<String, String> result = new HashMap<>();
        response.parameters().forEach(param -> result.put(param.name(), param.value()));
        return result;
    }
}
