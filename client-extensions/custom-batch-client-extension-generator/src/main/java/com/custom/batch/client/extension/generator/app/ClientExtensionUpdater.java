package com.custom.batch.client.extension.generator.app;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ClientExtensionUpdater {

	public static String updateClientExtension(
			WebClient webClient,
			String erc,
			String extensionName,
			File zipFile,
			String[] batchItems,
			String lxcDXPMainDomain,
			String lxcDXPServerProtocol
	) throws IOException {

		JSONObject payload = new JSONObject();
		payload.put("externalReferenceCode", erc);
		payload.put("clientExtensionName", extensionName);

		
		JSONArray batchArray = new JSONArray();
		for (String item : batchItems) {
			JSONObject batchObj = new JSONObject();
			batchObj.put("key", item);
			batchObj.put("name", item);
			batchArray.put(batchObj);
		}
		payload.put("batch", batchArray);

		
		String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(zipFile.toPath()));

		JSONObject fileJson = new JSONObject();
		fileJson.put("externalReferenceCode", erc + "-zip");
		fileJson.put("fileBase64", base64);
		fileJson.put("fileURL", "");
		fileJson.put("id", 0);
		fileJson.put("name", zipFile.getName());

	
		JSONObject scope = new JSONObject();
		scope.put("type", "Site");
		fileJson.put("scope", scope);

		payload.put("file", fileJson);

	
		String apiURL = lxcDXPServerProtocol + "://" + lxcDXPMainDomain
				+ "/o/c/clientextensions/by-external-reference-code/" + erc;

		return webClient.put()
				.uri(apiURL)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue(payload.toString())
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}
}
