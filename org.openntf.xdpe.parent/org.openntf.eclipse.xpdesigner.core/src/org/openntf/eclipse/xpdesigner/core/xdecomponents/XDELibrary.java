package org.openntf.eclipse.xpdesigner.core.xdecomponents;

public class XDELibrary {
	private final String className;
	private final String pluginID;
	private final String libraryID;
	
	public XDELibrary(String pluginID, String className,String libraryID) {
		this.className = className;
		this.pluginID = pluginID;
		this.libraryID = libraryID;
	}

	public String getClassName() {
		return className;
	}

	public String getPluginID() {
		return pluginID;
	}

	public String getLibraryID() {
		return libraryID;
	}
	
	
}
