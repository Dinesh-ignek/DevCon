package com.custom.batch.client.extension.generator.constant;

import java.util.Map;

public class BatchConstants {
	public static final String CLASS_NAME_OBJECT_DEFINITION = "com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition";

	public static final String CLASS_NAME_OBJECT_FOLDER = "com.liferay.object.admin.rest.dto.v1_0.ObjectFolder";

	public static final String EXTERNAL_REFERENCE_CODE ="externalReferenceCode";
	
	public static final String CLIENT_EXTENSION_NAME="clientExtensionName";
	
	public static final String OBJECT_API_URL="/o/c/clientextensions/by-external-reference-code/";
	
	public static final String OBJECT_ENTRY="objectEntry";
	
	public static final String VALUES="values";
	
	public static final String BATCH="batch";
	
	public static final String CLIENT_EXTENSION_YAML="client-extension.yaml";
	
	public static final String CLIENT_EXTENSION="client-extension";
	
	public static final String FILE_BASE_64="fileBase64";
	
	public static final String NAME="name";
	
	public static final String FILE="file";
	
	public static final String DEFAULT="default";
	
	public static final String ACTIONS="actions";
	
	public static final String SYSTEM="system";
	
	
	
	public static final String CLIENT_EXTENSION_YAML_TEMPLATE = 
			"assemble:\n" +
			"  - from: batch\n" +
			"    into: batch\n\n" +
			"%s:\n" +
			"  name: %s\n" +
			"  oAuthApplicationHeadlessServer: %s-oauth-application-headless-server\n" +
			"  type: batch\n\n" +
			"%s-oauth-application-headless-server:\n" +
			"  .serviceAddress: %s\n" +
			"  .serviceScheme: %s\n" +
			"  name: %s OAuth Application Headless Server\n" +
			"  type: oAuthApplicationHeadlessServer\n";

	public static final Map<String, String> BATCH_TYPE_MAPPING = Map.of(
		"picklist", "00-picklist.batch-engine-data.json",
		"objectfolderdefinition", "01-objectfolder.batch-engine-data.json",
		"userroles", "03-user-role.batch-engine-data.json",
		"objectdefinition", "object-definition.batch-engine-data.json"
	);

	private static final Map<String, String> API_ENDPOINT_MAPPING = Map.of(
		"picklist", "/o/headless-admin-list-type/v1.0/list-type-definitions",
		"objectfolderdefinition", "/o/object-admin/v1.0/object-folders",
		"userroles", "/o/headless-admin-user/v1.0/roles",
		"objectdefinition", "/o/object-admin/v1.0/object-definitions"
	);

	private static final Map<String, String> CLASS_NAME_MAPPING = Map.of(
		"picklist", "com.liferay.headless.admin.list.type.dto.v1_0.ListTypeDefinition",
		"objectfolderdefinition", "com.liferay.object.admin.rest.dto.v1_0.ObjectFolder",
		"userroles", "com.liferay.headless.admin.user.dto.v1_0.Role",
		"objectdefinition", "com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition"
	);

	public static String getEndpoint(String type) {
		return API_ENDPOINT_MAPPING.get(type.toLowerCase());
	}

	public static String getClassName(String type) {
		return CLASS_NAME_MAPPING.get(type.toLowerCase());
	}

	public static String getBatchFileName(String type) {
		return BATCH_TYPE_MAPPING.get(type.toLowerCase());
	}

	private BatchConstants() {
	}
}
