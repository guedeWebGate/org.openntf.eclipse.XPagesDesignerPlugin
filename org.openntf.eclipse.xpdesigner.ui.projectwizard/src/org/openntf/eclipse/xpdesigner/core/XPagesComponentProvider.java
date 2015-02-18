package org.openntf.eclipse.xpdesigner.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XSPLibrary;
import org.osgi.framework.Bundle;

import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryManager;

public enum XPagesComponentProvider {
	INSTANCE;

	private List<XSPLibrary> m_Libraries;
	private XspRegistryManager m_Manager;

	public synchronized List<XSPLibrary> scanPlugins4XSPLibraries() {
		System.out.println("REGMAN: " + XspRegistryManager.getManager().getRegistryProviderIds().size());
		if (m_Libraries == null) {
			m_Manager = XspRegistryManager.getManager();
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
									String className = pexElement.getAttribute("class").getValue();
									Class<?> cl = null;
									try {
										Bundle bdl = Platform.getBundle(pbase.getId());

										
										try {
											bdl.start();
										} catch (Exception e) {
											e.printStackTrace();
										}
										cl = bdl.loadClass(className);
										XspLibrary lib = (XspLibrary) cl.newInstance();
										XSPLibrary library = new XSPLibrary(pbase.getId(), pexElement.getAttribute("class").getValue(), lib);
										libraries.add(library);

									} catch (Exception ex) {
										ex.printStackTrace();
										System.out.println(className + " - not loaded..." + cl);
									}
								}
							}
						}
					}
				}

			}

			m_Libraries = sortLibraries(libraries);
		}
		return m_Libraries;
	}

	private List<XSPLibrary> sortLibraries(List<XSPLibrary> libraries) {
		List<XSPLibrary> listRC = new ArrayList<XSPLibrary>();
		List<String> lstChecked = new ArrayList<String>();
		for (XSPLibrary lib : libraries) {
			Set<String> setTemp = new HashSet<String>();
			List<String> deps = getDependencies(lib, lstChecked, setTemp, libraries);
			int nPosition = 0;
			for (String depID : deps) {
				System.out.println(lib.getLib().getLibraryId() + " search for:" + depID);
				nPosition = Math.max(nPosition, getPosition(depID, listRC) + 1);
			}
			listRC.add(nPosition, lib);
		}
		return listRC;
	}

	private int getPosition(String depID, List<XSPLibrary> listRC) {
		int nCounter = -1;
		for (XSPLibrary lib : listRC) {
			nCounter++;
			if (depID.equals(lib.getLib().getLibraryId())) {
				return nCounter;
			}
		}

		return -1;
	}

	private List<String> getDependencies(XSPLibrary lib, List<String> checked, Set<String> full, List<XSPLibrary> libraries) {
		List<String> eList = Collections.emptyList();
		if (checked.contains(lib.getLib().getLibraryId())) {
			return eList;
		}
		if (lib.getLib().getDependencies() == null) {
			return eList;
		}
		List<String> lstDep = Arrays.asList(lib.getLib().getDependencies());
		checked.add(lib.getLib().getLibraryId());
		for (String depID : lstDep) {
			XSPLibrary libNew = getLibraryByID(depID, libraries);
			if (libNew == null) {
				System.out.println("!!!! " + depID + "not found!");
			} else {
				List<String> ids = getDependencies(libNew, checked, full, libraries);
				for (String nId : ids) {
					full.add(nId);
				}
			}
		}
		return new ArrayList<String>(full);

	}

	private XSPLibrary getLibraryByID(String depID, List<XSPLibrary> libraries) {
		for (XSPLibrary lib : libraries) {
			if (depID.equals(lib.getLib().getLibraryId())) {
				return lib;
			}
		}
		return null;
	}

	public FacesSharableRegistry getRegistry() {
		if (m_Libraries == null) {
			scanPlugins4XSPLibraries();
		}
		String id = "empty local registry";
		SharableRegistryImpl reg = new SharableRegistryImpl(id);
		reg.setRegistryType(FacesSharableRegistry.TYPE_APPLICATION);
		// register the project (before this the registry is in an invalid
		// state)
		reg.createProject(id);
		for (XSPLibrary library : m_Libraries) {
			XspLibrary lib = library.getLib();
			System.out.println(lib.getLibraryId());
			SimpleRegistryProvider provider = new SimpleRegistryProvider();
			provider.init(new LibraryWrapper(lib.getLibraryId(), lib));
			reg.addDepend(provider.getRegistry());
		}
		reg.refreshReferences();
		return reg;
	}
}
