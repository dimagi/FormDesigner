package org.purc.purcforms.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;*/


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
		/*try{
			CommonsMultipartResolver multipartResover = new CommonsMultipartResolver();
			if(multipartResover.isMultipart(request)){
				MultipartHttpServletRequest multipartRequest = multipartResover.resolveMultipart(request);
				MultipartFile uploadedFile = multipartRequest.getFile("filecontents");
				if (uploadedFile != null && !uploadedFile.isEmpty()) 
					request.getSession().setAttribute(KEY_FILE_CONTENTS,IOUtils.toString(uploadedFile.getInputStream()));
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
}
