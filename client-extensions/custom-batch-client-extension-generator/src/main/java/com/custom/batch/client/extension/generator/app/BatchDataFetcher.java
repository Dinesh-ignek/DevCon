package com.custom.batch.client.extension.generator.app;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;

public class BatchDataFetcher {

	private static final Map<String, String> API_ENDPOINT_MAPPING = Map.of("picklist",
			"/o/headless-admin-list-type/v1.0/list-type-definitions", "objectfolder",
			"/o/object-admin/v1.0/object-folders", "userroles", "/o/headless-admin-user/v1.0/roles", "object",
			"/o/object-admin/v1.0/object-definitions");

	private static final Map<String, String> CLASS_NAME_MAPPING = Map.of("picklist",
			"com.liferay.headless.admin.list.type.dto.v1_0.ListTypeDefinition", "objectfolder",
			"com.liferay.object.admin.rest.dto.v1_0.ObjectFolder", "userroles",
			"com.liferay.headless.admin.user.dto.v1_0.Role", "object",
			"com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition");

	public static Map<String, BatchData> fetch(WebClient webClient, String[] batchItems) {
		Map<String, BatchData> result = new LinkedHashMap<>();

		for (String type : batchItems) {
			String normalizedType = type.trim().toLowerCase();
			if (API_ENDPOINT_MAPPING.containsKey(normalizedType)) {
				JSONArray allItems = fetchAllPages(webClient, API_ENDPOINT_MAPPING.get(normalizedType));
				String className = CLASS_NAME_MAPPING.get(normalizedType);

				result.put(normalizedType, new BatchData(allItems, className));
			}
		}

		return result;
	}

	private static JSONArray fetchAllPages(WebClient webClient, String endpoint) {
		int page = 1;
		int pageSize = 20;
		int lastPage = 1;

		JSONArray allItems = new JSONArray();

		do {
			String url = endpoint + "?page=" + page + "&pageSize=" + pageSize;

			String response = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();

			JSONObject json = new JSONObject(response);
			JSONArray items = json.optJSONArray("items");
			if (items != null) {
				for (int i = 0; i < items.length(); i++) {
					allItems.put(items.getJSONObject(i));
				}
			}

			lastPage = json.optInt("lastPage", 1);
			page++;

		} while (page <= lastPage);

		return allItems;
	}
}
