package org.openrosa.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;


/**
 * Servlet that handles opening of files.
 * 
 * @author daniel
 *
 */
public class FileOpenServlet extends HttpServlet{

	public static final long serialVersionUID = 111111111111113L;

	private final String KEY_FILE_CONTENTS = "FileContents";


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		response.setHeader("Cache-Control", "no-store");

		response.setContentType("text/xml; charset=utf-8"); 
		response.getOutputStream().print((String)request.getSession().getAttribute(KEY_FILE_CONTENTS));
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			if(!ServletFileUpload.isMultipartContent(request))
				return;

			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
			upload.setSizeMax(1000000);

			FileItemIterator items = upload.getItemIterator(request);
			while (items.hasNext()) {
				FileItemStream item = items.next();
				if(item.isFormField())
					continue;
				request.getSession().setAttribute(KEY_FILE_CONTENTS, IOUtils.toString(item.openStream(), "UTF-8"));
				break;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
