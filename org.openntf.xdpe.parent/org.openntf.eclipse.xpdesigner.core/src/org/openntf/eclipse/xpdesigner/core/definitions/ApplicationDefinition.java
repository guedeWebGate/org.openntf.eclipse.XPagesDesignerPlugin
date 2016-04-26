package org.openntf.eclipse.xpdesigner.core.definitions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationDefinition implements IDefinition {

	private static final String APPPROJECT = "APPPROJECT";
	private static final String AUTOUPDATE = "AUTOUPDATE";
	private static final String APPNAME = "APPNAME";
	private static final String SERVER_NAME = "SERVER_NAME";
	private final Properties applicationProperties = new Properties();

	
	/* (non-Javadoc)
	 * @see org.openntf.eclipse.xpdesigner.core.definitions.IDefinition#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return applicationProperties;
	}
	
	/* (non-Javadoc)
	 * @see org.openntf.eclipse.xpdesigner.core.definitions.IDefinition#loadProperties(java.io.InputStream)
	 */
	@Override
	public void loadProperties(InputStream is) throws IOException {
		applicationProperties.load(is);
	}
	
	public String getServerName() {
		return applicationProperties.getProperty(SERVER_NAME);
	}

	public void setServerName(String serverName) {
		applicationProperties.setProperty(SERVER_NAME, serverName);
	}

	public String getAppName() {
		return applicationProperties.getProperty(APPNAME);
	}

	public void setAppName(String appName) {
		applicationProperties.setProperty(APPNAME, appName);
	}

	public String getAutoUpdate() {
		return applicationProperties.getProperty(AUTOUPDATE);
	}

	public void setAutoUpdate(String autoUpdate) {
		applicationProperties.setProperty(AUTOUPDATE, autoUpdate);
	}

	public String getAppProject() {
		return applicationProperties.getProperty(APPPROJECT);
	}

	public void setAppProject(String appProject) {
		applicationProperties.setProperty(APPPROJECT, appProject);
	}

}
