package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public enum ApplicationDefinitionSupport {
	INSTANCE;

	public boolean initApplicationDefinition(Text appName, Button autoupdate, Text appPath, Combo selectedServer, Combo selectedApp) {
		IProject xpageServerProject = ServerDefinitionUtils.getXPagesServerProject(true);
		if (xpageServerProject == null) {
			return false;
		}
		String serverName = selectedServer.getText();
		if (!ServerDefinitionUtils.folderExist(xpageServerProject, serverName, false)) {
			return false;
		}
		String autoUpdateValue = autoupdate.isEnabled()?"TRUE":"FALSE";
		String appProject = selectedApp.getText();
		IFile file = xpageServerProject.getFile(serverName + "/"+appName.getText() +".xde-appmapping-properties");
		Properties props = new Properties();
		props.setProperty("SERVER_NAME", serverName);
		props.setProperty("APPNAME", appName.getText());
		props.setProperty("AUTOUPDATE", autoUpdateValue);
		props.setProperty("APPPROJECT", appProject);
		try {
			ServerDefinitionUtils.saveProperties2File(props, file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;

	}

}
