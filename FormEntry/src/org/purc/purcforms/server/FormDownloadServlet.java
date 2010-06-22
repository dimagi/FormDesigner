package org.purc.purcforms.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

		String formId = request.getParameter("formId");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if((username != null && username.equals("test")) || (password != null && password.equals("test"))){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		String xml = null;

		if(formId == null){
			xml = "<?xml version='1.0' encoding='UTF-8' ?>";
			xml += "\n<xforms>";

			xml += "\n  <xform id='1' name='Form 1' />";
			xml += "\n  <xform id='2' name='Form 2' />";
			xml += "\n  <xform id='3' name='Form 3' />";
			xml += "\n  <xform id='4' name='Form 4' />";
			xml += "\n  <xform id='5' name='Form 5' />";
			xml += "\n  <xform id='6' name='Form 6' />";
			xml += "\n  <xform id='7' name='Form 7' />";
			xml += "\n  <xform id='8' name='Form 8' />";
			xml += "\n  <xform id='9' name='Pregnancy Registration Form 9' />";
			xml += "\n  <xform id='10' name='Form 10' />";
			xml += "\n  <xform id='11' name='Form 11' />";
			xml += "\n  <xform id='12' name='Form 12' />";
			xml += "\n  <xform id='13' name='Form 13' />";
			xml += "\n  <xform id='14' name='Form 14' />";
			xml += "\n  <xform id='15' name='Form 15' />";

			xml += "\n</xforms>";
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
