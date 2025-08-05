package com.custom.batch.client.extension.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.custom.batch.client.extension.generator.constant.BatchConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ClientExtensionObjectUpdater {

	public static String updateClientExtension(WebClient webClient, String erc, String extensionName, File zipFile,
			String[] batchItems, String lxcDXPMainDomain, String lxcDXPServerProtocol) throws IOException {

		JSONObject payload = new JSONObject();
		payload.put(BatchConstants.EXTERNAL_REFERENCE_CODE, erc);
		payload.put(BatchConstants.CLIENT_EXTENSION_NAME, extensionName);
		String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(zipFile.toPath()));

		JSONObject fileJson = new JSONObject();

		fileJson.put(BatchConstants.FILE_BASE_64, base64);
		fileJson.put(BatchConstants.NAME, zipFile.getName());

		payload.put(BatchConstants.FILE, fileJson);

		String apiURL = lxcDXPServerProtocol + "://" + lxcDXPMainDomain + BatchConstants.OBJECT_API_URL + erc;

		return webClient.put().uri(apiURL).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue(payload.toString()).retrieve().bodyToMono(String.class).block();
	}

	private static final Log _log = LogFactory.getLog(ClientExtensionObjectUpdater.class);

}
