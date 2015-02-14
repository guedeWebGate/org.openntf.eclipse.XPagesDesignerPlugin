package org.openntf.eclipse.xpdesigner.core;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XSPLibrary;
import org.osgi.framework.Bundle;

import com.ibm.xsp.library.XspLibrary;

public enum XPagesComponentProvider {
	INSTANCE;

	private List<XSPLibrary> m_Libraries;

	public List<XSPLibrary> scanPlugins4XSPLibraries() {
		if (m_Libraries == null) {
			List<XSPLibrary> libraries = new LinkedList<XSPLibrary>();
			IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
			for (IPluginModelBase element : base) {
				IPluginBase pbase = element.getPluginBase();
				for (IPluginExtension pex : pbase.getExtensions()) {
					if (pex.getPoint().startsWith("com.ibm.commons.Extension")) {
						for (IPluginObject pexChild : pex.getChildren()) {
							if (pexChild instanceof IPluginElement) {
								IPluginElement pexElement = (IPluginElement) pexChild;
								if ("service".equals(pexElement.getName()) && pexElement.getAttribute("type") != null
										&& "com.ibm.xsp.Library".equalsIgnoreCase(pexElement.getAttribute("type").getValue())) {
									Bundle bdl = Platform.getBundle(pbase.getId());
									String className = pexElement.getAttribute("class").getValue();
									Class<?> cl = null;
									try {
										cl = bdl.loadClass(className);
										XspLibrary lib = (XspLibrary) cl.newInstance();
										XSPLibrary library = new XSPLibrary(pbase.getId(), pexElement.getAttribute("class").getValue(), lib);
										libraries.add(library);

									} catch (Exception ex) {
										System.out.println(className + " - not loaded..." + cl);
									}
								}
							}
						}
					}
				}

			}
			m_Libraries = libraries;
		}
		return m_Libraries;
	}

}
