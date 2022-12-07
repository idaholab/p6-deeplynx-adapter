package com.example.p6;

import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

public class DeepLynxService {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );
	private Environment env;
	private String containerID;
	private String dataSourceID;
	private String apiKey = null;
	private String apiSecret = null;
	private String token = null;

	public String getDataSourceID() {
		return dataSourceID;
	}

	public void setDataSourceID(String dataSourceID) {
		this.dataSourceID = dataSourceID;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeepLynxService(Environment env) {
		this.env = env;
		this.apiKey = env.getApiKey();
		this.apiSecret = env.getApiSecret();
		this.containerID = env.getContainerId();
	}

	/**
	 * If the data source already exists and has existing imports return the latest import date.
	 * If the data source does not exist, create it and return a default date (Date(0)).
	 * Else return null.
	 */
	public Date checkDataSource() {
		System.out.println("container Id: " + this.containerID);
		String path = env.getDeepLynxURL() + "/containers/" + this.containerID + "/import/datasources";
		JSONObject obj = this.makeHTTPRequest(path, "GET", null, null);
		if (obj == null) {
			return null;
		}
		JSONArray valArr = obj.getJSONArray("value");
		boolean dataSourceExists = false;
		for (int i = 0; i < valArr.length(); i++) {
			String dataSourceName = ((JSONObject) valArr.get(i)).getString("name");
			if (this.env.getDataSourceName().equals(dataSourceName)) {
				LOGGER.log(Level.INFO, "Data source exists");
				String dataSourceID = ((JSONObject) valArr.get(i)).getString("id");
				this.setDataSourceID(dataSourceID);
				dataSourceExists = true;
				// Check if imports have already occurred for this data source
				path = env.getDeepLynxURL() + "/containers/" + this.containerID + "/import/datasources/" + this.getDataSourceID() + "/imports";
				obj = this.makeHTTPRequest(path, "GET", null, null);
				valArr = obj.getJSONArray("value");
				if (valArr.length() > 0) {
					JSONObject latestImport = valArr.getJSONObject(0);
					Date importDate = null;
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
						importDate = dateFormat.parse(latestImport.getString("created_at"));
					} catch (JSONException e) {
						e.printStackTrace();
						LOGGER.log(Level.SEVERE, e.toString(), e);
					} catch (ParseException e) {
						e.printStackTrace();
						LOGGER.log(Level.SEVERE, e.toString(), e);
					}
					// TODO: Deep Lynx requires a fix to return the correct date in above API call
					System.out.println("Dev. Import date: " + importDate);
					return importDate;
				} else {
					// Data source has been created but does not have any imports
					return new Date(0);
				}
			}
		}

		if (!dataSourceExists) {
			// Create new data source
			String body = "{\"adapter_type\":\"standard\", \"active\": true, \"name\": \"" + env.getDataSourceName() + "\"}";
			obj = this.makeHTTPRequest(path, "POST", body, null);
			boolean isError = obj.getBoolean("isError");
			if (isError) {
				return null;
			} else {
				JSONObject val = obj.getJSONObject("value");
				String id = val.getString("id");
				this.setDataSourceID(id);
				return new Date(0);
			}
		}

		return null;
	}

	public void authenticate() {
		String path = env.getDeepLynxURL() + "/oauth/token";

		System.out.println(this.apiKey);
		System.out.println(this.apiSecret);
		System.out.println(this.containerID);

		// supply api keys and expiry via hashmap
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("x-api-key", this.apiKey);
		headers.put("x-api-secret", this.apiSecret);
		headers.put("x-api-expiry", "12h");

		String token = this.makeGETRequest(path, "GET", null, headers);
		this.setToken(token);
	}

	public void createManualImport(File importFile) {
		String path = env.getDeepLynxURL() + "/containers/" + this.containerID + "/import/datasources/" + this.getDataSourceID() + "/imports";
		JSONObject obj = this.makeHTTPFileRequest(path, importFile);
		boolean isError = obj.getBoolean("isError");
		if (isError) {
			LOGGER.log(Level.SEVERE, "Error with manual import call");
		} else {
			LOGGER.log(Level.INFO, "Successful manual import to Deep Lynx");
		}
	}

	public JSONObject makeHTTPRequest(String path, String method, String body, HashMap<String, String> headers) {
		URL url;
		HttpURLConnection con;
		try {
			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(method);

			// add authentication header if present
			if (this.token != null) {
				con.setRequestProperty("Authorization", "Bearer " + this.getToken());
			}

			// add user-defined headers following key value pairs
			if (headers != null && headers.size() > 0) {
				for (String i : headers.keySet()) {
					con.setRequestProperty(i, headers.get(i));
				}
			}

			if (method.equals("POST")) {
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				con.setRequestProperty("Accept", "application/json");

				con.connect();
				try(OutputStream os = con.getOutputStream()) {
				    byte[] input = body.getBytes(StandardCharsets.UTF_8);
				    os.write(input, 0, input.length);
				}
			}

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			LOGGER.log(Level.INFO, content.toString());
			JSONObject obj = new JSONObject(content.toString());
			return obj;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

	public String makeGETRequest(String path, String method, String body, HashMap<String, String> headers) {
		URL url;
		HttpURLConnection con;
		try {
			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(method);

			// add authentication header if present
			if (this.token != null) {
				con.setRequestProperty("Authorization", "Bearer " + this.getToken());
			}

			// add user-defined headers following key value pairs
			for (String i : headers.keySet()) {
				con.setRequestProperty(i, headers.get(i));
			}

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			String returnContent = content.toString();
			returnContent = returnContent.replace("\"", "");
			return returnContent;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

	public JSONObject makeHTTPFileRequest(String path, File myFile) {
		URL url;
		HttpURLConnection con;
		try {
			String CRLF = "\r\n";
			String charset = "UTF-8";

			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			String boundary = UUID.randomUUID().toString();
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			// add authentication header if present
			if (token != null) {
				con.setRequestProperty("Authorization", "Bearer " + getToken());
			}

			OutputStream output = con.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			// https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
			// Send text file.
	    writer.append("--" + boundary).append(CRLF);
	    writer.append("Content-Disposition: form-data; name=\"myFile\"; filename=\"" + myFile.getName() + "\"").append(CRLF);
			// Text file itself must be saved in this charset!
			writer.append("Content-Type: application/json; charset=" + charset).append(CRLF);
	    writer.append(CRLF).flush();
	    Files.copy(myFile.toPath(), output);
			// Important before continuing with writer!
	    output.flush();
			// CRLF is important! It indicates end of boundary.
	    writer.append(CRLF).flush();
	    // End of multipart/form-data.
	    writer.append("--" + boundary + "--").append(CRLF).flush();

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			LOGGER.log(Level.INFO, content.toString());
			JSONObject obj = new JSONObject(content.toString());
			return obj;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

}
