package com.neo.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cimit
 * @Date 08-01-2020
 *
 *       Utils.java
 */
public class Utils {
	public static List<String> getListParamName(String sql) {
		int currentIndex = 0;
		List<String> listParam = new ArrayList<>();
		while (sql.indexOf(":", currentIndex) != -1) {
			int startIndexKey = sql.indexOf(":", currentIndex);
			int endKey = sql.indexOf(" ", startIndexKey);

			String key = null;
			if (endKey == -1) {
				key = sql.substring(startIndexKey + 1);
			} else {
				key = sql.substring(startIndexKey + 1, endKey);
			}
			currentIndex = startIndexKey + 1;
			listParam.add(key.toUpperCase());
		}
		return listParam;
	}

	public static Map<String, String> toMap(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();

		Map<String, String> row = new LinkedHashMap<String, String>();
		int count = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < count; i++) {
				int columnNumber = i + 1;
				// use column label to get the name as it also handled SQL SELECT aliases
				String columnName;
				try {
					columnName = meta.getColumnLabel(columnNumber);
				} catch (SQLException e) {
					columnName = meta.getColumnName(columnNumber);
				}
				// use index based which should be faster
				int columnType = meta.getColumnType(columnNumber);
				if (columnType == Types.CLOB || columnType == Types.BLOB) {
					row.put(columnName, rs.getString(columnNumber));
				} else {
					row.put(columnName, rs.getString(columnNumber));
				}
			}
		}
		// rs.close();
		return row;
	}


	

	public static String sendGet(String url) throws Exception {
		StringBuilder response = new StringBuilder();
		HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();

		// optional default is GET
		httpClient.setRequestMethod("GET");
		httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");

		try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {

			String line;

			while ((line = in.readLine()) != null) {
				response.append(line);
			}
		}
		return response.toString();
	}
	
	public static String getSize(long size) {
	    long n = 1000L;
	    String s = "";
	    double kb = (size / n);
	    double mb = kb / n;
	    double gb = mb / n;
	    double tb = gb / n;
	    if (size < n) {
	      s = String.valueOf(size) + " Bytes";
	    } else if (size >= n && size < n * n) {
	      s = String.valueOf(String.format("%.2f", new Object[] { Double.valueOf(kb) })) + " KB";
	    } else if (size >= n * n && size < n * n * n) {
	      s = String.valueOf(String.format("%.2f", new Object[] { Double.valueOf(mb) })) + " MB";
	    } else if (size >= n * n * n && size < n * n * n * n) {
	      s = String.valueOf(String.format("%.2f", new Object[] { Double.valueOf(gb) })) + " GB";
	    } else if (size >= n * n * n * n) {
	      s = String.valueOf(String.format("%.2f", new Object[] { Double.valueOf(tb) })) + " TB";
	    } 
	    return s;
	  }
	  
	  public static Map<String, String> extractParameters(String res) {
	    String prefix = "", regex = "";
	    int start = 0;
	    int end = 0;
	    Pattern pattern = null;
	    Matcher m = null;
	    String beforeStart = "";
	    if (res.indexOf("<response") == -1) {
	      regex = "<*response";
	      pattern = Pattern.compile(regex);
	      m = pattern.matcher(res);
	      if (m.find()) {
	        start = m.start();
	        beforeStart = res.substring(0, start);
	        prefix = res.substring(beforeStart.lastIndexOf("<") + 1, start);
	      } 
	    } 
	    String tag = String.valueOf(prefix) + "response";
	    String extract = res.substring(res.indexOf("<" + tag), res.indexOf("</" + tag));
	    if (res.indexOf("<Parameters") == -1) {
	      regex = "<*Parameters";
	      pattern = Pattern.compile(regex);
	      m = pattern.matcher(extract);
	      if (m.find()) {
	        start = m.start();
	        end = m.end();
	        System.out.println(extract.substring(start, end));
	        beforeStart = extract.substring(0, start);
	        prefix = extract.substring(beforeStart.lastIndexOf("<") + 1, start);
	      } 
	    } 
	    tag = String.valueOf(prefix) + "Parameters";
	    start = extract.indexOf("<" + tag);
	    if (extract.indexOf(String.valueOf(tag) + " ") > -1) {
	      extract = extract.substring(extract.indexOf(">", start), extract.indexOf("</" + tag));
	    } else {
	      extract = extract.substring(start + tag.length(), extract.indexOf("</" + tag));
	    } 
	    Map<String, String> map = new HashMap<>();
	    regex = "<*Parameter";
	    pattern = Pattern.compile(regex);
	    m = pattern.matcher(extract);
	    if (m.find()) {
	      if (extract.indexOf("<Parameter") == -1) {
	        start = m.start();
	        beforeStart = extract.substring(0, start);
	        prefix = extract.substring(beforeStart.lastIndexOf("<") + 1, start);
	      } 
	      String[] result = subValue(extract, start, prefix);
	      map.put(result[0], result[1]);
	      m.find();
	      while (m.find()) {
	        start = m.start();
	        result = subValue(extract, start, prefix);
	        map.put(result[0], result[1]);
	        m.find();
	      } 
	    } 
	    return map;
	  }
	  
	  private static String[] subValue(String extract, int start, String prefix) {
	    String[] result = { "", "" };
	    int end = extract.indexOf("</" + prefix + "Parameter", start);
	    String nameAndValue = extract.substring(start, end);
	    String name = "";
	    if (nameAndValue.indexOf("<name>") > -1) {
	      name = nameAndValue.substring(nameAndValue.indexOf("<name>") + "<name>".length(), nameAndValue.indexOf("</name>"));
	    } else {
	      name = nameAndValue.substring(nameAndValue.indexOf("<" + prefix + "name>") + ("<" + prefix + "name>").length(), nameAndValue.indexOf("</" + prefix + "name>"));
	    } 
	    result[0] = name;
	    if (nameAndValue.indexOf("<" + prefix + "value/>") > 0)
	      return result; 
	    String value = "";
	    if (nameAndValue.indexOf("<value>") > -1) {
	      value = nameAndValue.substring(nameAndValue.indexOf("<value>") + "<value>".length(), nameAndValue.indexOf("</value>"));
	    } else {
	      value = nameAndValue.substring(nameAndValue.indexOf("<" + prefix + "value>") + ("<" + prefix + "value>").length(), nameAndValue.indexOf("</" + prefix + "value>"));
	    } 
	    result[1] = value;
	    return result;
	  }
	  
	  public static String extractValue(String response, String key) {
	    String tag = "<" + key + ">";
	    if (response.indexOf(tag) > 0) {
	      String tagEnd = "</" + key + ">";
	      return response.substring(response.indexOf(tag) + tag.length(), response.indexOf(tagEnd));
	    } 
	    String regex = "<*:" + key + ">";
	    Pattern pattern = Pattern.compile(regex);
	    Matcher m = pattern.matcher(response);
	    if (m.find()) {
	      int start = m.end();
	      m.find();
	      int end = m.start();
	      String a = response.substring(start, end);
	      return a.substring(0, a.indexOf("<"));
	    } 
	    return null;
	  }
	  
	  public static String convertTimeUnit(long value) {
	    if (value / Math.pow(10.0D, 9.0D) < 1.0D) {
	      if (value / Math.pow(10.0D, 6.0D) < 1.0D) {
	        if (value / Math.pow(10.0D, 3.0D) < 1.0D)
	          return String.valueOf(value) + " nanoseconds"; 
	        return String.format("%.2f microseconds", new Object[] { Double.valueOf(value / Math.pow(10.0D, 3.0D)) });
	      } 
	      return String.format("%.2f milliseconds", new Object[] { Double.valueOf(value / Math.pow(10.0D, 6.0D)) });
	    } 
	    return String.format("%.2f seconds", new Object[] { Double.valueOf(value / Math.pow(10.0D, 9.0D)) });
	  }
	  
	  public static String estimateTime(long startTime) {
	    long value = System.nanoTime() - startTime;
	    if (value / Math.pow(10.0D, 9.0D) < 1.0D) {
	      if (value / Math.pow(10.0D, 6.0D) < 1.0D) {
	        if (value / Math.pow(10.0D, 3.0D) < 1.0D)
	          return String.valueOf(value) + " nanoseconds"; 
	        return String.valueOf(TimeUnit.MICROSECONDS.convert(value, TimeUnit.NANOSECONDS)) + " microseconds";
	      } 
	      return String.valueOf(TimeUnit.MILLISECONDS.convert(value, TimeUnit.NANOSECONDS)) + " milliseconds";
	    } 
	    return String.valueOf(TimeUnit.SECONDS.convert(value, TimeUnit.NANOSECONDS)) + " seconds";
	  }
	  
	  public static Map<String, String> extractLogSOA(String content) {
	    if (content.indexOf("Parameters") > 0) {
	      Map<String, String> map = new HashMap<>();
	      String namespace = null;
	      String regex = "<*:Parameters";
	      Pattern pattern = Pattern.compile(regex);
	      Matcher m = pattern.matcher(content);
	      if (m.find()) {
	        int start = m.start();
	        String beforeStart = content.substring(0, start);
	        namespace = content.substring(beforeStart.lastIndexOf("<") + 1, start + 1);
	      } else {
	        return null;
	      } 
	      String contentParams = content.substring(content.indexOf("<" + namespace + "Parameters>") + ("<" + namespace + "Parameters>").length(), content.indexOf("</" + namespace + "Parameters>"));
	      regex = "<" + namespace + "Parameter>";
	      pattern = Pattern.compile(regex);
	      m = pattern.matcher(contentParams);
	      while (m.find()) {
	        int start = m.start();
	        String nameAndValue = contentParams.substring(start, contentParams.indexOf("</" + namespace + "Parameter>", start));
	        String name = "";
	        if (nameAndValue.indexOf("<name>") > -1) {
	          name = nameAndValue.substring(nameAndValue.indexOf("<name>") + "<name>".length(), nameAndValue.indexOf("</name>"));
	        } else {
	          name = nameAndValue.substring(nameAndValue.indexOf("<" + namespace + "name>") + ("<" + namespace + "name>").length(), nameAndValue.indexOf("</" + namespace + "name>"));
	        } 
	        if (nameAndValue.indexOf("value/>") > 0) {
	          map.put(name, "");
	          continue;
	        } 
	        String value = "";
	        if (nameAndValue.indexOf("<value>") > -1) {
	          value = nameAndValue.substring(nameAndValue.indexOf("<value>") + "<value>".length(), nameAndValue.indexOf("</value>"));
	        } else {
	          value = nameAndValue.substring(nameAndValue.indexOf("<" + namespace + "value>") + ("<" + namespace + "value>").length(), nameAndValue.indexOf("</" + namespace + "value>"));
	        } 
	        map.put(name, value);
	      } 
	      return map;
	    } 
	    return null;
	  }

}