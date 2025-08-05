package com.custom.batch.client.extension.generator;

import org.json.JSONArray;

public class BatchDTO {
	private final JSONArray items;
	private final String className;

	public BatchDTO(JSONArray items, String className) {
		this.items = items;
		this.className = className;
	}

	public JSONArray getItems() {
		return items;
	}

	public String getClassName() {
		return className;
	}
}
