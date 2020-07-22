package com.neo.cohgw.bs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author cimit
 * @Date 18-01-2020
 *
 *       HttpClient.java
 */
public class HttpClient {
	private static int timeOut = 120000;

	/**
	 * 
	 * @param request
	 * @param urlAPI
	 * @return
	 */
	public static String sendPost(String request, String urlAPI) {
		String v = "";
		StringBuilder response = new StringBuilder();
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlAPI);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeOut);

			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Length", String.valueOf(request.length()));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpConn.setRequestProperty("Accept", "");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(request.getBytes());
			out.flush();
			out.close();

			if (httpConn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				String value = null;
				while ((value = in.readLine()) != null) {
					response.append(value);
				}
				in.close();
				v = response.toString();
			} else {
				v = "ERROR_CODE:" + httpConn.getResponseCode();
			}
		} catch (Exception e) {
			v = "EXCEPTION:" + e.toString();
		} finally {
			if (httpConn != null) {
				try {
					httpConn.disconnect();
				} catch (Exception e) {
				}
			}
		}
		return v;
	}

	public static String sendPost(String request, String urlAPI, int timeout) {
		String v = "";
		StringBuilder response = new StringBuilder();
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlAPI);
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeout);

			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Length", String.valueOf(request.length()));
			httpConn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			httpConn.setRequestProperty("Accept", "");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(request.getBytes());
			out.flush();
			out.close();

			if (httpConn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				String value = null;
				while ((value = in.readLine()) != null) {
					response.append(value);
				}
				in.close();
				v = response.toString();
			} else {
				v = "ERROR_CODE:" + httpConn.getResponseCode();
			}
		} catch (Exception e) {
			v = "EXCEPTION:" + e.toString();
		} finally {
			if (httpConn != null) {
				try {
					httpConn.disconnect();
				} catch (Exception e) {
				}
			}
		}
		return v;
	}

	public static String sendJson(String request, String urlApi) throws IOException {
		String v = null;
		StringBuilder response = new StringBuilder();
		URL url = null;
		HttpURLConnection httpConn = null;
		
			url = new URL(urlApi);
			URLConnection urlConnection =  url.openConnection();
			urlConnection.setConnectTimeout(timeOut);
			urlConnection.setReadTimeout(timeOut);
			httpConn = (HttpURLConnection)urlConnection;

			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Length", String.valueOf(request.length()));
			httpConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			httpConn.setRequestProperty("Accept", "application/json");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);

			OutputStream out = httpConn.getOutputStream();
			out.write(request.getBytes());
			out.flush();
			out.close();

			if (httpConn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				String value = null;
				while ((value = in.readLine()) != null) {
					response.append(value);
				}
				isr.close();
				in.close();
				v = response.toString();
			} else {
				v = "Error Code:" + httpConn.getResponseCode();
			}
		return v;
	}

	public static String sendGet(String url) throws Exception {
		StringBuilder response = new StringBuilder();
		HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
		} catch (Exception e) {
			return "";
		} finally {
			try {
				httpClient.disconnect();
			} catch (Exception e) {
			}
		}
		return response.toString();
	}
}
