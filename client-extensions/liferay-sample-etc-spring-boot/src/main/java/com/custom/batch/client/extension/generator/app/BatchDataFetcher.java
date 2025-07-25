package com.custom.batch.client.extension.generator.app;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

public class BatchDataFetcher {

    private static final Map<String, String> API_ENDPOINT_MAPPING = Map.of(
            "picklist", "/o/headless-admin-list-type/v1.0/list-type-definitions",
            "objectfolder", "/o/object-admin/v1.0/object-folders",
            "userroles", "/o/headless-admin-user/v1.0/roles",
            "object", "/o/object-admin/v1.0/object-definitions"
    );

    private static final Map<String, String> CLASS_NAME_MAPPING = Map.of(
            "picklist", "com.liferay.headless.admin.list.type.dto.v1_0.ListTypeDefinition",
            "objectfolder", "com.liferay.object.admin.rest.dto.v1_0.ObjectFolder",
            "userroles", "com.liferay.headless.admin.user.dto.v1_0.Role",
            "object", "com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition"
    );

    public static Map<String, BatchData> fetch(WebClient webClient, String[] batchItems) {
        Map<String, BatchData> batchDataMap = new LinkedHashMap<>();

        for (String type : batchItems) {
            String normalizedType = type.trim().toLowerCase();
            if (API_ENDPOINT_MAPPING.containsKey(normalizedType)) {
                BatchData data = fetchItemsForType(webClient, normalizedType);
                if (data != null) {
                    batchDataMap.put(normalizedType, data);
                }
            }
        }

        return batchDataMap;
    }

    private static BatchData fetchItemsForType(WebClient webClient, String type) {
        return webClient.get()
                .uri(API_ENDPOINT_MAPPING.get(type))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray items = jsonObject.optJSONArray("items");
                    return new BatchData(items, CLASS_NAME_MAPPING.get(type));
                })
                .block();
    }
}
