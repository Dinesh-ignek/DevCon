package com.custom.batch.client.extension.generator.app;

import org.json.JSONArray;

public class BatchData {
	private final JSONArray items;
	private final String className;

	public BatchData(JSONArray items, String className) {
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
