package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;

@SuppressWarnings("restriction")
public enum PluginDependencyManager {
	INSTANCE;
	private static String[] required = { "org.eclipse.ui", "org.eclipse.core.runtime" };
	private static String[] optional = { "com.ibm.commons", "com.ibm.commons.xml", "com.ibm.commons.vfs", "com.ibm.jscript", "com.ibm.designer.runtime.directory", "com.ibm.designer.runtime",
			"com.ibm.xsp.core", "com.ibm.xsp.extsn", "com.ibm.xsp.designer", "com.ibm.xsp.domino", "com.ibm.notes.java.api" };

	// not needed: , "com.ibm.xsp.rcp"

	public List<IPluginImport> buildMandatoryImports(WorkspaceBundlePluginModel model) throws CoreException {

		List<IPluginImport> plugins = new LinkedList<IPluginImport>();
		for (String req : required) {
			IPluginImport imp = model.getPluginFactory().createImport();
			imp.setId(req);
			plugins.add(imp);
		}
		for (String opt : optional) {
			IPluginImport imp = model.getPluginFactory().createImport();
			imp.setId(opt);
			imp.setOptional(true);
			plugins.add(imp);
		}
		return plugins;
	}

	public List<IPluginImport> buildImportsDiff(List<XDELibrary> libs, IPluginImport[] imports, WorkspaceBundlePluginModel model) throws CoreException {
		List<IPluginImport> plugins = new LinkedList<IPluginImport>();
		for (XDELibrary lib : libs) {
			if (notAllreadyAdded(imports, lib.getPluginID())) {
				IPluginImport imp = model.getPluginFactory().createImport();
				imp.setId(lib.getPluginID());
				plugins.add(imp);
			}
		}
		return plugins;
	}

	private boolean notAllreadyAdded(IPluginImport[] imports, String pluginID) {
		for (IPluginImport imp:imports) {
			if (imp.getId().equals(pluginID)) {
				return false;
			}
		}
		return true;
	}
}
