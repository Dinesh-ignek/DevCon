package com.custom.batch.client.extension.generator.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ZipBuilder {

	private static final Map<String, String> BATCH_TYPE_MAPPING = Map.of("picklist",
			"00-picklist.batch-engine-data.json", "objectfolder", "01-objectfolder.batch-engine-data.json", "userroles",
			"03-user-role.batch-engine-data.json", "object", "object-definition.batch-engine-data.json");

	public static File build(String extensionName, Map<String, BatchData> batchDataMap, String domain, String protocol)
			throws IOException {
		File tempDir = Files.createTempDirectory("client-extension").toFile();
		try {
			File batchDir = new File(tempDir, "batch");
			if (!batchDir.exists()) {
				batchDir.mkdirs();
			}

			createClientExtensionYaml(tempDir, extensionName, domain, protocol);
			createBatchConfigFiles(batchDir, batchDataMap);

			File zipFile = new File(System.getProperty("user.dir"), extensionName + ".zip");
			try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
				addDirToZip(tempDir, zos, "");
			}

			return zipFile;
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private static void createClientExtensionYaml(File dir, String name, String domain, String protocol)
			throws IOException {
		File yaml = new File(dir, "client-extension.yaml");
		try (BufferedWriter writer = Files.newBufferedWriter(yaml.toPath(), StandardCharsets.UTF_8)) {
			writer.write(String.format("assemble:\n" + "  - from: batch\n" + "    into: batch\n\n" + "%s:\n"
					+ "  name: %s\n" + "  oAuthApplicationHeadlessServer: %s-oauth-application-headless-server\n"
					+ "  type: batch\n\n" + "%s-oauth-application-headless-server:\n" + "  .serviceAddress: %s\n"
					+ "  .serviceScheme: %s\n" + "  name: %s OAuth Application Headless Server\n"
					+ "  type: oAuthApplicationHeadlessServer\n", name, name, name, name, domain, protocol, name));
		}
	}

	private static void createBatchConfigFiles(File batchDir, Map<String, BatchData> dataMap) throws IOException {
		for (Map.Entry<String, BatchData> entry : dataMap.entrySet()) {
			String type = entry.getKey();
			String fileName = BATCH_TYPE_MAPPING.get(type);
			if (fileName == null)
				continue;

			BatchData data = entry.getValue();
			File file = new File(batchDir, fileName);

			JSONArray filteredItems = new JSONArray();

			for (int i = 0; i < data.getItems().length(); i++) {
				JSONObject item = data.getItems().getJSONObject(i);

				
				if ("com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition".equals(data.getClassName())
						&& item.optBoolean("system", false)) {
					continue;
				}
				
				JSONObject cleanedItem = new JSONObject(item.toString());
				cleanedItem.remove("actions");

				filteredItems.put(cleanedItem);
			}

			JSONObject config = new JSONObject()
					.put("configuration",
							new JSONObject().put("className", data.getClassName()).put("parameters",
									new JSONObject().put("containsHeaders", "true").put("createStrategy", "UPSERT")
											.put("onErrorFail", "false").put("updateStrategy", "UPDATE"))
									.put("taskItemDelegateName", "DEFAULT"))
					.put("items", filteredItems);

			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
				writer.write(config.toString(2));
			}
		}
	}

	private static void addDirToZip(File dir, ZipOutputStream zos, String basePath) throws IOException {
		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			String path = basePath + file.getName();
			if (file.isDirectory()) {
				zos.putNextEntry(new ZipEntry(path + "/"));
				zos.closeEntry();
				addDirToZip(file, zos, path + "/");
			} else {
				zos.putNextEntry(new ZipEntry(path));
				zos.write(Files.readAllBytes(file.toPath()));
				zos.closeEntry();
			}
		}
	}
}
