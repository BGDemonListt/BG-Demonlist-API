package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.common.InternalServerErrorException;
import com.bgdl.bgdl.services.GdRequestService;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Component
public class GdRequestServiceImpl implements GdRequestService {

    private static final String HOST = "www.boomlings.com";
    private static final String BASE_URL = "https://" + HOST + "/database";

    private static final Map<String, String> DEFAULT_PARAMS = Map.of(
            "secret", "Wmfd2893gb7",
            "gameVersion", "22",
            "binaryVersion", "45"
    );

    private final RestClient restClient;

    public GdRequestServiceImpl() {
        this.restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .baseUrl(BASE_URL)
                .defaultHeader("Host", HOST)
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .defaultHeader("User-Agent", "")
                .defaultHeader("Cookie", "gd=1;")
                .build();
    }

    @Override
    public String getLevelById(String levelId) {
        return gdRequest("getGJLevels21", Map.of("str", levelId, "type", "0"));
    }

    private Map<String, String> parseGdParams(Map<String, String> params) {
        Map<String, String> merged = new HashMap<>(params);
        merged.putAll(DEFAULT_PARAMS);
        return merged;
    }

    private String gdRequest(String target, Map<String, String> params) {
        if (target == null || target.isBlank()) {
            throw new InternalServerErrorException("No target provided in gd request.");
        }

        Map<String, String> requestParams = parseGdParams(params);

        StringBuilder formBody = new StringBuilder();
        requestParams.forEach((k, v) -> {
            if (!formBody.isEmpty()) formBody.append("&");
            formBody.append(k).append("=").append(v);
        });

        return restClient.post()
                .uri(BASE_URL + "/" + target + ".php")
                .body(formBody.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .body(String.class);
    }
}
