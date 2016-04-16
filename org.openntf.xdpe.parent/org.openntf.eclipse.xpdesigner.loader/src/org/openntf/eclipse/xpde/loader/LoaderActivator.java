package org.openntf.eclipse.xpde.loader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.FrameworkWiring;

public class LoaderActivator implements BundleActivator {

	private static LoaderActivator plugin;

	private BundleContext context;
	private MessageConsoleStream consoleLog;
	private Bundle ibmCoreBundle;

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		plugin = this;
		MessageConsole console = findConsole("XDPE -Console");
		consoleLog = console.newMessageStream();
		// bundleInstall(context);
		/*for (String message : Startup.getStartup().getLogMessages()) {
			log(message);
		}
		*/
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		consoleLog.close();

	}

	private void bundleInstall(BundleContext context) {
		List<String> pluginIds = Arrays.asList("com.ibm.designer.lib.acf", "com.ibm.designer.lib.jsf", "com.ibm.pvc.servlet", "com.ibm.xsp.core");
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

		refreshPackages(loadedBundle, context);
	}

	public void refreshPackages(List<Bundle> bundles, BundleContext context) {
		Bundle systemBundle = context.getBundle(0);
		Bundle myBundle = context.getBundle();
		// Bundle xdpeCore = null;
		for (Bundle bndl : bundles) {
			if ("com.ibm.xsp.core".equals(bndl.getSymbolicName())) {
				log("Starting: " + bndl);
				try {
					ibmCoreBundle = bndl;
					bndl.start();
				} catch (BundleException e) {
					logException(e);
				}
			}
		}
		// xdpeCore = context.getBundle("org.openntf.eclipse.xpdesigner.core");
		log("MyBundle is:  " + myBundle);
		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = new FrameworkListener() {
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
					synchronized (flag) {
						log("DOING REFRESH: " + event.toString());
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
		if (myBundle != null) {
			frameworkWiring.resolveBundles(Arrays.asList(myBundle));
		}
		context.removeFrameworkListener(listener);
		log("Refresh done...");
		try {
			Class<?> cl = ibmCoreBundle.loadClass("com.ibm.xsp.library.XspLibrary");
			log("Class is " + cl);
			log("Try to load com.ibm.xsp.registry.FacesRegistry");
			Class<?> clReg = ibmCoreBundle.loadClass("com.ibm.xsp.registry.FacesRegistry");
			log("Reg is " + clReg);
			log("Try to load class from MyBundle Core");
			Class<?> clCoreXSP = myBundle.loadClass("com.ibm.xsp.library.XspLibrary");
			log("Class is " + clCoreXSP);

		} catch (Exception ex) {
			logException(ex);
		}

	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LoaderActivator getDefault() {
		return plugin;
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
		consoleLog.println(">>LOADER: " + message);
	}

	public void logException(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		log(sw.toString());
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

	private ITargetDefinition getActiveTargetDefinition() {
		BundleContext bc = context;
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

	public Bundle getIbmCoreBundle() {
		return ibmCoreBundle;
	}

	public void setIbmCoreBundle(Bundle ibmCoreBundle) {
		this.ibmCoreBundle = ibmCoreBundle;
	}

}
