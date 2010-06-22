package org.purc.purcforms.server.util;

import java.io.File;


/**
 * Utility class for listing names of files in a directory.
 * Used when creating the HTML 5 off-line cache manifest to have names of the GWT
 * generated files especially because these names keep changing often on compilation.
 * 
 * @author daniel
 *
 */
public class FileListUtil {

	public static void listFiles(String prefix, String folderName){

		File folder = new File(folderName);
		if(folder == null)
			return;

		for(File file : folder.listFiles())
			System.out.println(prefix + file.getName());
	}
}
