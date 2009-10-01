package org.purc.purcforms.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet that handles saving of files.
 * 
 * @author daniel
 *
 */
public class FormSaveServlet extends HttpServlet{

	public static final long serialVersionUID = 111111111111112L;
	
	private String filecontents;
	private String filename;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*try{
			filecontents = null;
			CommonsMultipartResolver multipartResover = new CommonsMultipartResolver();
			if(multipartResover.isMultipart(request)){
				MultipartHttpServletRequest multipartRequest = multipartResover.resolveMultipart(request);
				filecontents = multipartRequest.getParameter("filecontents");
				if (filecontents == null || filecontents.trim().length() == 0)
					return;
			}
			
			filename = "filename.xml";		
			if(request.getParameter("filename") != null)
				filename = request.getParameter("filename")+".xml";
		}
		catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		response.setContentType("text/xml; charset=utf-8"); 
		
		response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-store");
        
		response.getOutputStream().print(filecontents);
	}
}
