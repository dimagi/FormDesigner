package org.purc.purcforms.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.purc.purcforms.server.util.RedirectUtil;
import org.purc.purcforms.server.util.XFormsUtil;



/**
 * 
 * @author daniel
 *
 */
public class FormDownloadServlet extends HttpServlet{

	public static final long serialVersionUID = 123456789;


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		

		//FileListUtil.listFiles("FormEntry/", this.getServletContext().getRealPath("") + "FormEntry/");
		//FileListUtil.listFiles("FormDesigner/", "/Projects/PurcForms/FormDesigner/war/FormDesigner/");
		
		String formId = request.getParameter("formId");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String redirectUrl = request.getParameter("redirectUrl");

		if(redirectUrl != null)
			RedirectUtil.doGet(formId, username, password, redirectUrl, response);
		else{
			if((username != null && username.equals("test")) || (password != null && password.equals("test"))){
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			String xml = null;

			if(formId == null){
				xml = XFormsUtil.getFormList2();
			}
			else
				xml = XFormsUtil.getSampleForm();


			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", -1);
			response.setHeader("Cache-Control", "no-store");

			response.setContentType("text/xml; charset=utf-8"); 
			response.getWriter().print(xml);
		}
	}
}
