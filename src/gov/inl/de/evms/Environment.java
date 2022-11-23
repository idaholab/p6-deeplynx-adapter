package gov.inl.de.evms;

public class Environment {
	
	private String userName;
	private String password;
	private String deepLynxURL;
	private String containerName;
	private String dataSourceName;
	private String apiKey;
	private String apiSecret;
	private String p6URL;
	private String projectID;
	private int timer;
	
	public Environment() {}

	public Environment(String userName, String password, String deepLynxURL, String containerName, String dataSourceName, String apiKey, String apiSecret, String p6URL, String projectID, int timer) {
		this.userName = userName;
		this.password = password;
		this.deepLynxURL = deepLynxURL;
		this.containerName = containerName;
		this.dataSourceName = dataSourceName;
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.p6URL = p6URL;
		this.projectID = projectID;
		this.timer = timer;
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

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
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

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

}
