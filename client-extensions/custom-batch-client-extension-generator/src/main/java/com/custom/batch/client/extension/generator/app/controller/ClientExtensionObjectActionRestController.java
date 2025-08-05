package com.custom.batch.client.extension.generator.app.controller;

import com.custom.batch.client.extension.generator.BatchDTO;
import com.custom.batch.client.extension.generator.BatchDataFetcher;
import com.custom.batch.client.extension.generator.ClientExtensionObjectUpdater;
import com.custom.batch.client.extension.generator.WebClientFactory;
import com.custom.batch.client.extension.generator.ZipBuilder;
import com.custom.batch.client.extension.generator.constant.BatchConstants;
import com.liferay.client.extension.util.spring.boot.BaseRestController;
import com.liferay.petra.string.StringPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RequestMapping("/object/action")
@RestController
public class ClientExtensionObjectActionRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt, @RequestBody String json) throws IOException {
		
		JSONObject requestJson = new JSONObject(json);
		JSONObject objectEntry = requestJson.getJSONObject(BatchConstants.OBJECT_ENTRY);
		JSONObject values = objectEntry.getJSONObject(BatchConstants.VALUES);
		String extensionName = values.getString(BatchConstants.CLIENT_EXTENSION_NAME).replaceAll("\\s+", StringPool.MINUS);
		String[] batchItems = values.getString(BatchConstants.BATCH).split("\\s*,\\s*");
		String erc = objectEntry.getString(BatchConstants.EXTERNAL_REFERENCE_CODE);

		WebClient webClient = WebClientFactory.create(jwt, lxcDXPMainDomain, lxcDXPServerProtocol);

		Map<String, BatchDTO> batchDataMap = BatchDataFetcher.fetch(webClient, batchItems);
		File zipFile = ZipBuilder.build(extensionName, batchDataMap, lxcDXPMainDomain, lxcDXPServerProtocol);

		ClientExtensionObjectUpdater.updateClientExtension(
				webClient,
				erc,
				extensionName,
				zipFile,
				batchItems,
				lxcDXPMainDomain,
				lxcDXPServerProtocol
		);
		Files.deleteIfExists(zipFile.toPath());
		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private static final Log _log = LogFactory.getLog(ClientExtensionObjectActionRestController.class);
}
