package com.custom.batch.client.extension.generator;

import com.custom.batch.client.extension.generator.constant.BatchConstants;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;

public class BatchDataFetcher {

	public static Map<String, BatchDTO> fetch(WebClient webClient, String[] batchItems) {
		Map<String, BatchDTO> result = new LinkedHashMap<>();

		for (String type : batchItems) {
			String normalizedType = type.trim().toLowerCase();
			String endpoint = BatchConstants.getEndpoint(normalizedType);
			String className = BatchConstants.getClassName(normalizedType);

			if (endpoint != null && className != null) {
				JSONArray allItems = fetchAllPages(webClient, endpoint);
				result.put(normalizedType, new BatchDTO(allItems, className));
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
