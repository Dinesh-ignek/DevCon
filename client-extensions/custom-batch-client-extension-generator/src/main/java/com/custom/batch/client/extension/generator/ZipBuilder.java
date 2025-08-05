package com.custom.batch.client.extension.generator;

import com.custom.batch.client.extension.generator.constant.BatchConstants;

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

	public static File build(String extensionName, Map<String, BatchDTO> batchDataMap, String domain, String protocol)
			throws IOException {
		File tempDir = Files.createTempDirectory(BatchConstants.CLIENT_EXTENSION).toFile();
		try {
			File batchDir = new File(tempDir, BatchConstants.BATCH);
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

		File yaml = new File(dir, BatchConstants.CLIENT_EXTENSION_YAML);

		try (BufferedWriter writer = Files.newBufferedWriter(yaml.toPath(), StandardCharsets.UTF_8)) {
			writer.write(String.format(
				BatchConstants.CLIENT_EXTENSION_YAML_TEMPLATE,
				name, name, name, name, domain, protocol, name
			));
		}
	}


	private static void createBatchConfigFiles(File batchDir, Map<String, BatchDTO> dataMap) throws IOException {
		for (Map.Entry<String, BatchDTO> entry : dataMap.entrySet()) {
			String type = entry.getKey();
			String fileName = BatchConstants.getBatchFileName(type);

			if (fileName == null)
				continue;

			BatchDTO data = entry.getValue();
			File file = new File(batchDir, fileName);
			JSONArray filteredItems = new JSONArray();

			for (int i = 0; i < data.getItems().length(); i++) {
				JSONObject item = data.getItems().getJSONObject(i);
				String className = data.getClassName();

				if (BatchConstants.CLASS_NAME_OBJECT_DEFINITION.equals(className)
						&& item.optBoolean(BatchConstants.SYSTEM, false)) {
					continue;
				}

				if (BatchConstants.CLASS_NAME_OBJECT_FOLDER.equals(className)
						&& BatchConstants.DEFAULT.equals(item.optString(BatchConstants.EXTERNAL_REFERENCE_CODE))) {
					continue;
				}

				JSONObject cleanedItem = new JSONObject(item.toString());
				cleanedItem.remove(BatchConstants.ACTIONS);

				filteredItems.put(cleanedItem);
			}

			if (filteredItems.length() > 0) {
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
