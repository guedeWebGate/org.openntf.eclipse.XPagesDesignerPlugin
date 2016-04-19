package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public enum ApplicationDefinitionSupport {
	INSTANCE;

	public boolean initApplicationDefinition(String appName, String autoupdate, String appPath, String selectedServer, String appProject) {
		IProject xpageServerProject = ServerDefinitionUtils.getXPagesServerProject(true);
		if (xpageServerProject == null) {
			return false;
		}
		if (!ServerDefinitionUtils.folderExist(xpageServerProject, selectedServer, false)) {
			return false;
		}
		IFile file = xpageServerProject.getFile(selectedServer + "/"+appName +".xde-appmapping-properties");
		Properties props = new Properties();
		props.setProperty("SERVER_NAME", selectedServer);
		props.setProperty("APPNAME", appName);
		props.setProperty("AUTOUPDATE", autoupdate);
		props.setProperty("APPPROJECT", appProject);
		try {
			ServerDefinitionUtils.saveProperties2File(props, file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;

	}

}
