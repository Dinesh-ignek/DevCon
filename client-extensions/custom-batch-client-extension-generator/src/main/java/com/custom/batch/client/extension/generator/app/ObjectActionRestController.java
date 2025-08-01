package com.custom.batch.client.extension.generator.app;

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
public class ObjectActionRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt, @RequestBody String json) throws IOException {
		
		JSONObject requestJson = new JSONObject(json);
		JSONObject objectEntry = requestJson.getJSONObject("objectEntry");
		JSONObject values = objectEntry.getJSONObject("values");
		String extensionName = values.getString("clientExtensionName").replaceAll("\\s+", StringPool.MINUS);
		String[] batchItems = values.getString("batch").split("\\s*,\\s*");
		String erc = objectEntry.getString("externalReferenceCode");

		WebClient webClient = WebClientFactory.create(jwt, lxcDXPMainDomain, lxcDXPServerProtocol);

		Map<String, BatchData> batchDataMap = BatchDataFetcher.fetch(webClient, batchItems);
		File zipFile = ZipBuilder.build(extensionName, batchDataMap, lxcDXPMainDomain, lxcDXPServerProtocol);

		ClientExtensionUpdater.updateClientExtension(
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

	private static final Log _log = LogFactory.getLog(ObjectActionRestController.class);
}
