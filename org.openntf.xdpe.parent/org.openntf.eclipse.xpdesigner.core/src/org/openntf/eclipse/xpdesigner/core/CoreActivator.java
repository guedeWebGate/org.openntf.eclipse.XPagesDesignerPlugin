package org.openntf.eclipse.xpdesigner.core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openntf.eclipse.xpdesigner.core.loaders.TargetBundleClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.packageadmin.PackageAdmin;

public class CoreActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.openntf.eclipse.xpdesigner.core";

	private static final String[] XSP_CL_IDS = { "org.eclipse.core.jobs", "org.eclipse.equinox.common", "org.eclipse.core.resources", PLUGIN_ID, "com.ibm.xsp.core", "com.ibm.xsp.designer" };

	private static CoreActivator coreActivator;

	private ClassLoader customClassLoader;
	private BundleContext context;
	private MessageConsoleStream consoleLog;
	private String tempDir;
	@Override
	public void start(BundleContext arg0) throws Exception {
		coreActivator = this;
		context = arg0;
		MessageConsole console = findConsole("XDPE -Console Core");
		consoleLog = console.newMessageStream();
		tempDir = getTempSubDir();
		//bundleInstall(arg0);
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		consoleLog.close();
		coreActivator = null;
		context = null;

	}

	public static CoreActivator getDefault() {
		return coreActivator;
	}
	
	public BundleContext getBundleContext() {
		return context;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public void log(String message) {
		consoleLog.println(message);
	}

	public void logException(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		log(sw.toString());
	}
	
	private String getTempSubDir() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		String subDir = "XDPE_" + sdf.format(new Date());
		String path = System.getProperty("java.io.tmpdir") + File.separator + subDir;
		File tempDirFile = new File(path);
		tempDirFile.mkdirs();
		return path;
	}

	
	public String getNewTempSubDir(String subDir) {
		String path = tempDir + File.separator + subDir;
		File tempDirFile = new File(path);
		tempDirFile.mkdirs();
		return path;
	}

	public synchronized ClassLoader getXSPClassLoader() throws MalformedURLException {
		if (customClassLoader == null) {
			customClassLoader = buildBaseClassLoader(Arrays.asList(XSP_CL_IDS));
		}
		return customClassLoader;
	}


	public synchronized ClassLoader buildBaseClassLoader(List<String> pluginIds) throws MalformedURLException {
		log("Start building TargetBundleClassLoader: " + new Date());
		IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
		ITargetDefinition activeTargetDefinition = TargetPlatformBuilder.INSTANCE.getActiveTargetDefinition();
		Set<String> allDependencies = new HashSet<String>();
		for (IPluginModelBase element : base) {
			IPluginBase pbase = element.getPluginBase();
			if (pluginIds.contains(pbase.getId())) {
				log("Loading Dependencies for " + pbase.getId());
				Set<String> dependecies = buildDependencies(pbase.getPluginModel(), pbase.getId());
				allDependencies.addAll(dependecies);
			}
		}
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		List<TargetBundle> tBundles = collectTargetBundles(activeTargetDefinition, allDependencies);
		List<URI> allBundles = buildURIListFromTargetBundles(tBundles);
		log("All bundles has " + allBundles.size() + " entries.");
		TargetBundleClassLoader targetBundleCL = new TargetBundleClassLoader(allBundles, this.getClass().getClassLoader(), CoreActivator.PLUGIN_ID + "-mainCL");
		return targetBundleCL;
	}

	private List<URI> buildURIListFromTargetBundles(List<TargetBundle> tBundles) {
		List<URI> allBundles = new ArrayList<URI>();
		for (TargetBundle tbundle : tBundles) {
			allBundles.add(tbundle.getBundleInfo().getLocation());
		}
		return allBundles;
	}
	private Set<String> buildDependencies(IPluginModelBase model, String id) {
		Set<String> depend = new HashSet<String>();
		depend.add(id);
		addDependencies(model.getBundleDescription().getRequiredBundles(), depend);
		return depend;
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
	
	private void bundleInstall(BundleContext context) {
		
		List<String> pluginIds = Arrays.asList("com.ibm.designer.lib.acf",
		"com.ibm.designer.lib.jsf",
		"com.ibm.pvc.servlet",
		"com.ibm.xsp.core");
		//List<String> pluginIds = Arrays.asList("com.ibm.xsp.core", "com.ibm.xsp.designer", "com.ibm.pvc.servlet", "com.ibm.xsp.extsn", "com.ibm.designer.lib.jsf", "com.ibm.jscript",
		//		"com.ibm.designer.lib.acf", "com.ibm.designer.runtime.directory", "com.ibm.notes.java.init", "com.ibm.notes.java.api", "com.ibm.designer.runtime", "com.ibm.commons.jdbc");
		log("Start bundleInstaller: " + new Date());
		// IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
		ITargetDefinition activeTargetDefinition = getActiveTargetDefinition();
		if (activeTargetDefinition == null) {
			log("TargetPlatform is not defined!");
			return;
		}
		Set<String> allDependencies = new HashSet<String>();
		/*
		 * for (IPluginModelBase element : base) { IPluginBase pbase =
		 * element.getPluginBase(); if (pluginIds.contains(pbase.getId())) {
		 * log("Loading Dependencies for " + pbase.getId()); Set<String>
		 * dependecies = buildDependencies(pbase.getPluginModel(),
		 * pbase.getId()); allDependencies.addAll(dependecies); } }
		 */
		for (String id : pluginIds) {
			allDependencies.add(id);
		}
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		List<TargetBundle> tBundles = collectTargetBundles(activeTargetDefinition, allDependencies);
		List<Bundle> loadedBundle = new ArrayList<Bundle>();
		for (TargetBundle bundle : tBundles) {
			try {

				Bundle bndl = context.installBundle(bundle.getBundleInfo().getLocation().toString());
				if (bndl.getState() == Bundle.INSTALLED) {
					loadedBundle.add(bndl);
				} else {
					log("ERROR: " + bndl.getSymbolicName() + " - STATE: " + bndl.getState() + " / " + Bundle.INSTALLED);
				}
			} catch (Exception e) {
				logException(e);
			}
		}

		refreshTotal(context.getBundle(0));
		//refreshPackages(loadedBundle, context);
	}
	
	public void refreshTotal(Bundle systemBundle) {
		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = new FrameworkListener() {
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
					synchronized (flag) {
						log("DOING REFRESH: "+event.toString());
						flag[0] = true;
						flag.notifyAll();
					}
				}
			}
		};
		context.addFrameworkListener(listener);
		FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
		frameworkWiring.refreshBundles(null);
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		frameworkWiring.resolveBundles(Arrays.asList(context.getBundle()));
		try {
			Class<?> libCls = context.getBundle().loadClass("com.ibm.xsp.library.XspLibrary");
			log("2nd Try Lib class is " + libCls);
		} catch (Exception ex) {
			logException(ex);
		}
		log("Refresh done...");
	}

	public void refreshPackages(List<Bundle> bundles, BundleContext context) {
		ServiceReference<?> packageAdminRef = context.getServiceReference(PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null) {
				return;
			}
		}

		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = new FrameworkListener() {
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
					synchronized (flag) {
						log("DOING REFRESH: "+event.toString());
						flag[0] = true;
						flag.notifyAll();
					}
				}
			}
		};
		context.addFrameworkListener(listener);

		Bundle core = Platform.getBundle("org.openntf.eclipse.xpdesigner.core");
		try {
			log("CORE STATE: " + core.getState() + " / Active would be= " + Bundle.ACTIVE);
		} catch (Exception ex) {
			logException(ex);
		}
		Bundle core2 = Platform.getBundle("org.eclipse.core.resources");
		// bundles.add(core);
		bundles.add(core2);
		packageAdmin.refreshPackages(bundles.toArray(new Bundle[bundles.size()]));
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			log("CORE STATE after REFRESH: " + core.getState() + " / Active would be= " + Bundle.ACTIVE);
		} catch (Exception ex) {
			logException(ex);
		}
		packageAdmin.resolveBundles(new Bundle[] { core });
		try {
			log("CORE STATE after Resolve: " + core.getState() + " / Active would be= " + Bundle.ACTIVE);
		} catch (Exception ex) {
			logException(ex);
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
		try {
			Class<?> libCls = core.loadClass("com.ibm.xsp.library.XspLibrary");
			log("2nd Try Lib class is " + libCls);
		} catch (Exception ex) {
			logException(ex);
		}
		try {
			log("CORE STATE after Access: " + core.getState() + " / Active would be= " + Bundle.ACTIVE);
		} catch (Exception ex) {
			logException(ex);
		}

	}
	
	private ITargetDefinition getActiveTargetDefinition() {
		BundleContext bc = getBundle().getBundleContext();
		ServiceReference<ITargetPlatformService> ref = bc.getServiceReference(ITargetPlatformService.class);

		ITargetPlatformService tpService = bc.getService(ref);
		ITargetDefinition targetDef = null;
		try {
			for (ITargetHandle def : tpService.getTargets(null)) {
				if ("XPDE-Server".equals(def.getTargetDefinition().getName())) {
					targetDef = def.getTargetDefinition();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return targetDef;

	}


}
