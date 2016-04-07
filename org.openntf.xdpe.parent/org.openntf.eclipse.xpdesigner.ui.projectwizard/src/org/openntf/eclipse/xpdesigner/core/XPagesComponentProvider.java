package org.openntf.eclipse.xpdesigner.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.TargetBundle;
import org.openntf.eclipse.xpdesigner.core.loaders.TargetBundleClassLoader;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEComponentElement;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;
import org.osgi.framework.Bundle;

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
			m_Manager = XspRegistryManager.getManager();
			List<XDELibrary> libraries = new LinkedList<XDELibrary>();
			IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
			ITargetDefinition activeTargetDefinition = TargetPlatformBuilder.INSTANCE.getActiveTargetDefinition();
			Activator.getDefault().log("Activate Target Definition: " + activeTargetDefinition == null ? " not found!" : activeTargetDefinition.getName());
			for (IPluginModelBase element : base) {
				IPluginBase pbase = element.getPluginBase();

				for (IPluginExtension pex : pbase.getExtensions()) {
					if (pex.getPoint().startsWith("com.ibm.commons.Extension")) {
						for (IPluginObject pexChild : pex.getChildren()) {
							if (pexChild instanceof IPluginElement) {
								IPluginElement pexElement = (IPluginElement) pexChild;
								if ("service".equals(pexElement.getName()) && pexElement.getAttribute("type") != null
										&& "com.ibm.xsp.Library".equalsIgnoreCase(pexElement.getAttribute("type").getValue())) {
									Activator.getDefault().log("Reading Plugin: " + pbase.getId() + " - " + pbase.getVersion() + " / " + pbase.isValid());
									String className = pexElement.getAttribute("class").getValue();
									Activator.getDefault().log("---> class is " + className);
									Class<?> cl = null;

									try {
										Bundle bdl = Platform.getBundle(pbase.getId());
										if (bdl != null) {
											try {
												bdl.start();
											} catch (Exception e) {
												e.printStackTrace();
											}
											cl = bdl.loadClass(className);
										} else {
											Activator.getDefault().log("No Bundle");
											cl = findClassfromTargetDefinition(pbase, activeTargetDefinition, className);
										}
										Object lib = cl.newInstance();
										XDELibrary library = new XDELibrary(pbase.getId(), pexElement.getAttribute("class").getValue(), (XspLibrary) lib);
										libraries.add(library);
										Activator.getDefault().log("---> " + lib.getClass().getName() + " loaded!");

									} catch (Exception ex) {
										Activator.getDefault().logException(ex);
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

	private Class<?> findClassfromTargetDefinition(IPluginBase pbase, ITargetDefinition activeTargetDefinition, String className) {
		Class<?> cl = null;
		Set<String> dependecies = buildDependencies(pbase.getPluginModel(), pbase.getId());
		List<TargetBundle> tBundles = new ArrayList<TargetBundle>();
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		for (TargetBundle bundle : activeTargetDefinition.getBundles()) {
			String bundleId = bundle.getBundleInfo().getSymbolicName();
			if (dependecies.contains(bundleId)) {
				tBundles.add(bundle);
			}
		}
		if (!tBundles.isEmpty()) {
			Activator.getDefault().log("Loading" + pbase.getId() + " with " + tBundles.size() + " bundles");
			Set<URI> allBundles = new HashSet<URI>();
			for (TargetBundle tbundle : tBundles) {
				allBundles.add(tbundle.getBundleInfo().getLocation());
			}
			try {
				TargetBundleClassLoader targetBundleCL = new TargetBundleClassLoader(new ArrayList<URI>(allBundles), Thread.currentThread().getContextClassLoader());
				cl = targetBundleCL.loadClass(className);
				Activator.getDefault().log(className + " ---> " + cl);
			} catch (ClassNotFoundException e) {
				Activator.getDefault().logException(e);
			} catch (MalformedURLException e) {
				Activator.getDefault().logException(e);
			}
		} else {
			Activator.getDefault().log(pbase.getId() + " not found!");

		}
		return cl;
	}

	private Set<String> buildDependencies(IPluginModelBase model, String id) {
		Set<String> depend = new HashSet<String>();
		depend.add(id);
		addDependencies(model.getBundleDescription().getRequiredBundles(), depend);
		return depend;
	}

	private void addDependencies(BundleSpecification[] bundlesSpecs, Set<String> depend) {
		if (bundlesSpecs == null) {
			return;
		}
		for (BundleSpecification spec : bundlesSpecs) {
			String name = spec.getName();
			if (!depend.contains(name) && !spec.isOptional()) {
				depend.add(name);
			}
		}
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
				// System.out.println("!!!! " + depID + "not found!");

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

		scanPlugins4XSPLibraries();
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
}
