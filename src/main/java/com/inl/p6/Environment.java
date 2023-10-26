package com.inl.p6;

public class Environment {

	private String userName;
	private String password;
	private String deepLynxURL;
	private String containerID;
	private String dataSourceId;
	private String apiKey;
	private String apiSecret;
	private String p6URL;
	private String projectID;

	public Environment() {}

	public Environment(String userName, String password, String deepLynxURL, String containerID, String dataSourceId, String apiKey, String apiSecret, String p6URL, String projectID) {
		this.userName = userName;
		this.password = password;
		this.deepLynxURL = deepLynxURL;
		this.containerID = containerID;
		this.dataSourceId = dataSourceId;
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.p6URL = p6URL;
		this.projectID = projectID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeepLynxURL() {
		return deepLynxURL;
	}

	public void setDeepLynxURL(String deepLynxURL) {
		this.deepLynxURL = deepLynxURL;
	}

	public String getContainerId() {
		return containerID;
	}

	public void setContainerId(String containerID) {
		this.containerID = containerID;
	}

	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public String getP6URL() {
		return p6URL;
	}

	public void setP6URL(String p6URL) {
		this.p6URL = p6URL;
	}

	public String getProjectID() {
		return projectID;
	}

	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}
}
