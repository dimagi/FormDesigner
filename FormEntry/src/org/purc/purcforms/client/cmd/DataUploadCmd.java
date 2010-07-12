package org.purc.purcforms.client.cmd;

import org.purc.purcforms.client.FormEntryConstants;
import org.purc.purcforms.client.FormEntryContext;
import org.purc.purcforms.client.listener.DataLoadListener;
import org.purc.purcforms.client.listener.DataUploadListener;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.util.Utils;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;


/**
 * 
 * @author daniel
 *
 */
public class DataUploadCmd implements DataLoadListener{

	private DataUploadListener listener;
	private int pos;
	private int len;
	
	
	public DataUploadCmd(String id, int pos, int len, DataUploadListener listener){
		this.listener = listener;
		this.pos = pos;
		this.len = len;
		
		FormEntryContext.getDatabaseManager().loadFormData(id, this);
	}
	
	public void onDataReceived(final String data){
		assert(data != null);
		
		FormUtil.dlg.setText("uploading " + pos + " of " + len);
		FormUtil.dlg.center();

		DeferredCommand.addCommand(new Command(){
			public void execute() {

				String url = FormUtil.getHostPageBaseURL();
				if(!FormEntryContext.getDataUploadUrl().contains("http"))
					url += FormEntryContext.getDataUploadUrl();
				else{
					url += FormEntryConstants.DATA_UPLOAD_URL_SUFFIX;
					url = Utils.addParameter(url, FormEntryConstants.PARAM_NAME_REDIRECT_URL, FormEntryContext.getDataUploadUrl());
				}
				
				url = Utils.urlAppendNamePassword(url, FormEntryContext.getUserName(), FormEntryContext.getPassword());

				RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,URL.encode(url));

				try{
					builder.sendRequest(data, new RequestCallback(){
						public void onResponseReceived(Request request, Response response){
							
							if(response.getStatusCode() != Response.SC_OK){
								FormUtil.displayReponseError(response);
								listener.onDataUploadFailed();
								return;
							}
							
							listener.onDataUploaded();
							//FormUtil.dlg.hide();
						}

						public void onError(Request request, Throwable exception){
							FormUtil.displayException(exception);
							listener.onDataUploadFailed();
						}
					});
				}
				catch(RequestException ex){
					FormUtil.displayException(ex);
					listener.onDataUploadFailed();
				}
			}
		});
	}
}
