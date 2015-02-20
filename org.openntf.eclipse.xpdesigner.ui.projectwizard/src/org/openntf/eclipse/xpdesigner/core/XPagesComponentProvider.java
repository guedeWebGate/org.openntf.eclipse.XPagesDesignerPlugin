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
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XDELibrary;
import org.osgi.framework.Bundle;

import com.ibm.xsp.library.LibraryWrapper;
import com.ibm.xsp.library.XspLibrary;
import com.ibm.xsp.registry.FacesComponentDefinition;
import com.ibm.xsp.registry.FacesSharableRegistry;
import com.ibm.xsp.registry.SharableRegistryImpl;
import com.ibm.xsp.registry.config.SimpleRegistryProvider;
import com.ibm.xsp.registry.config.XspRegistryManager;

public enum XPagesComponentProvider {
	INSTANCE;

	private List<XDELibrary> m_Libraries;
	private XspRegistryManager m_Manager;

	public synchronized List<XDELibrary> scanPlugins4XSPLibraries() {
		System.out.println("REGMAN: " + XspRegistryManager.getManager().getRegistryProviderIds().size());
		if (m_Libraries == null) {
			m_Manager = XspRegistryManager.getManager();
			List<XDELibrary> libraries = new LinkedList<XDELibrary>();
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
										XDELibrary library = new XDELibrary(pbase.getId(), pexElement.getAttribute("class").getValue(), lib);
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

	private List<XDELibrary> sortLibraries(List<XDELibrary> libraries) {
		List<XDELibrary> listRC = new ArrayList<XDELibrary>();
		for (XDELibrary lib : libraries) {
			Set<String> setTemp = new HashSet<String>();
			Set<String> deps = getDependencies(lib, setTemp, libraries);
			int nPosition = 0;
			for (String depID : deps) {
				System.out.println(lib.getLib().getLibraryId() + " search for:" + depID);
				nPosition = Math.max(nPosition, getPosition(depID, listRC) + 1);
			}
			listRC.add(nPosition, lib);
		}
		return listRC;
	}

	private int getPosition(String depID, List<XDELibrary> listRC) {
		int nCounter = -1;
		for (XDELibrary lib : listRC) {
			nCounter++;
			if (depID.equals(lib.getLib().getLibraryId())) {
				return nCounter;
			}
		}

		return -1;
	}

	private Set<String> getDependencies(XDELibrary lib, Set<String> full, List<XDELibrary> libraries) {
		Set<String> eList = Collections.emptySet();
		if (lib.getLib().getDependencies() == null) {
			return eList;
		}
		List<String> lstDep = Arrays.asList(lib.getLib().getDependencies());
		for (String depID : lstDep) {
			XDELibrary libNew = getLibraryByID(depID, libraries);
			if (libNew == null) {
				System.out.println("!!!! " + depID + "not found!");

			} else {
				if (!full.contains(libNew.getLib().getLibraryId())) {
					full.add(libNew.getLib().getLibraryId());
					Set<String> ids = getDependencies(libNew, full, libraries);
					full.addAll(ids);
				}
			}
		}
		return full;

	}

	private XDELibrary getLibraryByID(String depID, List<XDELibrary> libraries) {
		for (XDELibrary lib : libraries) {
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
		for (XDELibrary library : m_Libraries) {
			XspLibrary lib = library.getLib();
			SimpleRegistryProvider provider = new SimpleRegistryProvider();
			provider.init(new LibraryWrapper(lib.getLibraryId(), lib));
			checkManager( provider);
			reg.addDepend(provider.getRegistry());
		}
		reg.refreshReferences();
		return reg;
	}

	private void checkManager(SimpleRegistryProvider provider) {
		for (String id: m_Manager.getRegistryProviderIds()) {
			if (id.equals(provider.getId())) {
				return;
			}
		}
		System.out.println("add "+ provider.getId() +" to XSPManager");
		m_Manager.setRegistryProvider(provider.getId(), provider);
	}
	
	public void crawlRegistry() {
		FacesSharableRegistry reg = getRegistry();
		System.out.println(reg.findDefs().size());
		for (FacesComponentDefinition def: reg.findComponentDefs()) {
			System.out.println(def.getFirstDefaultPrefix() +":"+def.getTagName() + " "+ def.getNamespaceUri());
			System.out.println(def.getExtension("designer-extension"));
			
		}
	}
}
