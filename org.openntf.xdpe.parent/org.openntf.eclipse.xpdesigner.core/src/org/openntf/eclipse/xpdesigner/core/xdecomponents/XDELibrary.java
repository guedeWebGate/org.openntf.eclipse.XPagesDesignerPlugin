package org.openntf.eclipse.xpdesigner.core.xdecomponents;

import com.ibm.xsp.library.XspLibrary;

public class XDELibrary {
	private final String className;
	private final String pluginID;
	private final String libraryID;
	private final XspLibrary library;

	public XDELibrary(String pluginID, String className, XspLibrary library) {
		this.className = className;
		this.pluginID = pluginID;
		this.libraryID = library.getLibraryId();
		this.library = library;
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

	public XspLibrary getLibrary() {
		return library;
	}

}
