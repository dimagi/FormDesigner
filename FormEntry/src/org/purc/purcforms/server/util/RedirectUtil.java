package org.purc.purcforms.server.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author daniel
 *
 */
public class RedirectUtil {

	public static void doGet(String formId, String username, String password, String url, HttpServletResponse response) throws IOException {
		URL u = new URL(url);
		HttpURLConnection connection = (HttpURLConnection)u.openConnection();
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setDoInput(true);
		connection.setRequestMethod("GET");

		// write connection to file
		int status = connection.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK){
			response.getOutputStream().write(IOUtils.toByteArray(connection.getInputStream()));

			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", -1);
			response.setHeader("Cache-Control", "no-store");

			response.setContentType("text/xml; charset=utf-8"); 
			response.setStatus(HttpServletResponse.SC_OK);
		}
		else
			response.setStatus(status);
	}

	
	public static void doPost(String username, String password, String url, HttpServletRequest request, HttpServletResponse response) throws IOException {

		URL u = new URL(url);
		HttpURLConnection connection = (HttpURLConnection)u.openConnection();
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
		writer.write(IOUtils.toString(request.getInputStream(),"UTF-8"));
		writer.close();  

		int status = connection.getResponseCode();
		if(status == HttpServletResponse.SC_OK || status == HttpServletResponse.SC_CREATED)
			status = HttpServletResponse.SC_OK;
		
		response.setStatus(status);
	}
}
