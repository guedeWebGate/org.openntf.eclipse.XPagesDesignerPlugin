package org.openntf.eclipse.xpdesigner.core.target;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import org.openntf.eclipse.xpdesigner.core.CoreActivator;
import org.openntf.eclipse.xpdesigner.core.TargetPlatformBuilder;
import org.openntf.eclipse.xpdesigner.core.loaders.TargetBundleClassLoader;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;
import org.osgi.framework.Bundle;

import com.ibm.xsp.library.XspLibrary;

public enum XDELibraryScanner {
	INSTANCE;
	public List<XDELibrary> loadLibraries() throws MalformedURLException {
		List<XDELibrary> libraries = new LinkedList<XDELibrary>();
		IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
		ITargetDefinition activeTargetDefinition = TargetPlatformBuilder.INSTANCE.getActiveTargetDefinition();
		CoreActivator.getDefault().log("Activate Target Definition: " + activeTargetDefinition == null ? " not found!" : activeTargetDefinition.getName());
		for (IPluginModelBase element : base) {
			IPluginBase pbase = element.getPluginBase();

			for (IPluginExtension pex : pbase.getExtensions()) {
				if (pex.getPoint().startsWith("com.ibm.commons.Extension")) {
					for (IPluginObject pexChild : pex.getChildren()) {
						if (pexChild instanceof IPluginElement) {
							IPluginElement pexElement = (IPluginElement) pexChild;
							if ("service".equals(pexElement.getName()) && pexElement.getAttribute("type") != null
									&& "com.ibm.xsp.Library".equalsIgnoreCase(pexElement.getAttribute("type").getValue())) {
								CoreActivator.getDefault().log("Reading Plugin: " + pbase.getId() + " - " + pbase.getVersion() + " / " + pbase.isValid());
								String className = pexElement.getAttribute("class").getValue();
								CoreActivator.getDefault().log("---> class is " + className);
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
										CoreActivator.getDefault().log("No Bundle");
										cl = findClassfromTargetDefinition(pbase, activeTargetDefinition, className);
									}
									Object lib = cl.newInstance();
									XDELibrary library = new XDELibrary(pbase.getId(), pexElement.getAttribute("class").getValue(), ((XspLibrary) lib));
									libraries.add(library);
									CoreActivator.getDefault().log("---> " + lib.getClass().getName() + " loaded!");

								} catch (Exception ex) {
									CoreActivator.getDefault().logException(ex);
								}
							}
						}
					}
				}
			}

		}

		return libraries;
	}

	private Class<?> findClassfromTargetDefinition(IPluginBase pbase, ITargetDefinition activeTargetDefinition, String className) {
		Class<?> cl = null;
		Set<String> dependecies = buildDependencies(pbase.getPluginModel(), pbase.getId());
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		List<TargetBundle> tBundles = collectTargetBundles(activeTargetDefinition, dependecies);
		if (!tBundles.isEmpty()) {
			CoreActivator.getDefault().log("Loading " + pbase.getId() + " with " + tBundles.size() + " bundles");
			List<URI> allBundles = buildURIListFromTargetBundles(tBundles);
			CoreActivator.getDefault().log("All bundles has " + allBundles.size() + " entries.");
			try {
				TargetBundleClassLoader targetBundleCL = new TargetBundleClassLoader(allBundles, Thread.currentThread().getContextClassLoader(), pbase.getId());
				cl = targetBundleCL.loadClass(className);
				CoreActivator.getDefault().log(className + " ---> " + cl);
			} catch (ClassNotFoundException e) {
				CoreActivator.getDefault().logException(e);
			} catch (MalformedURLException e) {
				CoreActivator.getDefault().logException(e);
			}
		} else {
			CoreActivator.getDefault().log(pbase.getId() + " not found!");

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

	private List<TargetBundle> collectTargetBundles(ITargetDefinition activeTargetDefinition, Set<String> dependecies) {
		List<TargetBundle> tBundles = new ArrayList<TargetBundle>();
		for (TargetBundle bundle : activeTargetDefinition.getBundles()) {
			String bundleId = bundle.getBundleInfo().getSymbolicName();
			if (dependecies.contains(bundleId)) {
				tBundles.add(bundle);
			}
		}
		return tBundles;
	}

	private List<URI> buildURIListFromTargetBundles(List<TargetBundle> tBundles) {
		List<URI> allBundles = new ArrayList<URI>();
		for (TargetBundle tbundle : tBundles) {
			allBundles.add(tbundle.getBundleInfo().getLocation());
		}
		return allBundles;
	}

}
