package org.openntf.eclipse.xpdesigner.core;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteTemplateEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.eclipse.xpdesigner.core.target.XDELibraryScanner;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.ExtensionCreatorFactory;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEComponentElement;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;

import com.ibm.commons.util.StringUtil;
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
	private Map<String, List<FacesComponentDefinition>> m_ComponentsByCategory;

	public synchronized List<XDELibrary> scanPlugins4XSPLibraries() {
		if (m_Libraries == null) {
			try {
				loadLibraries();
			} catch (Exception ex) {
				CoreActivator.getDefault().logException(ex);
			}
		}
		return m_Libraries;
	}

	private void loadLibraries() throws MalformedURLException {
		m_Manager = XspRegistryManager.getManager();

		m_Libraries = sortLibraries(XDELibraryScanner.INSTANCE.loadLibraries());
	}

	private List<XDELibrary> sortLibraries(List<XDELibrary> libraries) {
		List<XDELibrary> listRC = new ArrayList<XDELibrary>();
		for (XDELibrary lib : libraries) {
			Set<String> setTemp = new HashSet<String>();
			Set<String> deps = getDependencies(lib, setTemp, libraries);
			int nPosition = 0;
			for (String depID : deps) {
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
			if (depID.equals(lib.getLibraryID())) {
				return nCounter;
			}
		}

		return -1;
	}

	private Set<String> getDependencies(XDELibrary lib, Set<String> full, List<XDELibrary> libraries) {
		Set<String> eList = Collections.emptySet();
		XspLibrary xspLib = lib.getLibrary();

		if (xspLib.getDependencies() == null) {
			return eList;
		}
		List<String> lstDep = Arrays.asList(xspLib.getDependencies());
		for (String depID : lstDep) {
			XDELibrary libNew = getLibraryByID(depID, libraries);
			if (libNew == null) {
				// System.out.println("!!!! " + depID + "not found!");

			} else {
				if (!full.contains(libNew.getLibraryID())) {
					full.add(libNew.getLibraryID());
					Set<String> ids = getDependencies(libNew, full, libraries);
					full.addAll(ids);
				}
			}
		}
		return full;

	}

	private XDELibrary getLibraryByID(String depID, List<XDELibrary> libraries) {
		for (XDELibrary lib : libraries) {
			if (depID.equals(lib.getLibraryID())) {
				return lib;
			}
		}
		return null;
	}

	public FacesSharableRegistry getRegistry() {

		scanPlugins4XSPLibraries();
		String id = "empty local registry";
		SharableRegistryImpl reg = new SharableRegistryImpl(id);
		reg.setRegistryType(FacesSharableRegistry.TYPE_APPLICATION);
		// register the project (before this the registry is in an invalid
		// state)
		reg.createProject(id);
		for (XDELibrary library : m_Libraries) {
			XspLibrary lib = library.getLibrary();
			SimpleRegistryProvider provider = new SimpleRegistryProvider();
			provider.init(new LibraryWrapper(lib.getLibraryId(), lib));
			checkManager(provider);
			reg.addDepend(provider.getRegistry());
		}
		reg.refreshReferences();
		return reg;
	}

	private void checkManager(SimpleRegistryProvider provider) {
		for (String id : m_Manager.getRegistryProviderIds()) {
			if (id.equals(provider.getId())) {
				return;
			}
		}
		// System.out.println("add " + provider.getId() + " to XSPManager");
		m_Manager.setRegistryProvider(provider.getId(), provider);
	}

	public synchronized void checkComponentTreeAndBuild() {
		if (m_ComponentsByCategory == null) {
			m_ComponentsByCategory = new TreeMap<String, List<FacesComponentDefinition>>();
			FacesSharableRegistry reg = getRegistry();
			for (FacesComponentDefinition def : reg.findComponentDefs()) {
				if (def.isTag()) {
					XDEComponentElement compElement = (XDEComponentElement) def.getExtension("designerComponent");
					String cat = compElement.getCateogry();
					if (StringUtil.isEmpty(cat)) {
						cat = "no category";
					}
					List<FacesComponentDefinition> list;
					if (!m_ComponentsByCategory.containsKey(cat)) {
						// System.out.println("New cat generated: " + cat);
						list = new LinkedList<FacesComponentDefinition>();
						list.add(def);
						m_ComponentsByCategory.put(cat, list);
					} else {
						list = m_ComponentsByCategory.get(cat);
						list.add(def);
					}
				}
			}
		}
	}

	public Map<String, List<FacesComponentDefinition>> getComponentMap() {
		checkComponentTreeAndBuild();
		return m_ComponentsByCategory;
	}

	public PaletteRoot buildPaletteRoot() throws MalformedURLException {
		PaletteRoot root = new PaletteRoot();
		Map<String, List<FacesComponentDefinition>> componentMap = getComponentMap();
		for (String key : componentMap.keySet()) {
			PaletteDrawer drawer = new PaletteDrawer(key);
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
			drawer.setDrawerType(PaletteTemplateEntry.PALETTE_TYPE_TEMPLATE);
			root.add(drawer);
			List<FacesComponentDefinition> components = componentMap.get(key);
			for (FacesComponentDefinition component : components) {
				XDEComponentElement element = (XDEComponentElement) ExtensionCreatorFactory.COMPONENT.getExtensionFromNode(component);
				ImageDescriptor descrLarge = null;
				ImageDescriptor descrSmall = null;
				try {
					if (element.getIcon() != null && !StringUtil.isEmpty(element.getIcon().getLargIcon())) {
						descrLarge = ImageDescriptor.createFromURL(element.getIcon().getLargeIconURL());
					}
					if (element.getIcon() != null && !StringUtil.isEmpty(element.getIcon().getSmallIcon())) {
						descrSmall = ImageDescriptor.createFromURL(element.getIcon().getSmallIconURL());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				PaletteEntry entry = new PaletteTemplateEntry(element.getDisplayName(), element.getDescription(), component, descrSmall, descrLarge);
				drawer.acceptsType(entry);
				drawer.add(entry);
			}
		}
		return root;
	}

}
